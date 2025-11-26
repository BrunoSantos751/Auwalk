import React, { useState, useEffect } from 'react';
import './historico.css';

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
  id_servico?: number; // ID do serviço
  id_usuario_prestador?: number; // ID do usuário prestador (quando papel é cliente)
  id_usuario_cliente?: number; // ID do usuário cliente (quando papel é prestador)
}

const Historico: React.FC = () => {
    const [agendamentos, setAgendamentos] = useState<Agendamento[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [avaliacaoModal, setAvaliacaoModal] = useState<{ aberto: boolean; agendamento: Agendamento | null }>({
        aberto: false,
        agendamento: null
    });
    const [avaliacoesExistentes, setAvaliacoesExistentes] = useState<Set<number>>(new Set());

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

                // Verificar avaliações existentes para serviços concluídos
                const servicosConcluidos = todosAgendamentos
                    .filter(a => a.status.toLowerCase() === 'concluido' && a.id_servico)
                    .map(a => a.id_servico!);

                if (servicosConcluidos.length > 0) {
                    verificarAvaliacoesExistentes(servicosConcluidos);
                }

            } catch (err) {
                setError("Não foi possível carregar o histórico. Tente novamente mais tarde.");
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchHistorico();
    }, []);

    const verificarAvaliacoesExistentes = async (idServicos: number[]) => {
        try {
            const token = localStorage.getItem("authToken");
            if (!token) return;

            const promises = idServicos.map(async (idServico) => {
                try {
                    const response = await fetch(`http://localhost:8080/avaliacoes-prestador?idServico=${idServico}`, {
                        headers: { 'Authorization': `Bearer ${token}` }
                    });
                    if (response.ok) {
                        const avaliacoes = await response.json();
                        return avaliacoes.length > 0 ? idServico : null;
                    }
                } catch (e) {
                    console.error(`Erro ao verificar avaliação para serviço ${idServico}:`, e);
                }
                return null;
            });

            const resultados = await Promise.all(promises);
            const servicosComAvaliacao = resultados.filter(id => id !== null) as number[];
            setAvaliacoesExistentes(new Set(servicosComAvaliacao));
        } catch (error) {
            console.error("Erro ao verificar avaliações existentes:", error);
        }
    };

    const marcarComoConcluido = async (agendamento: Agendamento) => {
        try {
            const token = localStorage.getItem("authToken");
            if (!token) {
                alert('Você precisa estar logado para realizar esta ação.');
                return;
            }

            const endpoint = agendamento.tipo === 'Passeio'
                ? `http://localhost:8080/schedule/walk/${agendamento.id}/status`
                : `http://localhost:8080/schedule/sitter/${agendamento.id}/status`;

            const response = await fetch(endpoint, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ status: 'concluido' })
            });

            if (response.ok) {
                // Atualizar o status localmente
                setAgendamentos(prev => prev.map(a =>
                    a.id === agendamento.id && a.tipo === agendamento.tipo
                        ? { ...a, status: 'concluido' }
                        : a
                ));
                alert('Status atualizado para concluído!');
            } else {
                const data = await response.json();
                alert(data.message || 'Erro ao atualizar status.');
            }
        } catch (error) {
            console.error("Erro ao marcar como concluído:", error);
            alert('Erro ao atualizar status. Tente novamente.');
        }
    };

    const abrirModalAvaliacao = (agendamento: Agendamento) => {
        setAvaliacaoModal({ aberto: true, agendamento });
    };

    const fecharModalAvaliacao = () => {
        setAvaliacaoModal({ aberto: false, agendamento: null });
    };

    const salvarAvaliacao = async (nota: number, comentario: string) => {
        if (!avaliacaoModal.agendamento || !avaliacaoModal.agendamento.id_servico) {
            alert('Erro: informações do serviço não encontradas.');
            return;
        }

        try {
            const token = localStorage.getItem("authToken");
            if (!token) {
                alert('Você precisa estar logado para realizar esta ação.');
                return;
            }

            // Usar o id_usuario_prestador que já vem na resposta do agendamento
            const idUsuarioPrestador = avaliacaoModal.agendamento.id_usuario_prestador;

            if (!idUsuarioPrestador) {
                alert('Erro: informações do prestador não encontradas. Tente novamente.');
                return;
            }

            const response = await fetch('http://localhost:8080/avaliacoes-prestador', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    idUsuario: idUsuarioPrestador,
                    idServico: avaliacaoModal.agendamento.id_servico,
                    nota: nota,
                    comentario: comentario || null
                })
            });

            if (response.ok) {
                alert('Avaliação salva com sucesso!');
                setAvaliacoesExistentes(prev => new Set([...prev, avaliacaoModal.agendamento!.id_servico!]));
                fecharModalAvaliacao();
            } else {
                const data = await response.json();
                alert(data.message || 'Erro ao salvar avaliação.');
            }
        } catch (error) {
            console.error("Erro ao salvar avaliação:", error);
            alert('Erro ao salvar avaliação. Tente novamente.');
        }
    };

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
                    agendamentos.map(item => {
                        const statusLower = item.status.toLowerCase();
                        const isConcluido = statusLower === 'concluido';
                        const temAvaliacao = item.id_servico && avaliacoesExistentes.has(item.id_servico);
                        const podeAvaliar = isConcluido && item.papel === 'cliente' && item.id_servico && !temAvaliacao;
                        const podeMarcarConcluido = item.papel === 'prestador' && statusLower !== 'concluido' && statusLower !== 'cancelado';

                        return (
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
                                    <div className={`agendamento-status status-${statusLower}`}>
                                        <span className="status-dot"></span>
                                        {item.status}
                                    </div>
                                    {podeMarcarConcluido && (
                                        <button
                                            className="btn-marcar-concluido"
                                            onClick={() => marcarComoConcluido(item)}
                                        >
                                            Marcar como Concluído
                                        </button>
                                    )}
                                    {podeAvaliar && (
                                        <button
                                            className="btn-avaliar"
                                            onClick={() => abrirModalAvaliacao(item)}
                                        >
                                            Avaliar Serviço
                                        </button>
                                    )}
                                    {temAvaliacao && (
                                        <span className="avaliacao-feita">✓ Avaliado</span>
                                    )}
                                </div>
                            </div>
                        );
                    })
                ) : (
                    <p>Nenhum agendamento encontrado no seu histórico.</p>
                )}
            </div>

            {/* Modal de Avaliação */}
            {avaliacaoModal.aberto && avaliacaoModal.agendamento && (
                <ModalAvaliacao
                    agendamento={avaliacaoModal.agendamento}
                    onClose={fecharModalAvaliacao}
                    onSalvar={salvarAvaliacao}
                />
            )}
        </div>
    );
};

