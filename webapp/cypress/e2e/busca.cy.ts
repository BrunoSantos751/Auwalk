describe('Funcionalidade de Busca', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  it('deve navegar para página de pesquisa ao submeter formulário', () => {
    cy.get('#servicoId').select('Passeio')
    cy.get('#enderecoId').type('Rua Teste, 123')
    
    // Obter data de amanhã
    const amanha = new Date()
    amanha.setDate(amanha.getDate() + 1)
    const dataFormatada = amanha.toISOString().split('T')[0]
    
    cy.get('#entradaId').type(dataFormatada)
    cy.get('button[type="submit"]').click()
    
    cy.url().should('include', '/search')
  })

  it('deve manter dados do formulário ao navegar para pesquisa', () => {
    cy.get('#servicoId').select('PetSitting')
    cy.get('#enderecoId').type('Avenida Principal, 456')
    
    const amanha = new Date()
    amanha.setDate(amanha.getDate() + 1)
    const dataFormatada = amanha.toISOString().split('T')[0]
    
    cy.get('#entradaId').type(dataFormatada)
    
    const amanhaMais1 = new Date()
    amanhaMais1.setDate(amanhaMais1.getDate() + 2)
    const dataSaidaFormatada = amanhaMais1.toISOString().split('T')[0]
    
    cy.get('#saidaId').type(dataSaidaFormatada)
    cy.get('button[type="submit"]').click()
    
    cy.url().should('include', '/search')
  })
})

