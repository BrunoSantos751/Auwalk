import React, { useState, useEffect } from "react";

import "./dadosTotal.css";
import avatarExemplo from "../../../assets/dog3.webp";

interface DadosTotalProps {
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
  onEditProfile?: (data: any) => void;
}

const DadosTotal: React.FC<DadosTotalProps> = ({
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
  //  Estado para edição
  const [isEditing, setIsEditing] = useState(false);

  //  Estado do formulário (inicia com valores das props)
  const [formData, setFormData] = useState({
    name,
    email,
    cpf,
    nascimento,
    celular,
    pais,
    estado,
    bairro,
    cep,
    cidade,
    endereco,
    numero,
    complemento,
  });

  //  Atualizar estado ao digitar

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({ ...FormData, [name]: value });
  };
  //
  const handleSave = () => {
    setIsEditing(false);
    if (onEditProfile) {
      onEditProfile(formData);
    }
    console.log("Dados salvos:", formData);
  };

  return (
    <div className="dados-prestador-container">
      <header className="dados-prestador-header">
        <h2>
          <span className="palavra-destaque-dados">Primeira vez aqui?</span>
          Complete seu cadastro e aproveite todos os recursos.
        </h2>
        <img
          src={avatarUrl || avatarExemplo}
          alt={`${formData.name} avatar`}
          className="dados-prestador-avatar"
        />
      </header>

      <form className="dados-prestador-form">
        <div className="dados-prestador-group dados-prestador-group-up">
          <label>
            Nome completo
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            CPF
            <input
              type="text"
              name="cpf"
              value={formData.cpf}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            E-mail
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Celular
            <input
              type="tel"
              name="celular"
              value={formData.celular}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Nascimento
            <input
              type="date"
              name="nascimento"
              onChange={handleChange}
              value={formData.nascimento}
              disabled={!isEditing}
            />
          </label>
        </div>
        <div className="dados-h2">
          <h2>Endereço de Residência</h2>
        </div>
        <div className="dados-prestador-group dados-prestador-group-down">
          <label>
            País
            <input
              type="text"
              name="pais"
              value={formData.pais}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Estado
            <input
              type="text"
              name="estado"
              value={formData.estado}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Cidade
            <input
              type="text"
              name="cidade"
              value={formData.cidade}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Bairro
            <input
              type="text"
              name="bairro"
              value={formData.bairro}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            CEP
            <input
              type="text"
              name="cep"
              value={formData.cep}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Endereço
            <input
              type="text"
              name="endereco"
              value={formData.endereco}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Número
            <input
              type="text"
              name="numero"
              value={formData.numero}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Complemento
            <input
              type="text"
              name="complemento"
              value={formData.complemento}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          {isEditing ? (
            <button
              type="button"
              onClick={handleSave}
              className="dados-prestador-btn-salvar"
            >
              Salvar
            </button>
          ) : (
            <button
              type="button"
              onClick={() => setIsEditing(true)}
              className="dados-prestador-edit-profile-btn"
            >
              Editar Perfil
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default DadosTotal;
