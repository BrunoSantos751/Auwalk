import React from "react";
import "./estiloCadastro.css";
import dogImage from "../../../assets/dogcat.webp";
import fundoImage from "../../../assets/fundo1.webp";

const CadastroPet: React.FC = () => {
  return (
    <div className="register-wrapper">
      <img src={fundoImage} alt="Fundo" className="background-img" />
      <h1>Cadastro Pet</h1>
      <div className="register-body">
        <img
          src={dogImage}
          alt="dog e gato"
          className="register-img dogcat-img"
        />

        <form className="register-form-pet">
          <input type="text" placeholder="Nome" />
          <input type="idade" placeholder="idade" />
          <input type="sexo" placeholder="Macho ou Fêmea?" />
          <select className="pet-select-op" name="pet" id="pet">
            <option value="" disabled selected>
              Selecione seu pet
            </option>
            <option value="cachorro">cachorro</option>
            <option value="gato">gato</option>
            <option value="outro"> + outro</option>
          </select>
          <button type="submit"> Cadastrar</button>
          <button type="submit"> Cadastrar outro pet</button>
        </form>
      </div>
    </div>
  );
};

export default CadastroPet;
