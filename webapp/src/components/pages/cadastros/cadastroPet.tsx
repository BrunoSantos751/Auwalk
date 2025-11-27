import React, { useState } from "react";
import "./estiloCadastro.css";
import dogImage from "../../../assets/dogcat.webp";
import fundoImage from "../../../assets/fundo1.webp";
import { useNavigate } from "react-router-dom";

// Interface para o estado do formulário (sem o campo 'sexo')
interface PetFormData {
  nome: string;
  idade: string;
  especie: string;
  raca: string;
  observacoes: string;
}

const CadastroPet: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<PetFormData>({
    nome: "",
    idade: "",
    especie: "",
    raca: "",
    observacoes: "",
  });

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const token = localStorage.getItem("authToken");
      if (!token) {
        alert("Erro de autenticação. Faça login novamente.");
        return;
      }
      const payload = JSON.parse(atob(token.split(".")[1]));
      const idUsuario = payload.sub;

      // Monta o objeto para o backend (agora com 'observacoes' como texto simples)
      const petParaEnviar = {
        idUsuario: idUsuario,
        nome: formData.nome,
        especie: formData.especie,
        raca: formData.raca,
        idade: parseInt(formData.idade, 10) || 0,
        observacoes: formData.observacoes, // Enviando o texto diretamente
      };

      const response = await fetch('http://auwalk.us-east-2.elasticbeanstalk.com/pets/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(petParaEnviar),
      });

      const result = await response.json();

      if (response.ok && result.success) {
        alert("Pet cadastrado com sucesso!");
        navigate("/perfilpet");
      } else {
        alert(`Erro ao cadastrar o pet: ${result.message}`);
      }
    } catch (error) {
      console.error("Erro ao cadastrar pet:", error);
      alert("Ocorreu um erro de conexão. Tente novamente.");
    }
  };

  return (
    <div className="register-wrapper">
      <img src={fundoImage} alt="Fundo" className="background-img" />
      <h1>Cadastro Pet</h1>
      <div className="register-body">
        <img
          src={dogImage}
          alt="dog e gato"
          className="register-img dogcat-img"
        />

        <form className="register-form-pet" onSubmit={handleSubmit}>
          <input
            type="text"
            name="nome"
            placeholder="Nome do Pet"
            value={formData.nome}
            onChange={handleChange}
            required
          />
          <input
            type="number"
            name="idade"
            placeholder="Idade"
            value={formData.idade}
            onChange={handleChange}
            required
          />
           <input
            type="text"
            name="raca"
            placeholder="Raça (Ex: SRD, Poodle)"
            value={formData.raca}
            onChange={handleChange}
            required
          />
          <select
            className="pet-select-op"
            name="especie"
            value={formData.especie}
            onChange={handleChange}
            required
          >
            <option value="" disabled>
              Selecione a espécie
            </option>
            <option value="cachorro">Cachorro</option>
            <option value="gato">Gato</option>
            <option value="ave">Ave</option>
            <option value="outro">Outro</option>
          </select>
          <input
            name="observacoes"
            placeholder="Observações (sexo, alergias, etc.)"
            value={formData.observacoes}
            onChange={handleChange}
          />

          <button type="submit">Cadastrar Pet</button>
        </form>
      </div>
    </div>
  );
};

export default CadastroPet;