import "./up.css";
import dogcatImage from "../../../assets/dogcat.webp";
const UpgradePrestador: React.FC = () => {
  return (
    <div className=" container-upgrade">
      <div className="topo-img">
        <img src={dogcatImage} alt="Cachorro e gato" className="pet-avatar" />
        <h1>Torne-se Prestador</h1>
        <h2>
          Adicione sua experiência e documentos para ativar seu perfil de
          prestador.
        </h2>
      </div>

      <div className="dados-group">
        <label>
          bio
          <input
            type="text"
            name="bio"
            placeholder="Escreva uma breve descrição sobre você..."
          />
        </label>
        <label>
          Experiência
          <input
            type="text"
            name="experiencia"
            placeholder="Descreva sua experiência profissional..."
          />
        </label>
        <label>
          CPF
          <input type="text" name="cpf" placeholder="Digite seu CPF aqui" />
        </label>
        <button type="button" className="up-salvar">
          Salvar
        </button>
      </div>
    </div>
  );
};
export default UpgradePrestador;
