import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AuthProvider } from './context/AuthContext';
import Home from "./components/pages/home/Home";
import Header from "./components/pages/header/header";
import CadastroPet from "./components/pages/cadastros/cadastroPet";
import Cadastro from "./components/pages/cadastros/cadastro";
import DadosTotal from "./components/pages/dados/dadosTotal";
import DadosPet from "./components/pages/dados/dadosPets";
import PrestadoPag from "./components/pages/prestador/pagPrestador";
import UpgradePrestador from "./components/pages/upPrestador/upgradePrestador";
import PerfilTotal from "./components/pages/perfis/perfilTotal";
import PerfilPet from "./components/pages/perfis/perfilPet";
import Login from "./components/pages/login/login";
import ComoFunciona from "./components/pages/footerPages/comofunciona";
import OndeEstamos from "./components/pages/footerPages/ondeEstamos";
import Termos from "./components/pages/footerPages/termos";
import Ajuda from "./components/pages/footerPages/ajuda";
import Footer from "./components/pages/footer/footer";
import Pesquisa from "./components/pages/pesquisa/Pesquisa";
import Historico from "./components/pages/historico/historico";

const rootElement = document.getElementById("root");
if (!rootElement) throw new Error("Root element not found");

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <AuthProvider>
      <BrowserRouter>
        <Header />
        <Routes>
          <Route path="/" element={<Home />}></Route>
          <Route path="/cadastro" element={<Cadastro />} />
          <Route path="/cadastropet" element={<CadastroPet />} />
          <Route path="/dados" element={<DadosTotal />} />
          <Route path="/dadospets/:petId" element={<DadosPet />} />
          <Route path="/pagprestador" element={<PrestadoPag />} />
          <Route path="/upgradeprestador" element={<UpgradePrestador />} />
          <Route path="/perfil" element={<PerfilTotal />} />
          <Route path="/perfilpet" element={<PerfilPet />} />
          <Route path="/login" element={<Login />} />
          <Route path="/comofunciona" element={<ComoFunciona />} />
          <Route path="/ondeEstamos" element={<OndeEstamos />} />
          <Route path="/ajuda" element={<Ajuda />} />
          <Route path="/termosdeprivacidade" element={<Termos />} />
          <Route path="/search" element={<Pesquisa />} />
          <Route path="/historico" element={<Historico />} />
        </Routes>
        <Footer />
      </BrowserRouter>
    </AuthProvider>
  </React.StrictMode>
);
