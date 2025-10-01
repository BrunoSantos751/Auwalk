import React from "react";
import "./perfilTotal.css";
import avatarExemplo from "../../../assets/dog3.webp";
import { useNavigate } from "react-router-dom";

interface PerfilTotalProps {
  name?: string;
  avatarUrl?: string; // imagem do backend
  onEditarSenha?: () => void;
}

const PerfilTotal: React.FC<PerfilTotalProps> = ({
  name,
  avatarUrl,
  onEditarSenha,
}) => {
  const navigate = useNavigate();
  return (
    <>
      {/* Cabeçalho do perfil */}
      <header className="profile-header">
        <h1>Perfil Tutor</h1>
        <img
          // usa a imagem do backend se existir, senão usa a imagem local de exemplo
          src={avatarUrl || avatarExemplo}
          alt={`${name} avatar`}
          className="profile-avatar"
        />
        {/* se name estiver vazio, mostra "Nome do Tutor" */}
        <h1 className="profile-name">{name || "Nome do Tutor"}</h1>
      </header>

      {/* Ações do usuário */}
      <nav className="profile-actions">
        <div className="btn-group btn-up">
          <button onClick={() => navigate("/dados")}>Meus Dados</button>
          <button onClick={() => navigate("/perfilpet")}>Dados do Pet</button>
          <button onClick={() => navigate("/historico")}>Histórico</button>
        </div>
        <div className="btn-group btn-down">
          <button onClick={() => navigate("/search")}>Novo Agendamento</button>
          <button onClick={() => navigate("/upgradeprestador")}>
            Quero ser prestador
          </button>
          <button onClick={onEditarSenha}>Editar Senha</button>
        </div>
        <div className="espaço-embranco"></div>
      </nav>
    </>
  );
};

export default PerfilTotal;
