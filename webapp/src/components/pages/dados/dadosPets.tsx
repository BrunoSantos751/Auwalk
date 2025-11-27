import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import "./dadosPet.css";
import avatarExemplo from "../../../assets/dog3.webp";

interface PetBackend {
  id_pet: number;
  nome: string;
  especie: string;
  raca: string;
  idade: number;
  observacoes: string | null;
}
interface PetFormData {
  nome: string;
  idade: string;
  especie: string;
  raca: string;
  observacoes: string;
}


const DadosPet: React.FC = () => {
  const { petId } = useParams<{ petId: string }>();

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<PetFormData>({
    nome: "",
    idade: "",
    especie: "",
    raca: "",
    observacoes: "",
  });

  useEffect(() => {
    if (!petId) return;

    const fetchPetData = async () => {
      try {
        const response = await fetch(`http://auwalk.us-east-2.elasticbeanstalk.com/pets/${petId}`);
        const result = await response.json();

        if (result.success) {
          const pet: PetBackend = result.data;
          setFormData({
            nome: pet.nome || "",
            idade: pet.idade.toString() || "",
            especie: pet.especie || "",
            raca: pet.raca || "",
            observacoes: pet.observacoes || "",
          });
        }
      } catch (error) {
        console.error("Erro ao buscar dados do pet:", error);
      }
    };

    fetchPetData();
  }, [petId]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    if (!petId) return;

    const petDataParaEnviar = {
      ...formData,
      idade: parseInt(formData.idade, 10) || 0,
    };

    try {
      // URL ALTERADA AQUI
      const response = await fetch(`http://auwalk.us-east-2.elasticbeanstalk.com/pets/update/${petId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(petDataParaEnviar),
      });

      const result = await response.json();

      if (response.ok && result.success) {
        alert("Pet atualizado com sucesso!");
        setIsEditing(false);
      } else {
        alert(`Erro ao atualizar o pet: ${result.message}`);
      }
    } catch (error) {
      console.error("Erro de rede ao salvar dados do pet:", error);
      alert("Erro de conexão ao salvar. Verifique o console.");
    }
  };

  return (
    // O JSX (layout do formulário) não precisa de nenhuma alteração
    <div className="dados-pet-container">
      <header className="dados-pet-header">
        <h2>
          <span className="palavra-destaque-dados">Perfil do seu pet</span>
        </h2>
        <img
          src={avatarExemplo}
          alt={`${formData.nome} avatar`}
          className="dados-pet-avatar"
        />
      </header>
      <form className="dados-pet-form" onSubmit={(e) => e.preventDefault()}>
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
            Espécie (Tipo)
            <select
              className="select-op"
              name="especie"
              value={formData.especie}
              onChange={handleChange}
              disabled={!isEditing}
            >
              <option value="">Selecione</option>
              <option value="cachorro">Cachorro</option>
              <option value="gato">Gato</option>
              <option value="ave">Ave</option>
              <option value="outro">Outro</option>
            </select>
          </label>
          <label>
            Raça
            <input
              type="text"
              name="raca"
              value={formData.raca}
              onChange={handleChange}
              placeholder="Ex.: SRD"
              disabled={!isEditing}
            />
          </label>
          <label>
            Observações
            <input
              name="observacoes"
              value={formData.observacoes}
              onChange={handleChange}
              placeholder="Alergias, medicamentos, cuidados especiais..."
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