// Componente Modal de Avaliação
interface ModalAvaliacaoProps {
    agendamento: Agendamento;
    onClose: () => void;
    onSalvar: (nota: number, comentario: string) => void;
}

const ModalAvaliacao: React.FC<ModalAvaliacaoProps> = ({ agendamento, onClose, onSalvar }) => {
    const [nota, setNota] = useState<number>(5);
    const [comentario, setComentario] = useState<string>('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (nota < 1 || nota > 5) {
            alert('Por favor, selecione uma nota entre 1 e 5.');
            return;
        }
        onSalvar(nota, comentario);
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <h2>Avaliar Serviço</h2>
                <p><strong>{agendamento.tipo}</strong> com {agendamento.nome_pet}</p>
                <p>Prestador: {agendamento.nome_contraparte}</p>
                
                <form onSubmit={handleSubmit}>
                    <div className="avaliacao-nota">
                        <label>Nota (1 a 5 estrelas):</label>
                        <div className="estrelas-input">
                            {[1, 2, 3, 4, 5].map((valor) => (
                                <button
                                    key={valor}
                                    type="button"
                                    className={`estrela ${nota >= valor ? 'ativa' : ''}`}
                                    onClick={() => setNota(valor)}
                                >
                                    ★
                                </button>
                            ))}
                            <span className="nota-selecionada">{nota} estrelas</span>
                        </div>
                    </div>

                    <div className="avaliacao-comentario">
                        <label htmlFor="comentario">Comentário (opcional):</label>
                        <textarea
                            id="comentario"
                            value={comentario}
                            onChange={(e) => setComentario(e.target.value)}
                            rows={4}
                            placeholder="Deixe um comentário sobre o serviço..."
                        />
                    </div>

                    <div className="modal-buttons">
                        <button type="button" className="btn-cancelar" onClick={onClose}>
                            Cancelar
                        </button>
                        <button type="submit" className="btn-salvar">
                            Salvar Avaliação
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Historico;