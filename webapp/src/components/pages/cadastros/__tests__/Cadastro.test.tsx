/// <reference types="vitest" />
import { describe, test, expect, beforeEach, beforeAll, vi } from 'vitest';
import { render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import Cadastro from '../cadastro';

// Mock do fetch
const mockedFetch: vi.Mock = vi.fn(() =>
  Promise.resolve({
    json: () => Promise.resolve({ message: 'Cadastro realizado' }),
  })
);

beforeAll(() => {
  (globalThis as any).fetch = mockedFetch;
});

describe('<Cadastro />', () => {
  beforeEach(() => {
    mockedFetch.mockClear();
  });

  test('Deve preencher os campos do formulário', async () => {
    const { getByPlaceholderText, getAllByPlaceholderText } = render(<Cadastro />);

    const nomeInput = getByPlaceholderText('Nome') as HTMLInputElement;
    const emailInput = getByPlaceholderText('Email') as HTMLInputElement;
    const telefoneInput = getByPlaceholderText('Celular') as HTMLInputElement;
    const senhaInput = getAllByPlaceholderText('Senha')[0] as HTMLInputElement;

    await userEvent.type(nomeInput, 'Teste Nome');
    await userEvent.type(emailInput, 'teste@email.com');
    await userEvent.type(telefoneInput, '999999999');
    await userEvent.type(senhaInput, 'Senha123');

    expect(nomeInput.value).toBe('Teste Nome');
    expect(emailInput.value).toBe('teste@email.com');
    expect(telefoneInput.value).toBe('999999999');
    expect(senhaInput.value).toBe('Senha123');
  });

  test('Deve chamar a API ao submeter o formulário', async () => {
    const { getByPlaceholderText, getAllByPlaceholderText, getByRole } = render(<Cadastro />);

    await userEvent.type(getByPlaceholderText('Nome'), 'Teste Nome');
    await userEvent.type(getByPlaceholderText('Email'), 'teste@email.com');
    await userEvent.type(getByPlaceholderText('Celular'), '999999999');
    await userEvent.type(getAllByPlaceholderText('Senha')[0], 'Senha123');

    await userEvent.click(getByRole('button', { name: /Cadastrar/i }));

    // espera o mock do fetch ser chamado
    await new Promise((resolve) => setTimeout(resolve, 0));

    expect(mockedFetch).toHaveBeenCalledWith(
      'http://localhost:8080/users/register',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nome: 'Teste Nome',
          email: 'teste@email.com',
          telefone: '999999999',
          senha: 'Senha123',
        }),
      })
    );
  });
});
