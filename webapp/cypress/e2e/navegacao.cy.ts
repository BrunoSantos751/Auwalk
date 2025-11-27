describe('Navegação Básica', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  it('deve carregar a página inicial', () => {
    cy.contains('AuWalk').should('be.visible')
    cy.contains('Sem tempo para o').should('be.visible')
  })

  it('deve navegar para a página de cadastro', () => {
    cy.contains('Cadastrar').click()
    cy.url().should('include', '/cadastro')
  })

  it('deve navegar para "Quero ser prestador"', () => {
    cy.contains('Quero ser prestador').click()
    cy.url().should('include', '/pagprestador')
  })

  it('deve navegar para a página de ajuda', () => {
    cy.contains('Ajuda').click()
    cy.url().should('include', '/ajuda')
  })

  it('deve retornar para home clicando no logo', () => {
    cy.visit('/login')
    cy.contains('AuWalk').first().click()
    cy.url().should('eq', Cypress.config().baseUrl + '/')
  })
})

