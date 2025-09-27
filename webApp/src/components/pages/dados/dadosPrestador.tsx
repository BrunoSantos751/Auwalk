import React from "react";
import "./dadosTotal.css";
import avatarExemplo from "../../../assets/gato.webp";

interface DadosPrestadorProps {
  name?: string;
  email?: string;
  cpf?: string;
  nascimento?: string;
  celular?: string;
  pais?: string;
  estado?: string;
  bairro?: string;
  cep?: string;
  cidade?: string;
  endereco?: string;
  numero?: string;
  complemento?: string;
  avatarUrl?: string;
  onEditProfile?: () => void;
}

const DadosPrestador: React.FC<DadosPrestadorProps> = ({
  name = "",
  email = "",
  cpf = "",
  nascimento = "",
  celular = "",
  pais = "",
  estado = "",
  bairro = "",
  cep = "",
  cidade = "",
  endereco = "",
  numero = "",
  complemento = "",
  avatarUrl,
  onEditProfile,
}) => {
  return (
    <div className="perfil-tutor">
      <header className="profile-header">
        <h2>
          <strong>Primeira vez aqui?</strong> Complete seu cadastro e aproveite
          todos os recursos.
        </h2>
        <img
          src={avatarUrl || avatarExemplo}
          alt={`${name} avatar`}
          className="profile-avatar"
        />
      </header>

      <form className="profile-actions">
        <div className="btn-group-btn-up">
          <label>
            Nome completo
            <input type="text" value={name} disabled />
          </label>

          <label>
            CPF
            <input type="text" value={cpf} disabled />
          </label>

          <label>
            E-mail
            <input type="email" value={email} disabled />
          </label>

          <label>
            Celular
            <input type="tel" value={celular} disabled />
          </label>

          <label>
            Nascimento
            <input type="date" value={nascimento} disabled />
          </label>
        </div>

        <div className="btn-group-btn-down">
          <h2>Endereço de Residência</h2>

          <label>
            País
            <input type="text" value={pais} disabled />
          </label>

          <label>
            Estado
            <input type="text" value={estado} disabled />
          </label>

          <label>
            Cidade
            <input type="text" value={cidade} disabled />
          </label>

          <label>
            Bairro
            <input type="text" value={bairro} disabled />
          </label>

          <label>
            CEP
            <input type="text" value={cep} disabled />
          </label>

          <label>
            Endereço
            <input type="text" value={endereco} disabled />
          </label>

          <label>
            Número
            <input type="text" value={numero} disabled />
          </label>

          <label>
            Complemento
            <input type="text" value={complemento} disabled />
          </label>
          <button type="button" className="btn-salvar">
            Salvar
          </button>
          <button onClick={onEditProfile} className="edit-profile-button">
            Editar Perfil
          </button>
        </div>
      </form>
    </div>
  );
};

export default DadosPrestador;
