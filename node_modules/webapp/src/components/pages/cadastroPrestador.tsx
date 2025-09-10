import React from "react";
import "./estiloCadastro.css";
import dogImage from "../../assets/dog3.webp";
import fundoImage from "../../assets/fundo1.webp";
const CadastroPrestador: React.FC = () => {
  return (
    <div className="register-wrapper">
      <h1>Cadastro Prestador</h1>
      <img src={fundoImage} alt="Fundo" className="background-img" />
      <div className="register-body">
        <img src={dogImage} alt="dog" className="register-img" />

        <form className="register-form">
          <input type="text" placeholder="Nome" />
          <input type="email" placeholder="Email" />
          <input type="tel" placeholder="Celular" />
          <label className="password-label">
            <input type="password" placeholder="Senha" />
            <small>
              Sua nova senha precisa ter : Mínimo 8 caracteres, 1 letra
              maiúscula e 1 número
            </small>
          </label>
          <label className="password-label">
            <input type="password" placeholder="Senha Novamente" />
            <small>
              Finalize seu cadastro em <strong>Meus Dados</strong> para liberar
              todos<strong> os recursos.</strong>
            </small>
          </label>
          <label className="terms">
            <input type="checkbox" required />
            Concordo com os <strong>Termos e Condições de uso</strong>
          </label>
          <button type="submit">Cadastrar</button>
        </form>
      </div>
    </div>
  );
};

export default CadastroPrestador;
