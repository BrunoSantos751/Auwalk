import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "./PerfilPrestador.css";
import profilePic from "../../../assets/profile-pic.png";

interface PerfilPrestadorData {
    id_prestador: number;
    id_usuario: number;
    bio: string | null;
    experiencia: string | null;
    documento: string;
}

interface Servico {
    idServico: number;
    idPrestador: number;
    tipoServico: string;
    descricao: string | null;
    preco: number | null;
    duracaoEstimada: number;
    disponibilidades: any[];
}

interface Avaliacao {
    idAvaliacao: number;
    idUsuario: number;
    idServico: number;
    nota: number;
    comentario: string | null;
    data: string;
}

interface MediaAvaliacao {
    media: number;
    totalAvaliacoes: number;
    nota_media?: number; // Campo do backend
    total_avaliacoes?: number; // Campo do backend
}

const PerfilPrestador: React.FC = () => {
    const { idUsuario } = useParams<{ idUsuario: string }>();
    const navigate = useNavigate();
    const [perfil, setPerfil] = useState<PerfilPrestadorData | null>(null);
    const [servicos, setServicos] = useState<Servico[]>([]);
    const [avaliacoes, setAvaliacoes] = useState<Avaliacao[]>([]);
    const [mediaAvaliacao, setMediaAvaliacao] = useState<MediaAvaliacao | null>(null);
    const [nomePrestador, setNomePrestador] = useState<string>("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (idUsuario) {
            carregarDadosPrestador(parseInt(idUsuario));
        }
    }, [idUsuario]);

    const carregarDadosPrestador = async (idUsuarioParam: number) => {
        try {
            setLoading(true);
            setError(null);

            // Buscar perfil do prestador
            const perfilResponse = await fetch(`http://http://auwalk.us-east-2.elasticbeanstalk.com/provider/profile?idUsuario=${idUsuarioParam}`);
            if (!perfilResponse.ok) {
                throw new Error('Perfil não encontrado');
            }
            const perfilData = await perfilResponse.json();
            if (perfilData.success && perfilData.data) {
                setPerfil(perfilData.data);
                // Obter nome do usuário se disponível
                if (perfilData.data.nome_usuario) {
                    setNomePrestador(perfilData.data.nome_usuario);
                }
                const idPrestador = perfilData.data.id_prestador;

                // Buscar serviços do prestador
                const servicosResponse = await fetch(`http://http://auwalk.us-east-2.elasticbeanstalk.com/services?idPrestador=${idPrestador}`);
                if (servicosResponse.ok) {
                    const servicosData = await servicosResponse.json();
                    if (servicosData.success && servicosData.data) {
                        setServicos(servicosData.data);
                    }
                }

                // Buscar avaliações do prestador
                const avaliacoesResponse = await fetch(`http://http://auwalk.us-east-2.elasticbeanstalk.com/avaliacoes-prestador?idUsuario=${idUsuarioParam}`);
                if (avaliacoesResponse.ok) {
                    const avaliacoesData = await avaliacoesResponse.json();
                    if (Array.isArray(avaliacoesData)) {
                        setAvaliacoes(avaliacoesData);
                    }
                }

                // Buscar média de avaliações
                const mediaResponse = await fetch(`http://http://auwalk.us-east-2.elasticbeanstalk.com/avaliacoes-prestador/media?idUsuario=${idUsuarioParam}`);
                if (mediaResponse.ok) {
                    const mediaData = await mediaResponse.json();
                    // O backend retorna nota_media e total_avaliacoes
                    if (mediaData.nota_media !== undefined || mediaData.media !== undefined) {
                        // Normalizar os dados do backend para o formato esperado
                        const mediaNormalizada: MediaAvaliacao = {
                            media: mediaData.nota_media ?? mediaData.media ?? 0,
                            totalAvaliacoes: mediaData.total_avaliacoes ?? mediaData.totalAvaliacoes ?? 0
                        };
                        setMediaAvaliacao(mediaNormalizada);
                    }
                }

                // Buscar nome do usuário (precisamos de um endpoint para isso ou usar o nome do serviço)
                // Por enquanto, vamos tentar obter do primeiro serviço ou deixar vazio
            }
        } catch (err) {
            console.error("Erro ao carregar dados do prestador:", err);
            setError("Erro ao carregar perfil do prestador");
        } finally {
            setLoading(false);
        }
    };

    const formatarData = (dataStr: string) => {
        try {
            const data = new Date(dataStr);
            return data.toLocaleDateString('pt-BR');
        } catch {
            return dataStr;
        }
    };

    const renderEstrelas = (nota: number) => {
        return "★".repeat(nota) + "☆".repeat(5 - nota);
    };

    if (loading) {
        return (
            <div className="perfil-prestador-container">
                <div className="loading">Carregando...</div>
            </div>
        );
    }

    if (error || !perfil) {
        return (
            <div className="perfil-prestador-container">
                <div className="error">
                    <p>{error || "Perfil não encontrado"}</p>
                    <button onClick={() => navigate("/search")}>Voltar para busca</button>
                </div>
            </div>
        );
    }

    return (
        <div className="perfil-prestador-container">
            <header className="profile-header">
                <h1>Perfil do Prestador</h1>
            </header>

            <div className="profile-main">
                <div className="profile-left">
                    <img
                        src={profilePic}
                        alt="Foto do prestador"
                        className="profile-avatar"
                    />
                    <div className="profile-info">
                        <h2 className="profile-name">{nomePrestador || "Prestador"}</h2>
                        <div className="rating-container">
                            {mediaAvaliacao && mediaAvaliacao.totalAvaliacoes > 0 && mediaAvaliacao.media > 0 ? (
                                <>
                                    <div className="rating-stars">
                                        {renderEstrelas(Math.round(mediaAvaliacao.media))}
                                    </div>
                                    <p className="rating-text">
                                        {mediaAvaliacao.media.toFixed(1)} ({mediaAvaliacao.totalAvaliacoes} {mediaAvaliacao.totalAvaliacoes === 1 ? 'avaliação' : 'avaliações'})
                                    </p>
                                </>
                            ) : (
                                <p className="rating-text no-rating">
                                    Ainda não há avaliações para este prestador
                                </p>
                            )}
                        </div>
                    </div>
                </div>

                <div className="profile-right">
                    {perfil.bio && (
                        <section className="profile-section">
                            <h3>Sobre</h3>
                            <p>{perfil.bio}</p>
                        </section>
                    )}

                    {perfil.experiencia && (
                        <section className="profile-section">
                            <h3>Experiência</h3>
                            <p>{perfil.experiencia}</p>
                        </section>
                    )}
                </div>
            </div>

            <div className="profile-content">

                {servicos.length > 0 && (
                    <section className="profile-section">
                        <h3>Historico de serviços</h3>
                        <div className="servicos-grid">
                            {servicos.map((servico) => (
                                <div key={servico.idServico} className="servico-card">
                                    <h4>{servico.tipoServico}</h4>
                                    {servico.descricao && <p>{servico.descricao}</p>}
                                    <div className="servico-info">
                                        <span className="servico-preco">R$ {servico.preco?.toFixed(2) || "0.00"}</span>
                                        <span className="servico-duracao">{servico.duracaoEstimada} min</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </section>
                )}

                {avaliacoes.length > 0 && (
                    <section className="profile-section">
                        <h3>Avaliações</h3>
                        <div className="avaliacoes-list">
                            {avaliacoes.map((avaliacao) => (
                                <div key={avaliacao.idAvaliacao} className="avaliacao-card">
                                    <div className="avaliacao-header">
                                        <div className="avaliacao-estrelas">
                                            {renderEstrelas(avaliacao.nota)}
                                        </div>
                                        <span className="avaliacao-data">{formatarData(avaliacao.data)}</span>
                                    </div>
                                    {avaliacao.comentario && (
                                        <p className="avaliacao-comentario">{avaliacao.comentario}</p>
                                    )}
                                </div>
                            ))}
                        </div>
                    </section>
                )}

                {avaliacoes.length === 0 && (
                    <section className="profile-section">
                        <p className="no-avaliacoes">Ainda não há avaliações para este prestador.</p>
                    </section>
                )}
            </div>

            <div className="profile-actions">
                <button onClick={() => navigate("/search")} className="btn-voltar">
                    Voltar para busca
                </button>
            </div>
        </div>
    );
};

export default PerfilPrestador;

