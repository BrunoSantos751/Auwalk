import React, { useState } from 'react';
import './AgendamentoModal.css'; // Reutiliza o estilo base do modal
import './Pesquisa.css'; // Puxa os estilos do formulário que acabamos de criar

interface CriarServicoModalProps {
    idPrestador: number;
    onClose: () => void;
    onServiceCreated: () => void; // Função para atualizar a lista após criar
}

const CriarServicoModal: React.FC<CriarServicoModalProps> = ({ idPrestador, onClose, onServiceCreated }) => {
    const [tipoServico, setTipoServico] = useState('Passeio');
    const [descricao, setDescricao] = useState('');
    const [preco, setPreco] = useState('');
    const [duracaoEstimada, setDuracaoEstimada] = useState('');
    const [inicioDisp, setInicioDisp] = useState('');
    const [fimDisp, setFimDisp] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const token = localStorage.getItem('authToken');
        if (!token) {
            alert("Sessão expirada. Faça login novamente.");
            return;
        }

        const payload = {
            idPrestador,
            tipoServico,
            descricao,
            preco: parseFloat(preco),
            duracaoEstimada: parseInt(duracaoEstimada, 10),
            disponibilidades: [{
                inicioHorarioAtendimento: inicioDisp,
                fimHorarioAtendimento: fimDisp,
            }]
        };

        try {
            const response = await fetch('http://localhost:8080/services', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}` // Endpoint de criação precisa de autenticação
                },
                body: JSON.stringify(payload)
            });

            const result = await response.json();
            if (response.ok && result.success) {
                alert("Serviço criado com sucesso!");
                onServiceCreated(); // Chama a função para fechar e atualizar
            } else {
                alert(`Erro ao criar serviço: ${result.message || 'Verifique os dados.'}`);
            }
        } catch (error) {
            alert("Erro de conexão ao criar serviço.");
        }
    };

    return (
        <div className="modal-backdrop" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="modal-close-btn" onClick={onClose}>&times;</button>
                <h2 className="modal-title">Criar Novo Serviço</h2>
                <form className="criar-servico-form" onSubmit={handleSubmit}>
                    <label>
                        Tipo de Serviço
                        <select value={tipoServico} onChange={e => setTipoServico(e.target.value)}>
                            <option value="Passeio">Passeio</option>
                            <option value="PetSitting">Pet Sitting (Visita)</option>
                        </select>
                    </label>
                    <label>
                        Descrição
                        <textarea value={descricao} onChange={e => setDescricao(e.target.value)} required />
                    </label>
                    <label>
                        Preço (R$)
                        <input type="number" value={preco} onChange={e => setPreco(e.target.value)} required min="0" step="0.01" />
                    </label>
                    <label>
                        Duração da Visita (minutos)
                        <input type="number" value={duracaoEstimada} onChange={e => setDuracaoEstimada(e.target.value)} required min="1" />
                    </label>
                    <div className="disponibilidade-group">
                        <label>
                            Início da Disponibilidade
                            <input type="datetime-local" value={inicioDisp} onChange={e => setInicioDisp(e.target.value)} required />
                        </label>
                        <label>
                            Fim da Disponibilidade
                            <input type="datetime-local" value={fimDisp} onChange={e => setFimDisp(e.target.value)} required />
                        </label>
                    </div>
                    <button type="submit" className="btn-agendar">Salvar Serviço</button>
                </form>
            </div>
        </div>
    );
};

export default CriarServicoModal;