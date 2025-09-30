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
import PerfilTotal from "./components/pages/perfis/perfilTotal";
import PerfilPet from "./components/pages/perfis/perfilPet";
import Login from "./components/pages/login/login";
import Comofunciona from "./components/pages/footerPages/comofunciona";
import Footer from "./components/pages/footer/footer";

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
        <Route path="/perfil" element={<PerfilTotal />} />
        <Route path="/perfilpet" element={<PerfilPet />} />
        <Route path="/login" element={<Login />} />
        <Route path="/comofunciona" element={<Comofunciona />} />
      </Routes>
      <Footer />
    </BrowserRouter>
    </AuthProvider>
  </React.StrictMode>
);
