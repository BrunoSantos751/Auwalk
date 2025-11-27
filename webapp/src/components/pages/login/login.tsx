import React, { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../../../context/AuthContext";
import google from '../../../assets/google.png';
import dog3 from "../../../assets/dog3.webp";
import "./login.css";

const Login: React.FC = () => {
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    try {
      const response = await fetch("https://auwalk-redirect.santosmoraes79.workers.dev/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, senha }),
      });

      if (!response.ok) {
        setError("Email ou senha inválidos");
        return;
      }

      const data = await response.json();

      if (data.success && data.token) {
        login(data.token); // atualiza contexto global
        navigate("/"); // redireciona para Home
      } else {
        setError("Email ou senha inválidos");
      }
    } catch (err) {
      console.error(err);
      setError("Erro de conexão");
    }
  };

  return (
    <div className="login-page">
      <div className="left-side">
        <img src={dog3} alt="Cachorro AuWalk" className="dog-image" />
      </div>

      <div className="right-side">
        <div className="login-form-container">
          <h1>AuWalk</h1>
          <h2>Faça o login agora mesmo</h2>
          <p>Como deseja continuar?</p>

          <button className="google-login-btn">
            <img src={google} alt="Google" /> Fazer login com o google
          </button>

          <div className="divider"><span>ou</span></div>

          <form onSubmit={handleLogin}>
            <input
              type="email"
              placeholder="E-mail"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder="Senha"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              required
            />
            <button type="submit" className="login-btn">ENTRAR</button>
          </form>

          {error && <p style={{ color: "red", marginTop: "10px" }}>{error}</p>}
        </div>
      </div>
    </div>
  );
};

export default Login;
