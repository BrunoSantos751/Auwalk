import React, { useState } from "react";
import "./estiloCadastro.css";

import dogImage from "../../assets/dog3.webp";

const CadastroTutor: React.FC = () => {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [telefone, setTelefone] = useState("");
  const [senha, setSenha] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const response = await fetch("http://localhost:8080/users/register", {
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
      <h1>Cadastro Tutor</h1>
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

export default CadastroTutor;
