import React from "react";
import { Link } from "react-router-dom";
import "./footer.css";

const Footer: React.FC = () => {
  const currentYear: number = new Date().getFullYear();
  return (
    <footer className="footer">
      <div className="footer-container">
        {/* Coluna 1 */}
        <div className="footer-column">
          <h4>Sobre nós</h4>
          <ul>
            <li>
              <Link to="/Home">Como Funciona</Link>
            </li>
            <li>
              <Link to="/Home">Onde Estamos</Link>
            </li>
          </ul>
        </div>

        {/* Coluna 2 */}
        <div className="footer-column">
          <h4>Serviços</h4>
          <ul>
            <li>
              <Link to="/">Passeios</Link>
            </li>
            <li>
              <Link to="/">Pet Sitter</Link>
            </li>
          </ul>
        </div>

        {/* Coluna 3 */}
        <div className="footer-column">
          <h4>Central de Ajuda</h4>
          <ul>
            <li>
              <Link to="/">Ajuda</Link>
            </li>
            <li>
              <Link to="/">Termos de Privacidade</Link>
            </li>
          </ul>
        </div>
      </div>
      {/* Copyright */}
      <div className="footer-copy">
        &copy; {currentYear} AuWalk. Todos os direitos reservados.
      </div>
    </footer>
  );
};
export default Footer;
