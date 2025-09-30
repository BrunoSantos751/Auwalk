//import React from 'react';
import "./login.css"; // Importa nosso arquivo de estilos
import google from "../../../assets/google.png";
import dog3 from "../../../assets/dog3.webp";

const Login = () => {
  return (
    <div className="login-page">
      {/* Coluna da Esquerda (com o cachorro) */}
      <div className="left-side">
        <div className="dog-image">
          {<img src={dog3} alt="Cachorro AuWalk" className="dog-image" />}
        </div>
      </div>

      {/* Coluna da Direita (com o formulário) */}
      <div className="right-side">
        <div className="login-form-container">
          <h1>AuWalk</h1>
          <h2>Faça o login agora mesmo</h2>
          <p>Como deseja continuar?</p>

          <button className="google-login-btn">
            {<img src={google} alt="Google" />}
            Fazer login com o google
          </button>

          <div className="divider">
            <span>ou</span>
          </div>

          <form>
            <input type="email" placeholder="E-mail" />
            <input type="password" placeholder="Senha" />
            <button type="submit" className="login-btn">
              ENTRAR
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
