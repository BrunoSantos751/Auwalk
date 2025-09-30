import React from "react";
import "./pagPrestador.css";
import { useNavigate } from "react-router-dom";
import ImgCorrendo from "../../../assets/correndo1.webp";
import fundoImage from "../../../assets/fundo1.webp";
import fundoImageMobile from "../../../assets/correndo2.webp";

const PrestadoPag: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div className="app">
      <h1>Passeador ou Pet sitter?</h1>
      <img src={fundoImage} alt="Fundo" className="fundo" />

      <div className="conteudo">
        <h2>
          "Transforme seu amor por animais em uma oportunidade única!” Seja
          <strong> pet sitter</strong> ou <strong>passeador</strong> e descubra
          a alegria de cuidar e brincar com nossos melhores amigos de quatro
          patas.
        </h2>
        <div className="h3-container">
          <h3>O primeiro passo é simples →</h3>
          <button onClick={() => navigate("/cadastro")}>cadastre-se</button>
        </div>
      </div>
      <div className="imagem-final">
        <picture>
          <source srcSet={fundoImageMobile} media="(max-width: 480px)" />
          <img src={ImgCorrendo} alt="correndo" />
        </picture>
      </div>
    </div>
  );
};
export default PrestadoPag;
