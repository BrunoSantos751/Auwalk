import React, { useContext } from "react"; // Importe o useContext
import { Link, useNavigate } from "react-router-dom";
import { PiUserBold } from "react-icons/pi";
import { AuthContext } from "../../../context/AuthContext"; // Importe o seu AuthContext
import "./header.css";

const Header: React.FC = () => {
  // Consuma o contexto para obter o token e a função de logout
  const { token, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  // A variável isLoggedIn agora é derivada diretamente da existência do token
  const isLoggedIn = !!token;

  const handleLogout = () => {
    logout(); // Chama a função de logout do contexto
    navigate("/login"); // Redireciona o usuário para a página de login
  };

  return (
    <header className="header">
      <nav className="navbar">
        <div className="navbar-left">
          <h1>AuWalk</h1>
          <ul>
            {!isLoggedIn ? (
              <>
                <li>
                  <Link to="/pagprestador">Quero ser prestador</Link>
                </li>
                <li>
                  <Link to="/ajuda">Ajuda</Link>
                </li>
              </>
            ) : (
              <>
                <li>
                  <Link to="/Home">Início</Link>
                </li>
                <li>
                  <Link to="/passeiopesquisa">Passeio</Link>
                </li>
                <li>
                  <Link to="/petsitterpesquisa">Pet Sitter</Link>
                </li>
                <li>
                  <Link to="/faleConosco">Fale Conosco</Link>
                </li>
              </>
            )}
          </ul>
        </div>

        <div className="navbar-right">
          {/* Botões diferentes dependendo do estado de login */}
          {!isLoggedIn ? (
            <>
              <Link to="/cadastro">
                <button className="navbar-button-estilo">Cadastrar</button>
              </Link>
              <Link to="/Login">
                <button className="navbar-button">Login</button>
              </Link>
            </>
          ) : (
            <>
              <Link to="/perfil">
                <button className="navbar-button-estilo">
                  Perfil <PiUserBold />
                </button>
              </Link>
              <button
                className="navbar-button"
                onClick={handleLogout} // Use a função handleLogout
              >
                Logout
              </button>
            </>
          )}
        </div>
      </nav>
    </header>
  );
};

export default Header;