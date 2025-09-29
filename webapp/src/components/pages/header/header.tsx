import React, { useState } from "react";
import { Link } from "react-router-dom";
import { PiUserBold } from "react-icons/pi";
import "./header.css";

const Header: React.FC = () => {
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

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
                onClick={() => setIsLoggedIn(false)}
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
