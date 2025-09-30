import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import './Pesquisa.css';
import profilePic from "../../../assets/profile-pic.png";

export default function Pesquisa() {
    const location = useLocation();
    const filtros = location.state || {}; // recebe filtros da Home
    const [servico, setServico] = useState(filtros.servico || "");
    const [entrada, setEntrada] = useState(filtros.entrada || "");
    const [saida, setSaida] = useState(filtros.saida || "");
    const [servicosEncontrados, setServicosEncontrados] = useState([]);
    const dataAtual = new Date().toISOString().split("T")[0];

    // busca automática quando a página carrega com filtros
    useEffect(() => {
        if (filtros.servico || filtros.entrada || filtros.saida) {
            handleFetch();
        }
    }, []);

    async function handleFetch() {
        const payload = {};
        if (entrada) payload.data = entrada;
        if (servico) payload.tipoServico = servico;

        const response = await fetch('http://localhost:8080/search', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        setServicosEncontrados(data.data?.servicos || []);
    }

    async function handleSubmit(e) {
        e.preventDefault();
        handleFetch();
    }

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
                            <select
                                id="servicoId"
                                value={servico}
                                onChange={(e) => setServico(e.target.value)}
                            >
                                <option value="" disabled>Selecione uma opção</option>
                                <option value="Passeio">Passeios</option>
                                <option value="PetSitting">Pet sitter</option>
                            </select>
                        </label>
                        <label htmlFor="enderecoId">
                            <p className="busca-title">Endereço</p>
                            <input type="text" placeholder="Endereço" />
                        </label>
                    </div>

                    <div className="opcoes-data">
                        <label htmlFor="entradaId">
                            <p className="busca-title">Entrada</p>
                            <input
                                type="date"
                                value={entrada}
                                onChange={(e) => setEntrada(e.target.value)}
                            />
                        </label>
                        <label htmlFor="saidaId">
                            <p className="busca-title">Saída</p>
                            <input
                                type="date"
                                min={dataAtual}
                                value={saida}
                                onChange={(e) => setSaida(e.target.value)}
                                disabled={servico !== "PetSitting"}
                            />
                        </label>
                    </div>

                    <button className="btn-buscar">Buscar</button>
                </form>
            </div>

            {/* Cards */}
            <div className="cardsContainer">
                {servicosEncontrados.length > 0 ? (
                    servicosEncontrados.map((s) => (
                        <div key={s.idServico} className="card">
                            <img src={profilePic} alt="foto de perfil" className='profile-pic' />
                            <div className="tipoServico">
                                <h3 className='tipoServico-title'>{s.tipoServico}</h3>
                                <p className='tipoServico-desc'>{s.descricao}</p>
                            </div>
                            <p className="preco">R$ {s.preco}</p>
                            <button className="btn-agendar">Agendar</button>
                            <button className="btn-verperfil">Ver perfil</button>
                        </div>
                    ))
                ) : (
                    <p>Nenhum serviço encontrado.</p>
                )}
            </div>
        </div>
    );
}
