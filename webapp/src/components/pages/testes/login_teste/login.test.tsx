// npx jest src/components/pages/testes/login_teste/login.test.tsx

/*
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import Login from "../../login/login";
import { AuthContext } from "../../../../context/AuthContext";
import { MemoryRouter } from "react-router-dom";

const mockNavigate = jest.fn();

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
}));

const mockLogin = jest.fn();

describe("Login Page", () => {
  test("renderiza formulário e realiza login com sucesso", async () => {
    globalThis.fetch = jest.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ success: true, token: "fake-token" }),
      })
    ) as jest.Mock;

    render(
      <AuthContext.Provider
        value={{ token: "", login: mockLogin, logout: jest.fn() }}
      >
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </AuthContext.Provider>
    );

    fireEvent.change(screen.getByPlaceholderText(/E-mail/i), {
      target: { value: "teste@teste.com" },
    });
    fireEvent.change(screen.getByPlaceholderText(/Senha/i), {
      target: { value: "123456" },
    });
    fireEvent.click(screen.getByRole("button", { name: /entrar/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith("fake-token");
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });
});
*/
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import Login from "../../login/login";
import { AuthContext } from "../../../../context/AuthContext";
import { MemoryRouter } from "react-router-dom";

const mockNavigate = jest.fn();

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
}));

const mockLogin = jest.fn();

describe("Login Page", () => {
  beforeEach(() => {
    jest.clearAllMocks(); // limpa os mocks entre os testes
  });

  test("renderiza formulário e realiza login com sucesso", async () => {
    globalThis.fetch = jest.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ success: true, token: "fake-token" }),
      })
    ) as jest.Mock;

    render(
      <AuthContext.Provider
        value={{ token: "", login: mockLogin, logout: jest.fn() }}
      >
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </AuthContext.Provider>
    );

    fireEvent.change(screen.getByPlaceholderText(/E-mail/i), {
      target: { value: "teste@teste.com" },
    });
    fireEvent.change(screen.getByPlaceholderText(/Senha/i), {
      target: { value: "123456" },
    });
    fireEvent.click(screen.getByRole("button", { name: /entrar/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith("fake-token");
      expect(mockNavigate).toHaveBeenCalledWith("/");
    });
  });

  // 🧨 NOVO TESTE: login com erro
  test("exibe mensagem de erro quando o login falha", async () => {
    globalThis.fetch = jest.fn(() =>
      Promise.resolve({
        ok: false, // simula erro no servidor
        json: () =>
          Promise.resolve({ success: false, message: "Credenciais inválidas" }),
      })
    ) as jest.Mock;

    render(
      <AuthContext.Provider
        value={{ token: "", login: mockLogin, logout: jest.fn() }}
      >
        <MemoryRouter>
          <Login />
        </MemoryRouter>
      </AuthContext.Provider>
    );

    fireEvent.change(screen.getByPlaceholderText(/E-mail/i), {
      target: { value: "erro@teste.com" },
    });
    fireEvent.change(screen.getByPlaceholderText(/Senha/i), {
      target: { value: "senhaerrada" },
    });
    fireEvent.click(screen.getByRole("button", { name: /entrar/i }));

    // Espera o componente reagir ao erro
    await waitFor(() => {
      expect(screen.getByText(/credenciais inválidas/i)).toBeInTheDocument();
      expect(mockLogin).not.toHaveBeenCalled();
      expect(mockNavigate).not.toHaveBeenCalled();
    });
  });
});
