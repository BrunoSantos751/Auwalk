describe('Página Home - Funcionalidades', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  it('deve exibir o formulário de busca', () => {
    cy.get('form.formBusca').should('be.visible')
    cy.get('#servicoId').should('be.visible')
    cy.get('#enderecoId').should('be.visible')
    cy.get('#entradaId').should('be.visible')
    cy.get('button[type="submit"]').should('be.visible')
  })

  it('deve permitir selecionar tipo de serviço', () => {
    cy.get('#servicoId').select('Passeio')
    cy.get('#servicoId').should('have.value', 'Passeio')
    
    cy.get('#servicoId').select('PetSitting')
    cy.get('#servicoId').should('have.value', 'PetSitting')
  })

  it('deve habilitar campo de saída apenas para Pet Sitter', () => {
    // Inicialmente, campo de saída deve estar desabilitado
    cy.get('#saidaId').should('be.disabled')
    
    // Ao selecionar PetSitting, deve habilitar
    cy.get('#servicoId').select('PetSitting')
    cy.get('#saidaId').should('not.be.disabled')
    
    // Ao selecionar Passeio, deve desabilitar novamente
    cy.get('#servicoId').select('Passeio')
    cy.get('#saidaId').should('be.disabled')
  })

  it('deve permitir preencher endereço', () => {
    cy.get('#enderecoId').type('Rua Teste, 123')
    cy.get('#enderecoId').should('have.value', 'Rua Teste, 123')
  })

  it('deve permitir selecionar data de entrada', () => {
    const hoje = new Date().toISOString().split('T')[0]
    cy.get('#entradaId').type(hoje)
    cy.get('#entradaId').should('have.value', hoje)
  })

  it('deve exibir seção de prestadores bem avaliados', () => {
    cy.contains('Os mais bem avaliados').should('be.visible')
    cy.contains('Laura Costa').should('be.visible')
    cy.contains('Maria Layanne').should('be.visible')
  })

  it('deve exibir informações sobre o serviço', () => {
    cy.contains('Seu pet merece mais atenção e cuidado!').should('be.visible')
    cy.contains('Auwalk').should('be.visible')
  })
})

