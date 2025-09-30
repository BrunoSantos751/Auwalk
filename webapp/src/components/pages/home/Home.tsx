import './Home.css';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';

export default function Home() {
  const dataAtual = new Date().toISOString().split("T")[0];
  const navigate = useNavigate();
  const [servico, setServico] = useState("");
  const [entrada, setEntrada] = useState("");
  const [saida, setSaida] = useState("");

  function handleSubmit(e) {
    e.preventDefault();
    // envia filtros para a página de pesquisa
    navigate('/search', {
      state: { servico, entrada, saida }
    });
  }

  return (
    <div className="home">
      <div className="hero-geral">
        <div className="hero">
          <div className="hero-content">
            <p className='topo-esquerda'>Sem tempo para o <br /> passeio hoje?</p>
            <p className='baixo-direita'>A gente leva por você!</p>
          </div>

          <div className="busca">
            <form className='formBusca' onSubmit={handleSubmit}>
              <div className="opcoes">
                <label htmlFor="servicoId">
                  <p className='busca-title'>Serviço</p>
                  <select
                    name="servicoId"
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
                  <p className='busca-title'>Endereço</p>
                  <input
                    type="text"
                    name="enderecoId"
                    id="enderecoId"
                    placeholder='Endereço'
                  />
                </label>
              </div>

              <div className="opcoes-data">
                <label htmlFor="entradaId">
                  <p className='busca-title'>Entrada</p>
                  <input
                    type="date"
                    name="entradaName"
                    id="entradaId"
                    min={dataAtual}
                    value={entrada}
                    onChange={(e) => setEntrada(e.target.value)}
                  />
                </label>
                <label htmlFor="saidaId">
                  <p className='busca-title'>Saída</p>
                  <input
                    type="date"
                    name="saidaName"
                    id="saidaId"
                    min={dataAtual}
                    value={saida}
                    onChange={(e) => setSaida(e.target.value)}
                    disabled={servico !== "PetSitting"}
                  />
                </label>
              </div>

              <button type="submit" className='btn-buscar'>Buscar</button>
            </form>
          </div>
        </div>
      </div>

      <div className="info-atencao">
        <h2 className="info-title">Seu pet merece mais atenção e cuidado!</h2>
        <div className="info-background">
          <p className='info-texto'>
            No <span className='palavra-destaque'>Auwalk</span>, você encontra passeadores e pet sitters de confiança, 
            agenda serviços com <span className="palavra-destaque">poucos cliques</span> e acompanha tudo de forma segura e prática.
          </p>    
        </div>
        <div className="imagem-gato"></div>
      </div>

      <div className="melhorAvaliados">
        <h2 className="melhorAvaliados-title">Os mais bem avaliados</h2>
        <div className="melhorAvaliadosCards">
          {["Laura Costa", "Maria Layanne", "Ana Paula", "João Pedro"].map((nome, index) => (
            <div className="card" key={index}>
              <span className="card-imagem">:)</span>
              <p className="card-nome">{nome}</p>
              <span className="card-estrelas">⭐⭐⭐⭐⭐</span>
              <button className="btn-perfil">Ver perfil</button>
            </div>
          ))}
        </div>
      </div>

      <div className="bottomSection">
        <div className="bottomSection-img"></div>
        <p className='topo-esquerda'>Amor e atenção fazem parte do<br /> bem-estar do seu pet.</p>
        <p className='baixo-direita'>Baixe o app Auwalk agora!</p>
        <div className="googleApp"></div>
      </div>
    </div>
  );
}
