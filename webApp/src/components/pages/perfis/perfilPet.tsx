import React from "react";
import "./perfilPet.css";
import dogcatImage from "../../../assets/dogcat.webp";
import { useNavigate } from "react-router-dom";

const PerfilPet: React.FC = () => {
  const navigate = useNavigate();

  return (
    <>
      {/* Cabeçalho do perfil do pet */}
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

          {/* Ações do usuário */}
          <div className="pet-actions">
            <div className="pet-btn-group">
              <select className="pet-select">
                <option value="" disabled selected>
                  Meus Pets
                </option>
                <option value="1">Rex</option>
                <option value="2">Luna</option>
              </select>
              <button
                className="pet-btn"
                onClick={() => navigate("/perfilTutor")}
              >
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
