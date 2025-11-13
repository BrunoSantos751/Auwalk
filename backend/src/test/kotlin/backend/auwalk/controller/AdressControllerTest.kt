package backend.auwalk.controller

import backend.auwalk.service.EnderecoService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.* // post, get, delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.* // status, jsonPath

// Carrega apenas o contexto web para o EnderecoController
@WebMvcTest(EnderecoController::class)
class EnderecoControllerTest {

    // Utilitário para disparar requisições HTTP falsas (como o 'supertest' do Jest)
    @Autowired
    private lateinit var mockMvc: MockMvc

    // Cria um MOCK do serviço. O Controller real usará este mock.
    @MockBean
    private lateinit var enderecoService: EnderecoService

    // Usado para converter objetos Kotlin/Java em JSON e vice-versa
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `deve criar um endereco via POST`() {
        // 1. Arrange
        val requestDTO = EnderecoService.EnderecoDTO(
            idEndereco = 0, idUsuario = 1, logradouro = "Rua Teste", numero = "123",
            cidade = "Cidade Teste", estado = "TS", cep = "12345-678", rua = "Rua Teste",
            complemento = "Apto 101", pais = "Brasil", latitude = -10.0, longitude = -20.0
        )

        val expectedResultMap = mapOf<String, Any>(
            "id_endereco" to 1,
            "id_usuario" to 1,
            "cidade" to "Cidade Teste"
        )
        whenever(enderecoService.criarEndereco(any())).thenReturn(expectedResultMap)

        // 2. Act & 3. Assert (combinados no MockMvc)
        mockMvc.perform(
            post("/enderecos") // Simula um POST para /enderecos
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)) // Envia o DTO como JSON
        )
            .andExpect(status().isOk) // (como expect(res.status).toBe(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // Verifica o JSON de resposta (como expect(res.body.id_endereco).toBe(1))
            .andExpect(jsonPath("$.id_endereco").value(1))
            .andExpect(jsonPath("$.cidade").value("Cidade Teste"))
    }

    @Test
    fun `deve listar enderecos por idUsuario via GET`() {
        // 1. Arrange
        val idUsuario = 1
        val expectedDTO = EnderecoService.EnderecoDTO(
            idEndereco = 1, idUsuario = idUsuario, logradouro = "Rua", numero = "123",
            cidade = "Cidade", estado = "TS", cep = "12345-000", rua = "Rua",
            complemento = null, pais = "Brasil", latitude = -10.0, longitude = -20.0
        )
        val expectedList = listOf(expectedDTO)

        // Configura o mock do serviço
        whenever(enderecoService.listarEnderecos(eq(idUsuario))).thenReturn(expectedList)

        // 2. Act & 3. Assert
        mockMvc.perform(
            get("/enderecos") // Simula um GET para /enderecos
                .param("idUsuario", idUsuario.toString()) // Adiciona o query param
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            // Verifica se a resposta é um array ([...])
            .andExpect(jsonPath("$").isArray)
            // Verifica o primeiro item do array
            .andExpect(jsonPath("$[0].idEndereco").value(1))
            .andExpect(jsonPath("$[0].cidade").value("Cidade"))
    }

    @Test
    fun `deve deletar um endereco via DELETE`() {
        // 1. Arrange
        val idEndereco = 1

        // Configura o mock para um método 'void' (Unit)
        doNothing().whenever(enderecoService).deletarEndereco(eq(idEndereco))

        // 2. Act & 3. Assert
        mockMvc.perform(
            delete("/enderecos/{id}", idEndereco) // Simula um DELETE para /enderecos/1
        )
            .andExpect(status().isNoContent) // Verifica pelo status 204 No Content

        // Opcionalmente, verifique se o serviço foi chamado
        verify(enderecoService).deletarEndereco(eq(idEndereco))
    }


    @Test
    fun `deve atualizar um endereco via PUT`() {
        // 1. Arrange
        val idEndereco = 1
        val requestDTO = EnderecoService.EnderecoDTO(
            idEndereco = 0, idUsuario = 1, logradouro = "Rua Nova", numero = "456",
            cidade = "Cidade Nova", estado = "NV", cep = "98765-432", rua = "Rua Nova",
            complemento = null, pais = "Brasil", latitude = -11.0, longitude = -22.0
        )

        // Configura o mock do serviço
        whenever(enderecoService.atualizarEndereco(eq(idEndereco), any()))
            .thenReturn(1) // Simula 1 linha afetada

        // 2. Act & 3. Assert
        mockMvc.perform(
            put("/enderecos/{id}", idEndereco) // Simula um PUT para /enderecos/1
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)) // Envia o DTO
        )
            .andExpect(status().isOk)
            // Verifica se o corpo da resposta é o número 1
            .andExpect(content().string("1"))
    }
}