import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import './Pesquisa.css';
import profilePic from "../../../assets/profile-pic.png";
import AgendamentoModal from './AgendamentoModal';
import CriarServicoModal from './CriarServicoModal';
import MapaPrestadores from './MapaPrestadores';

// --- INTERFACES ---
interface Disponibilidade {
    inicio: string;
    fim: string;
}

interface Servico {
    idServico: number;
    idPrestador: number;
    tipoServico: string;
    descricao: string | null;
    preco: number | null;
    nomePrestador: string;
    duracaoEstimada: number;
    disponibilidades: Disponibilidade[];
}

interface SearchPayload {
    data?: string;
    tipoServico?: string;
}

interface PrestadorProfile {
    id_prestador: number;
    // inclua outros campos do perfil se precisar no futuro
}


export default function Pesquisa() {
    const location = useLocation();
    const navigate = useNavigate();
    const filtros = location.state || {};

    // --- ESTADOS (State) ---
    const [servico, setServico] = useState(filtros.servico || "");
    const [entrada, setEntrada] = useState(filtros.entrada || "");
    const [servicosEncontrados, setServicosEncontrados] = useState<Servico[]>([]);

    // Estados para o modal de Agendamento
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedService, setSelectedService] = useState<Servico | null>(null);

    // Estados para a funcionalidade de Prestador
    const [isPrestador, setIsPrestador] = useState(false);
    const [prestadorProfile, setPrestadorProfile] = useState<PrestadorProfile | null>(null);
    const [isCriarServicoModalOpen, setIsCriarServicoModalOpen] = useState(false);

    // Estado para o mapa
    const [isMapaOpen, setIsMapaOpen] = useState(false);

    // Estados para filtro por proximidade
    const [raioKm, setRaioKm] = useState<string>("");
    const [enderecoUsuario, setEnderecoUsuario] = useState<{
        latitude: number;
        longitude: number;
    } | null>(null);
    const [mostrarFiltroProximidade, setMostrarFiltroProximidade] = useState(false);

    const dataAtual = new Date().toISOString().split("T")[0];

    // --- EFEITOS (useEffect) ---
    useEffect(() => {
        handleFetch();
        checkProviderStatus();
        carregarEnderecoUsuario();
    }, []);

    // Carregar endereço do usuário logado
    const carregarEnderecoUsuario = async () => {
        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                setMostrarFiltroProximidade(false);
                return;
            }

            const payload = JSON.parse(atob(token.split(".")[1]));
            const idUsuario = parseInt(payload.sub, 10);

            const response = await fetch(`http://http://auwalk.us-east-2.elasticbeanstalk.com/enderecos?idUsuario=${idUsuario}`);
            if (!response.ok) {
                setMostrarFiltroProximidade(false);
                return;
            }

            const enderecos = await response.json();
            if (Array.isArray(enderecos) && enderecos.length > 0) {
                const endereco = enderecos[0];
                if (endereco.latitude && endereco.longitude && 
                    !isNaN(endereco.latitude) && !isNaN(endereco.longitude)) {
                    setEnderecoUsuario({
                        latitude: endereco.latitude,
                        longitude: endereco.longitude
                    });
                    setMostrarFiltroProximidade(true);
                } else {
                    setMostrarFiltroProximidade(false);
                }
            } else {
                setMostrarFiltroProximidade(false);
            }
        } catch (error) {
            console.error("Erro ao carregar endereço do usuário:", error);
            setMostrarFiltroProximidade(false);
        }
    };

    // --- FUNÇÕES ---
    async function handleFetch() {
        try {
            let todosServicos: Servico[] = [];
            
            // Se houver filtro de proximidade e endereço do usuário, buscar prestadores próximos primeiro
            if (raioKm && enderecoUsuario) {
                const raioMetros = parseInt(raioKm) * 1000; // Converter KM para metros
                
                console.log(`🔍 Buscando prestadores em um raio de ${raioKm}km...`);
                
                try {
                    const responseProximos = await fetch(
                        `http://http://auwalk.us-east-2.elasticbeanstalk.com/enderecos/proximos?latitude=${enderecoUsuario.latitude}&longitude=${enderecoUsuario.longitude}&raioMetros=${raioMetros}`
                    );
                    
                    if (responseProximos.ok) {
                        const prestadoresProximos = await responseProximos.json();
                        const idUsuariosProximos = prestadoresProximos.map((p: any) => p.id_usuario);
                        
                        console.log(`📍 Prestadores próximos encontrados: ${idUsuariosProximos.length}`, idUsuariosProximos);
                        
                        // Se não encontrou prestadores próximos, retornar lista vazia
                        if (idUsuariosProximos.length === 0) {
                            console.log('⚠️ Nenhum prestador encontrado no raio especificado');
                            setServicosEncontrados([]);
                            return;
                        }
                        
                        // Buscar todos os serviços com os filtros aplicados
                        const payload: SearchPayload = {};
                        if (entrada) payload.data = entrada;
                        if (servico) payload.tipoServico = servico;
                        
                        const response = await fetch('http://http://auwalk.us-east-2.elasticbeanstalk.com/search', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(payload)
                        });
                        
                        if (response.ok) {
                            const data = await response.json();
                            todosServicos = data.data?.servicos || [];
                            
                            // Filtrar apenas serviços dos prestadores próximos
                            // Lembrando que idPrestador na verdade é idUsuario
                            todosServicos = todosServicos.filter((s: Servico) => 
                                idUsuariosProximos.includes(s.idPrestador)
                            );
                            
                            console.log(`✅ Serviços filtrados por proximidade: ${todosServicos.length} de ${data.data?.servicos?.length || 0} serviços`);
                        }
                    } else {
                        console.error(`⚠️ Erro ao buscar prestadores próximos (status: ${responseProximos.status})`);
                    }
                } catch (error) {
                    console.error("Erro ao buscar prestadores próximos:", error);
                }
            } else {
                // Se não tiver filtro de proximidade, buscar normalmente
                const payload: SearchPayload = {};
                if (entrada) payload.data = entrada;
                if (servico) payload.tipoServico = servico;
                
                const response = await fetch('http://http://auwalk.us-east-2.elasticbeanstalk.com/search', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                
                if (!response.ok) { throw new Error('Erro na busca'); }
                const data = await response.json();
                todosServicos = data.data?.servicos || [];
            }
            
            setServicosEncontrados(todosServicos);
        } catch (error) {
            console.error("Falha ao buscar serviços:", error);
            setServicosEncontrados([]);
        }
    }

    const checkProviderStatus = async () => {
        const token = localStorage.getItem('authToken');
        if (!token) {
            setIsPrestador(false);
            return;
        }
        try {
            const response = await fetch('http://http://auwalk.us-east-2.elasticbeanstalk.com/provider/profile/me', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const data = await response.json();
            if (data.isPrestador) {
                setIsPrestador(true);
                setPrestadorProfile(data.data);
            } else {
                setIsPrestador(false);
            }
        } catch (error) {
            console.error("Erro ao verificar status de prestador:", error);
            setIsPrestador(false);
        }
    };

    function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        handleFetch();
    }

    // Refazer busca quando o raio de proximidade mudar
    useEffect(() => {
        // Só fazer busca automática se o raio foi alterado (não na montagem inicial)
        const timer = setTimeout(() => {
            if (raioKm !== "" || servico || entrada) {
                handleFetch();
            }
        }, 300); // Debounce de 300ms para evitar múltiplas buscas
        
        return () => clearTimeout(timer);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [raioKm]);

    const handleOpenModal = (service: Servico) => {
        const token = localStorage.getItem('authToken');
        if (!token) {
            alert('Você precisa estar logado para agendar um serviço.');
            navigate('/login');
            return;
        }
        setSelectedService(service);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedService(null);
    };

    const handleAgendar = async (horario: string, petId: number) => {
        if (!selectedService) return;
        try {
            const token = localStorage.getItem('authToken');
            if (!token) { throw new Error('Sessão expirada.'); }

            const payloadToken = JSON.parse(atob(token.split(".")[1]));
            const idCliente = parseInt(payloadToken.sub, 10);

            const isPasseio = selectedService.tipoServico.toLowerCase().includes('passeio');
            const endpoint = isPasseio ? 'http://http://auwalk.us-east-2.elasticbeanstalk.com/schedule/walk' : 'http://http://auwalk.us-east-2.elasticbeanstalk.com/schedule/sitter';

            const payload = { idCliente, idServico: selectedService.idServico, idPet: petId, dataHora: horario, observacoes: 'Agendado pela plataforma.' };

            const response = await fetch(endpoint, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
            await fetch('http://http://auwalk.us-east-2.elasticbeanstalk.com/chats', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
              },
              body: JSON.stringify({idDestinatario: selectedService.idPrestador })
            });

            const result = await response.json();


            if (response.ok && result.success) {
                alert('Serviço agendado com sucesso!');
                handleCloseModal();
                handleFetch();
            } else {
                alert(`Erro ao agendar: ${result.message || 'Tente novamente.'}`);
            }
        } catch (error) {
            console.error("Erro no agendamento:", error);
            const errorMessage = (error instanceof Error) ? error.message : 'Tente novamente.';
            alert(`Ocorreu um erro: ${errorMessage}`);
        }
    };

    const handleServiceCreated = () => {
        setIsCriarServicoModalOpen(false);
        handleFetch();
    };

    const handleVerPerfil = async (idPrestador: number) => {
        try {
            // Buscar idUsuario a partir do idPrestador usando o endpoint que aceita idPrestador
            const response = await fetch(`http://http://auwalk.us-east-2.elasticbeanstalk.com/provider/profile?idPrestador=${idPrestador}`);
            if (!response.ok) {
                alert('Erro ao carregar perfil do prestador.');
                return;
            }
            const data = await response.json();
            if (data.success && data.data) {
                const idUsuario = data.data.id_usuario;
                navigate(`/perfilprestador/${idUsuario}`);
            } else {
                alert('Perfil não encontrado.');
            }
        } catch (error) {
            console.error("Erro ao buscar perfil:", error);
            alert('Erro ao carregar perfil do prestador.');
        }
    };

    const getDatasDisponiveis = (disponibilidades: Disponibilidade[]): string => {
        if (!disponibilidades || disponibilidades.length === 0) return "Nenhuma";
        const datas = disponibilidades.map(d => d.inicio.split('T')[0]);
        const datasUnicas = [...new Set(datas)];
        const datasFormatadas = datasUnicas.map(dataStr => {
            const [ano, mes, dia] = dataStr.split('-');
            return `${dia}/${mes}/${ano}`;
        });
        return datasFormatadas.join(', ');
    };

    const servicosVisiveis = servicosEncontrados.filter(
        s => s.disponibilidades && s.disponibilidades.length > 0
    );

    // --- RENDERIZAÇÃO ---
    return (
        <div className="pesquisaPage">
            <div className="texto">
                <h1 className="textoTitle">Encontre seu passeador perfeito!</h1>
                <p className="textoParagrafo">Conectamos você aos melhores passeadores da sua região!</p>
            </div>
            <div className="pesquisaSection">
                <form className="formPesquisa" onSubmit={handleSubmit}>
                    <div className="opcoes">
                        <label htmlFor="servicoId">
                            <p className="busca-title">Serviço</p>
                            <select id="servicoId" value={servico} onChange={(e) => setServico(e.target.value)}>
                                <option value="" disabled>Selecione</option>
                                <option value="Passeio">Passeios</option>
                                <option value="PetSitting">Pet Sitter</option>
                            </select>
                        </label>
                        {mostrarFiltroProximidade && (
                            <label htmlFor="proximidadeId">
                                <p className="busca-title">Prestadores próximos</p>
                                <select 
                                    id="proximidadeId" 
                                    value={raioKm} 
                                    onChange={(e) => setRaioKm(e.target.value)}
                                >
                                    <option value="">Todos</option>
                                    <option value="1">Até 1 km</option>
                                    <option value="3">Até 3 km</option>
                                    <option value="5">Até 5 km</option>
                                    <option value="10">Até 10 km</option>
                                    <option value="15">Até 15 km</option>
                                    <option value="20">Até 20 km</option>
                                </select>
                            </label>
                        )}
                    </div>
                    <div className="opcoes-data">
                        <label htmlFor="entradaId">
                            <p className="busca-title">Data</p>
                            <input type="date" value={entrada} min={dataAtual} onChange={(e) => setEntrada(e.target.value)} />
                        </label>
                    </div>
                    <button type="submit" className="btn-buscar">Buscar</button>

                    {isPrestador && (
                        <button
                            type="button"
                            className="btn-criar-servico"
                            onClick={() => setIsCriarServicoModalOpen(true)}
                        >
                            Criar Serviço
                        </button>
                    )}
                </form>
                {servicosVisiveis.length > 0 && (
                    <button
                        type="button"
                        className="btn-ver-mapa"
                        onClick={() => setIsMapaOpen(true)}
                    >
                        Ver no Mapa
                    </button>
                )}
            </div>
            <div className="cardsContainer">
                {servicosVisiveis.length > 0 ? (
                    servicosVisiveis.map((s) => (
                        <div key={s.idServico} className="card">
                            <img src={profilePic} alt="foto de perfil" className='profile-pic' />
                            <h4 className="prestador-nome">{s.nomePrestador}</h4>
                            <div className="tipoServico">
                                <h3 className='tipoServico-title'>{s.tipoServico}</h3>
                                <p className='tipoServico-desc'>{s.descricao}</p>
                            </div>
                            <div className="datas-disponiveis">
                                <strong>Datas:</strong> {getDatasDisponiveis(s.disponibilidades)}
                            </div>
                            <p className="duracao">
                                <strong>Duração:</strong> {s.duracaoEstimada} min
                            </p>
                            <p className="preco">R$ {s.preco?.toFixed(2)}</p>
                            <button className="btn-agendar" onClick={() => handleOpenModal(s)}>Agendar</button>
                            <button className="btn-verperfil" onClick={() => handleVerPerfil(s.idPrestador)}>Ver perfil</button>
                        </div>
                    ))
                ) : (
                    <p>Nenhum serviço encontrado com horários disponíveis.</p>
                )}
            </div>

            {isModalOpen && selectedService && (
                <AgendamentoModal service={selectedService} onClose={handleCloseModal} onAgendar={handleAgendar} />
            )}

            {isCriarServicoModalOpen && prestadorProfile && (
                <CriarServicoModal
                    idPrestador={prestadorProfile.id_prestador}
                    onClose={() => setIsCriarServicoModalOpen(false)}
                    onServiceCreated={handleServiceCreated}
                />
            )}

            {isMapaOpen && (
                <MapaPrestadores
                    servicos={servicosVisiveis}
                    onClose={() => setIsMapaOpen(false)}
                />
            )}
        </div>
    );
}