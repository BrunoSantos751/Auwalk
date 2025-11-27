import React, { useState, useEffect } from "react";
import "./perfilPet.css";
import dogcatImage from "../../../assets/dogcat.webp";
import { useNavigate } from "react-router-dom";

// Interface para tipar os dados do pet
interface Pet {
  id_pet: number;
  nome: string;
}

const PerfilPet: React.FC = () => {
  const navigate = useNavigate();
  const [pets, setPets] = useState<Pet[]>([]);

  // Efeito para buscar os pets do usuário ao carregar a página
  useEffect(() => {
    const fetchUserPets = async () => {
      try {
        const token = localStorage.getItem("authToken");
        if (!token) return;

        const payload = JSON.parse(atob(token.split(".")[1]));
        const idUsuario = payload.sub;

        if (!idUsuario) return;

        const response = await fetch(`http://auwalk.us-east-2.elasticbeanstalk.com/pets?idUsuario=${idUsuario}`);
        const result = await response.json();

        if (result.success) {
          setPets(result.data);
        }
      } catch (error) {
        console.error("Erro ao buscar a lista de pets:", error);
      }
    };

    fetchUserPets();
  }, []);

  // Função chamada ao selecionar um pet no dropdown
  const handlePetSelect = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const petId = e.target.value;
    if (petId) {
      // Navega para a tela de detalhes, passando o ID do pet na URL
      navigate(`/dadospets/${petId}`);
    }
  };

  return (
    <>
      <div className="pet-container">
        <header className="pet-header">
          <div className="pet-header-top">
            <img
              src={dogcatImage}
              alt="Cachorro e gato"
              className="pet-avatar"
            />
            <div className="pet-name">
              <h1>Perfil Pets</h1>
            </div>
          </div>

          <div className="pet-actions">
            <div className="pet-btn-group">
              <select
                className="pet-select"
                onChange={handlePetSelect}
                defaultValue=""
              >
                <option value="" disabled>
                  Meus Pets
                </option>
                {/* Popula o dropdown com os pets buscados */}
                {pets.map((pet) => (
                  <option key={pet.id_pet} value={pet.id_pet}>
                    {pet.nome}
                  </option>
                ))}
              </select>

              <button
                className="pet-btn"
                onClick={() => navigate("/cadastropet")}
              >
                Cadastrar Pet
              </button>
              <button className="pet-btn" onClick={() => navigate("/perfil")}>
                Voltar ao Perfil
              </button>
            </div>
          </div>
        </header>
      </div>
    </>
  );
};

export default PerfilPet;