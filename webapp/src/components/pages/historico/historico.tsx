import React, { useState, useEffect } from 'react';
import './Historico.css';

// Interface unificada para representar qualquer tipo de agendamento
interface Agendamento {
  id: number;
  tipo: string; // "Passeio" ou "PetSitting"
  status: string;
  data_inicio: string;
  data_fim?: string; // Opcional, apenas para Pet Sitting
  nome_contraparte: string; // Nome do cliente ou do prestador
  nome_pet: string;
  papel: 'cliente' | 'prestador'; // Seu papel no agendamento
}

const Historico: React.FC = () => {
    const [agendamentos, setAgendamentos] = useState<Agendamento[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchHistorico = async () => {
            const token = localStorage.getItem("authToken");
            if (!token) {
                setError("Você precisa estar logado para ver o histórico.");
                setLoading(false);
                return;
            }

            try {
                const [resCliente, resPrestador] = await Promise.all([
                    fetch('http://localhost:8080/schedule/my-appointments/client', {
                        headers: { 'Authorization': `Bearer ${token}` }
                    }),
                    fetch('http://localhost:8080/schedule/my-appointments/provider', {
                        headers: { 'Authorization': `Bearer ${token}` }
                    })
                ]);

                if (!resCliente.ok || !resPrestador.ok) {
                    throw new Error("Falha ao buscar o histórico.");
                }

                const resultCliente = await resCliente.json();
                const resultPrestador = await resPrestador.json();

                const agendamentosCliente: Agendamento[] = resultCliente.data.map((item: any) => ({ ...item, papel: 'cliente' }));
                const agendamentosPrestador: Agendamento[] = resultPrestador.data.map((item: any) => ({ ...item, papel: 'prestador' }));

                const todosAgendamentos = [...agendamentosCliente, ...agendamentosPrestador];
                todosAgendamentos.sort((a, b) => new Date(b.data_inicio).getTime() - new Date(a.data_inicio).getTime());

                setAgendamentos(todosAgendamentos);

            } catch (err) {
                setError("Não foi possível carregar o histórico. Tente novamente mais tarde.");
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchHistorico();
    }, []);

    const formatarData = (dataStr: string) => {
        return new Date(dataStr).toLocaleDateString('pt-BR', {
            day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
        });
    };

    if (loading) {
        return <div className="historico-container"><p>Carregando histórico...</p></div>;
    }

    if (error) {
        return <div className="historico-container"><p style={{ color: 'red' }}>{error}</p></div>;
    }

    return (
        <div className="historico-container">
            <h1 className="historico-title">Histórico de Agendamentos</h1>
            <div className="agendamentos-list">
                {agendamentos.length > 0 ? (
                    agendamentos.map(item => (
                        <div key={`${item.tipo}-${item.id}`} className="agendamento-card">
                            <div className="agendamento-info">
                                <h3>{item.tipo} com {item.nome_pet}</h3>
                                <p>
                                    {item.papel === 'cliente'
                                        ? `Prestador: ${item.nome_contraparte}`
                                        : `Cliente: ${item.nome_contraparte}`
                                    }
                                </p>
                                <p><strong>Início:</strong> {formatarData(item.data_inicio)}</p>
                                {item.data_fim && <p><strong>Fim:</strong> {formatarData(item.data_fim)}</p>}
                            </div>
                            <div className="agendamento-status-role">
                                <span className="agendamento-role">
                                    {item.papel === 'cliente' ? "Você Contratou" : "Você Prestou o Serviço"}
                                </span>
                                <div className={`agendamento-status status-${item.status.toLowerCase()}`}>
                                    <span className="status-dot"></span>
                                    {item.status}
                                </div>
                            </div>
                        </div>
                    ))
                ) : (
                    <p>Nenhum agendamento encontrado no seu histórico.</p>
                )}
            </div>
        </div>
    );
};

export default Historico;