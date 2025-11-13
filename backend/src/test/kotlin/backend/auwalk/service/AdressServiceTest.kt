package backend.auwalk.service

import org.assertj.core.api.Assertions.assertThat // O "expect" do AssertJ
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.* // Importa 'whenever', 'any', 'eq', 'verify'
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper

// Diz ao JUnit 5 para usar a extensão do Mockito
@ExtendWith(MockitoExtension::class)
class EnderecoServiceTest {

    // Cria um mock do JdbcTemplate (como jest.mock('JdbcTemplate'))
    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    // Cria uma instância do EnderecoService e injeta os mocks (como o 'jdbcTemplate') nela
    @InjectMocks
    private lateinit var enderecoService: EnderecoService

    @Test
    fun `deve criar um endereco com sucesso`() {
        // 1. Arrange (Given)
        val requestDTO = EnderecoService.EnderecoDTO(
            idEndereco = 0, // Id é 0 na criação
            idUsuario = 1,
            logradouro = "Rua Teste",
            numero = "123",
            cidade = "Cidade Teste",
            estado = "TS",
            cep = "12345-678",
            rua = "Rua Teste",
            complemento = "Apto 101",
            pais = "Brasil",
            latitude = -10.0,
            longitude = -20.0
        )

        val expectedResultMap = mapOf<String, Any>(
            "id_endereco" to 1,
            "id_usuario" to 1,
            "cidade" to "Cidade Teste",
            "estado" to "TS",
            "pais" to "Brasil"
        )

        // Configura o mock (como 'mockResolvedValue' ou 'mockImplementation')
        // "quando jdbcTemplate.queryForMap for chamado COM QUALQUER string (sql)
        // E com os parâmetros da request, ENTÃO retorne o 'expectedResultMap'"
        whenever(
            jdbcTemplate.queryForMap(
                any<String>(), // Não nos importamos com o SQL exato no mock
                eq(requestDTO.idUsuario),
                eq(requestDTO.logradouro),
                eq(requestDTO.numero),
                eq(requestDTO.cidade),
                eq(requestDTO.estado),
                eq(requestDTO.cep),
                eq(requestDTO.rua),
                eq(requestDTO.complemento),
                eq(requestDTO.pais),
                eq(requestDTO.longitude), // Note a ordem: longitude, latitude
                eq(requestDTO.latitude)
            )
        ).thenReturn(expectedResultMap)

        // 2. Act (When)
        val result = enderecoService.criarEndereco(requestDTO)

        // 3. Assert (Then)
        assertThat(result).isEqualTo(expectedResultMap) // (como expect(result).toEqual(expectedResultMap))
        assertThat(result["id_endereco"]).isEqualTo(1)
        assertThat(result["cidade"]).isEqualTo("Cidade Teste")
    }

    @Test
    fun `deve listar enderecos por usuario`() {
        // 1. Arrange
        val idUsuario = 1
        val expectedDTO = EnderecoService.EnderecoDTO(
            idEndereco = 1, idUsuario = idUsuario, logradouro = "Rua", numero = "123",
            cidade = "Cidade", estado = "TS", cep = "12345-000", rua = "Rua",
            complemento = null, pais = "Brasil", latitude = -10.0, longitude = -20.0
        )
        val expectedList = listOf(expectedDTO)

        // Configura o mock: "quando query for chamada com qualquer string,
        // um array contendo idUsuario, e qualquer RowMapper..."
        // A sintaxe 'any()' captura a lambda do RowMapper
        whenever(
            jdbcTemplate.query(any<String>(), eq(arrayOf(idUsuario)), any<RowMapper<EnderecoService.EnderecoDTO>>())
        ).thenReturn(expectedList)

        // 2. Act
        val result = enderecoService.listarEnderecos(idUsuario)

        // 3. Assert
        assertThat(result).isEqualTo(expectedList)
        assertThat(result).hasSize(1)
        assertThat(result[0].idUsuario).isEqualTo(idUsuario)
    }

    @Test
    fun `deve deletar um endereco`() {
        // 1. Arrange
        val idEndereco = 1

        // Configura o mock para um método que retorna 'Int' (linhas afetadas)
        whenever(
            jdbcTemplate.update(any<String>(), eq(idEndereco))
        ).thenReturn(1) // Simula que 1 linha foi afetada

        // 2. Act
        // 'deletarEndereco' não retorna nada (Unit), então apenas o executamos
        enderecoService.deletarEndereco(idEndereco)

        // 3. Assert
        // Em vez de verificar um retorno, verificamos se o mock foi chamado corretamente
        // (como 'expect(mockFn).toHaveBeenCalledWith(idEndereco)')
        verify(jdbcTemplate).update(
            eq("DELETE FROM endereco WHERE id_endereco = ?"),
            eq(idEndereco)
        )
    }

    @Test
    fun `deve atualizar um endereco`() {
        // 1. Arrange
        val idEndereco = 1
        val requestDTO = EnderecoService.EnderecoDTO(
            idEndereco = 0, // O ID no DTO não importa para o update
            idUsuario = 1,
            logradouro = "Rua Nova",
            numero = "456",
            cidade = "Cidade Nova",
            estado = "NV",
            cep = "98765-432",
            rua = "Rua Nova",
            complemento = null,
            pais = "Brasil",
            latitude = -11.0,
            longitude = -22.0
        )

        // Configura o mock para o 'update'
        whenever(
            jdbcTemplate.update(
                any<String>(), // Não nos importamos com o SQL exato no mock
                eq(requestDTO.logradouro),
                eq(requestDTO.numero),
                eq(requestDTO.cidade),
                eq(requestDTO.estado),
                eq(requestDTO.cep),
                eq(requestDTO.rua),
                eq(requestDTO.complemento),
                eq(requestDTO.pais),
                eq(requestDTO.longitude), // Note a ordem: longitude, latitude
                eq(requestDTO.latitude),
                eq(idEndereco) // O ID do endereço é o último parâmetro
            )
        ).thenReturn(1) // Simula que 1 linha foi atualizada

        // 2. Act
        val linhasAfetadas = enderecoService.atualizarEndereco(idEndereco, requestDTO)

        // 3. Assert
        assertThat(linhasAfetadas).isEqualTo(1)
    }
}