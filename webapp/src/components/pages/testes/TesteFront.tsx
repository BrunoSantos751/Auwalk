import { useState, useEffect } from "react";
import usersData from "../../data/users.json"; // importa o JSON local
import "./TesteFront.css";

interface User {
  id: number;
  nome: string;
  email: string;
}

export function TesteFront() {
  // Estado com todos os usuários
  const [users, setUsers] = useState<User[]>([]);
  const [search, setSearch] = useState("");
  const [newUser, setNewUser] = useState({ nome: "", email: "" });

  // Simula "buscar usuários" do backend (carrega do JSON)
  useEffect(() => {
    setUsers(usersData);
  }, []);

  // Filtra por ID ou Email
  const handleSearch = () => {
    if (!search.trim()) {
      setUsers(usersData);
      return;
    }

    const lowerSearch = search.toLowerCase();
    const filtered = usersData.filter(
      (u) =>
        u.id.toString() === lowerSearch ||
        u.email.toLowerCase().includes(lowerSearch)
    );
    setUsers(filtered);
  };

  // Adiciona novo usuário
  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();

    const nextId = users.length > 0 ? users[users.length - 1].id + 1 : 1;
    const newEntry: User = {
      id: nextId,
      nome: newUser.nome,
      email: newUser.email,
    };

    setUsers((prev) => [...prev, newEntry]); // adiciona à lista atual
    setNewUser({ nome: "", email: "" }); // limpa campos
  };

  return (
    <div className="user-page">
      <h1>Lista de Usuários</h1>
      {/*  Busca */}
      <div className="search">
        <input
          type="text"
          placeholder="Buscar por ID ou e-mail..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button className="teste-butt" onClick={handleSearch}>
          Buscar
        </button>
      </div>
      {/*  Tabela */}
      <div className="tabela-teste">
        <h2>Usuários Cadastrados</h2>
        <ul>
          {users.map((u) => (
            <li className="li-teste" key={u.id}>
              <strong>ID:</strong> {u.id} — <strong>Nome:</strong> {u.nome} —{" "}
              <strong>Email:</strong> {u.email} <hr />
            </li>
          ))}
        </ul>
      </div>

      {/*  Formulário */}
      <hr />
      <h2>Adicionar Novo Usuário</h2>
      <form onSubmit={handleRegister}>
        <div className="form-teste">
          <input
            type="text"
            placeholder="Nome"
            value={newUser.nome}
            onChange={(e) => setNewUser({ ...newUser, nome: e.target.value })}
            required
          />
          <input
            type="email"
            placeholder="Email"
            value={newUser.email}
            onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
            required
          />
        </div>
        <button className="teste-but" type="submit">
          Cadastrar
        </button>
      </form>
    </div>
  );
}
export default TesteFront;
