describe('Página de Login', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('deve carregar a página de login corretamente', () => {
    cy.contains('AuWalk').should('be.visible')
    cy.contains('Faça o login agora mesmo').should('be.visible')
    cy.contains('Como deseja continuar?').should('be.visible')
  })

  it('deve exibir botão de login com Google', () => {
    cy.contains('Fazer login com o google').should('be.visible')
  })

  it('deve exibir formulário de login', () => {
    cy.get('input[type="email"]').should('be.visible')
    cy.get('input[type="password"]').should('be.visible')
    cy.get('button[type="submit"]').should('be.visible')
    cy.contains('ENTRAR').should('be.visible')
  })

  it('deve permitir preencher email e senha', () => {
    cy.get('input[type="email"]').type('teste@example.com')
    cy.get('input[type="password"]').type('senha123')
    
    cy.get('input[type="email"]').should('have.value', 'teste@example.com')
    cy.get('input[type="password"]').should('have.value', 'senha123')
  })

  it('deve validar campos obrigatórios', () => {
    cy.get('button[type="submit"]').click()
    // O navegador deve mostrar validação HTML5
    cy.get('input[type="email"]').should('have.attr', 'required')
    cy.get('input[type="password"]').should('have.attr', 'required')
  })

  it('deve exibir erro ao tentar login com credenciais inválidas', () => {
    // Mock da resposta de erro da API
    cy.intercept('POST', 'https://api.auwalk.com.br/auth/login', {
      statusCode: 401,
      body: { success: false }
    }).as('loginRequest')

    cy.get('input[type="email"]').type('email@invalido.com')
    cy.get('input[type="password"]').type('senhaerrada')
    cy.get('button[type="submit"]').click()

    cy.wait('@loginRequest')
    cy.contains('Email ou senha inválidos').should('be.visible')
  })
})

