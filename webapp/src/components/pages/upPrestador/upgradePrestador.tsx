import { useState } from "react";
import "./up.css";
import dogcatImage from "../../../assets/dogcat.webp";

const UpgradePrestador: React.FC = () => {
  const [bio, setBio] = useState("");
  const [experiencia, setExperiencia] = useState("");
  const [cpf, setCpf] = useState("");
  const [mensagem, setMensagem] = useState<{ type: 'success' | 'error', text: string } | null>(null);

  const handleSalvar = async () => {
    setMensagem(null);

    try {
      // 1. Pega o token do localStorage
      const authToken = localStorage.getItem('authToken');

      if (!authToken) {
        setMensagem({ type: 'error', text: 'Erro de autenticação. Faça login novamente.' });
        return;
      }

      // 2. Decodifica o token para extrair o id do usuário (igual ao seu exemplo)
      const payload = JSON.parse(atob(authToken.split(".")[1]));
      const idUsuario = payload.sub;

      // 3. Monta o corpo da requisição, incluindo o idUsuario
      const requestBody = {
        idUsuario: idUsuario,
        bio: bio,
        experiencia: experiencia,
        documento: cpf,
      };

      // 4. Faz a requisição PUT para o backend (sem o cabeçalho Authorization)
      const response = await fetch('http://auwalk.us-east-2.elasticbeanstalk.com/provider/profile', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`
        },
        body: JSON.stringify(requestBody),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        setMensagem({ type: 'success', text: data.message || 'Perfil atualizado com sucesso!' });
      } else {
        setMensagem({ type: 'error', text: data.message || 'Ocorreu um erro ao atualizar o perfil.' });
      }
    } catch (error) {
      console.error("Erro ao salvar perfil:", error);
      // Este erro pode acontecer se o token for inválido e não puder ser decodificado
      setMensagem({ type: 'error', text: 'Ocorreu um erro. Verifique seus dados ou tente novamente.' });
    }
  };

  return (
    <div className="container-upgrade">
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
          Bio
          <input
            type="text"
            name="bio"
            placeholder="Escreva uma breve descrição sobre você..."
            value={bio}
            onChange={(e) => setBio(e.target.value)}
          />
        </label>
        <label>
          Experiência
          <input
            type="text"
            name="experiencia"
            placeholder="Descreva sua experiência profissional..."
            value={experiencia}
            onChange={(e) => setExperiencia(e.target.value)}
          />
        </label>
        <label>
          CPF
          <input
            type="text"
            name="cpf"
            placeholder="Digite seu CPF aqui"
            value={cpf}
            onChange={(e) => setCpf(e.target.value)}
          />
        </label>

        {mensagem && (
          <div className={`message ${mensagem.type}`}>
            {mensagem.text}
          </div>
        )}

        <button type="button" className="up-salvar" onClick={handleSalvar}>
          Salvar
        </button>
      </div>
    </div>
  );
};

export default UpgradePrestador;