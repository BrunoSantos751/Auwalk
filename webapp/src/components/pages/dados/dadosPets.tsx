import React, { useState } from "react";
import "./dadosPet.css";
import avatarExemplo from "../../../assets/dog3.webp";

interface DadosPetProps {
  nome?: string;
  idade?: string;
  tipo?: string;
  sexo?: string;
  temperamento?: string;
  gostos?: string;
  necessidades?: string;
  avatarUrl?: string;
  onEditPet?: (data: any) => void;
}

const DadosPet: React.FC<DadosPetProps> = ({
  nome = "",
  idade = "",
  tipo = "",
  sexo = "",
  temperamento = "",
  gostos = "",
  necessidades = "",
  onEditPet,
  avatarUrl,
}) => {
  const [isEditing, setIsEditing] = useState(false);

  const [formData, setFormData] = useState({
    nome,
    idade,
    tipo,
    sexo,
    temperamento,
    gostos,
    necessidades,
  });

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = () => {
    setIsEditing(false);
    if (onEditPet) onEditPet(formData);
    console.log("Dados do pet salvos:", formData);
  };

  return (
    <div className="dados-pet-container">
      <header className="dados-pet-header">
        <h2>
          <span className="palavra-destaque-dados">Cadastre seu pet</span>
          para aproveitar todos os recursos.
        </h2>
        <img
          src={avatarUrl || avatarExemplo}
          alt={`${formData.name} avatar`}
          className="dados-pet-avatar"
        />
      </header>

      <form className="dados-pet-form">
        <div className="dados-pet-group ">
          <label>
            Nome do Pet
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={handleChange}
              placeholder="Ex.: Rex"
              disabled={!isEditing}
            />
          </label>

          <label>
            Idade
            <input
              type="text"
              name="idade"
              value={formData.idade}
              onChange={handleChange}
              placeholder="Ex.: 2 anos"
              disabled={!isEditing}
            />
          </label>
          <label>
            Gostos
            <input
              type="text"
              name="gostos"
              value={formData.gostos}
              onChange={handleChange}
              placeholder="Brincadeiras favoritas, Passeios, Comidas"
              disabled={!isEditing}
            />
          </label>

          <label>
            Sexo
            <select
              className="select-op"
              name="sexo"
              value={formData.sexo}
              onChange={handleChange}
              disabled={!isEditing}
            >
              <option value="">Selecione</option>
              <option value="Macho">Macho</option>
              <option value="Fêmea">Fêmea</option>
            </select>
          </label>

          <label>
            Temperamento
            <input
              type="text"
              name="temperamento"
              value={formData.temperamento}
              onChange={handleChange}
              placeholder="Calmo, Energético, Tímido"
              disabled={!isEditing}
            />
          </label>
          <label>
            Tipo
            <select
              className="select-op"
              name="tipo"
              value={formData.tipo}
              onChange={handleChange}
              disabled={!isEditing}
            >
              <option value="">Selecione</option>
              <option value="Cachorro">Cachorro</option>
              <option value="Gato">Gato</option>
              <option value="Outros">Outros</option>
            </select>
          </label>

          <label>
            Necessidades especiais
            <input
              type="text"
              name="necessidades"
              value={formData.necessidades}
              onChange={handleChange}
              placeholder="Remédios, Cuidados extras"
              disabled={!isEditing}
            />
          </label>

          {isEditing ? (
            <button type="button" onClick={handleSave} className="pet-salvar">
              Salvar
            </button>
          ) : (
            <button
              type="button"
              onClick={() => setIsEditing(true)}
              className="pet-edit"
            >
              Editar Perfil
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default DadosPet;
