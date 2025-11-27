import React, { useState, useEffect, useMemo } from 'react';
import './AgendamentoModal.css';

// Interfaces
interface Disponibilidade {
    inicio: string;
    fim: string;
}
interface Servico {
    idServico: number;
    tipoServico: string;
    disponibilidades: Disponibilidade[];
}
interface Pet {
    id_pet: number;
    nome: string;
}
interface ModalProps {
    service: Servico;
    onClose: () => void;
    onAgendar: (horario: string, petId: number) => void;
}

const AgendamentoModal: React.FC<ModalProps> = ({ service, onClose, onAgendar }) => {
    const [pets, setPets] = useState<Pet[]>([]);
    const [selectedPetId, setSelectedPetId] = useState<string>('');

    useEffect(() => {
        const fetchUserPets = async () => {
            try {
                const token = localStorage.getItem('authToken');
                if (!token) { return; }

                const payload = JSON.parse(atob(token.split(".")[1]));
                const idUsuario = payload.sub;

                if (!idUsuario) return;

                const response = await fetch(`http://auwalk.us-east-2.elasticbeanstalk.com/pets?idUsuario=${idUsuario}`);
                if (!response.ok) { throw new Error('Falha ao buscar os pets.'); }

                const result = await response.json();
                if (result.success && Array.isArray(result.data)) {
                    setPets(result.data);
                    if (result.data.length > 0) {
                        setSelectedPetId(result.data[0].id_pet.toString());
                    }
                }
            } catch (error) {
                console.error("Erro ao carregar pets do usuário:", error);
            }
        };
        fetchUserPets();
    }, []);

    const horariosAgrupados = useMemo(() => {
        const groups = new Map<string, Disponibilidade[]>();
        if (!service.disponibilidades) return groups;

        service.disponibilidades.forEach(horario => {
            const dataStr = horario.inicio.split('T')[0];
            if (!groups.has(dataStr)) {
                groups.set(dataStr, []);
            }
            groups.get(dataStr)!.push(horario);
        });
        return groups;
    }, [service.disponibilidades]);

    const handleAgendamentoClick = (horario: string) => {
        if (!selectedPetId) {
            alert('Por favor, selecione um pet para o agendamento.');
            return;
        }
        onAgendar(horario, parseInt(selectedPetId, 10));
    };

    const formatarHorario = (horarioString: string) => {
        const data = new Date(horarioString);
        return data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    };

    const formatarDataGrupo = (dataStr: string) => {
        const [ano, mes, dia] = dataStr.split('-');
        return `${dia}/${mes}/${ano}`;
    };

    return (
        <div className="modal-backdrop" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="modal-close-btn" onClick={onClose}>&times;</button>
                <h2 className="modal-title">Agendar {service.tipoServico}</h2>

                <label htmlFor="pet-select" className="pet-select-label">Selecione o seu Pet:</label>
                <select
                    id="pet-select"
                    className="modal-pet-select"
                    value={selectedPetId}
                    onChange={(e) => setSelectedPetId(e.target.value)}
                    disabled={pets.length === 0}
                >
                    {pets.length > 0 ? (
                        pets.map(pet => (
                            <option key={pet.id_pet} value={pet.id_pet}>{pet.nome}</option>
                        ))
                    ) : (
                        <option value="">Você ainda não cadastrou pets</option>
                    )}
                </select>

                <div className="horarios-container">
                    {Array.from(horariosAgrupados.entries()).length > 0 ? (
                        Array.from(horariosAgrupados.entries()).map(([data, horarios]) => (
                            <div key={data} className="date-group">
                                <h3 className="date-group-title">{formatarDataGrupo(data)}</h3>
                                <div className="time-slots">
                                    {horarios.map((horario, index) => (
                                        <button
                                            key={index}
                                            className="horario-btn"
                                            onClick={() => handleAgendamentoClick(horario.inicio)}
                                            disabled={pets.length === 0}
                                        >
                                            {formatarHorario(horario.inicio)}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        ))
                    ) : (
                        <p>Nenhum horário disponível para este dia.</p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default AgendamentoModal;