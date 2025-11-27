import fundoImage from "../../../assets/fundo1.webp";
import React, { useState } from "react";
import "./estiloCadastro.css";
import dogImage from "../../../assets/dog3.webp";

const Cadastro: React.FC = () => {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [telefone, setTelefone] = useState("");
  const [senha, setSenha] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const response = await fetch("https://api.auwalk.com.br/users/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ nome, email, telefone, senha }),
      });

      const data = await response.json();
      alert(data.message || "Erro ao cadastrar");
    } catch (error) {
      alert("Não foi possível conectar ao backend");
      console.error(error);
    }
  };

  return (
    <div className="register-wrapper">
      <h1>Cadastro</h1>
      <img src={fundoImage} alt="Fundo" className="background-img" />
      <div className="register-body">
        <div className="register-mask"></div>
        <img src={dogImage} alt="dog" className="register-img" />

        <form className="register-form" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Nome"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
          />
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <input
            type="tel"
            placeholder="Celular"
            value={telefone}
            onChange={(e) => setTelefone(e.target.value)}
          />
          <label className="password-label">
            <input
              type="password"
              placeholder="Senha"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
            <small>
              Sua nova senha precisa ter : Mínimo 8 caracteres, 1 letra
              maiúscula e 1 número
            </small>
          </label>
          <label className="password-label">
            <input type="password" placeholder="Senha Novamente" />
            <small>
              Finalize seu cadastro em
              <span className="password-palavra-destaque"> Meus Dados </span>
              para liberar todos
              <span className="password-palavra-destaque"> os recursos.</span>
            </small>
          </label>
          <label className="terms">
            <input type="checkbox" required />
            Concordo com os
            <span className="terms-palavra-destaque">
              Termos e Condições de uso
            </span>
          </label>
          <button type="submit">Cadastrar</button>
        </form>
      </div>
    </div>
  );
};

export default Cadastro;