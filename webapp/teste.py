import requests
import json

# URL base da sua API.
# Altere para o endereço correto se não estiver rodando localmente na porta 8080.
BASE_URL = "http://auwalk.us-east-2.elasticbeanstalk.com"

def testar_rota_login():
    """
    Função que testa a rota de login com um caso de sucesso e um de falha.
    """
    print("--- Iniciando testes na API de autenticação ---")

    # --- CASO 1: Login com sucesso ---
    print("\n[TESTE 1] Tentando login com credenciais VÁLIDAS...")
    login_sucesso_payload = {
        "email": "usuario@exemplo.com",  # Use um email que exista no seu banco de dados
        "senha": "senha123"             # Use a senha correta para o email acima
    }

    try:
        # Faz a requisição POST para o endpoint /auth/login
        response_sucesso = requests.post(f"{BASE_URL}/auth/login", json=login_sucesso_payload)

        # Imprime o resultado
        print(f"Status Code: {response_sucesso.status_code}")
        print("Resposta JSON recebida:")
        # O .json() converte a resposta para um dicionário Python
        # O json.dumps formata a saída para ficar mais legível (pretty-print)
        print(json.dumps(response_sucesso.json(), indent=2))

    except requests.exceptions.RequestException as e:
        print(f"ERRO ao conectar na API: {e}")
        print("Verifique se a sua aplicação backend está rodando no endereço correto.")


    # --- CASO 2: Login com falha ---
    print("\n[TESTE 2] Tentando login com credenciais INVÁLIDAS...")
    login_falha_payload = {
        "email": "bob@email.com", # Email pode até ser válido
        "senha": "1234"         # Senha incorreta
    }

    try:
        # Faz a requisição POST
        response_falha = requests.post(f"{BASE_URL}/auth/login", json=login_falha_payload)

        # Imprime o resultado
        print(f"Status Code: {response_falha.status_code}")
        print("Resposta JSON recebida:")
        print(json.dumps(response_falha.json(), indent=2))

    except requests.exceptions.RequestException as e:
        print(f"ERRO ao conectar na API: {e}")


    print("\n--- Testes finalizados ---")


# --- Executa a função de teste ---
if __name__ == "__main__":
    # Antes de rodar, lembre-se de trocar os dados em 'login_sucesso_payload'
    # para um usuário e senha que realmente existam no seu banco de dados!
    testar_rota_login()