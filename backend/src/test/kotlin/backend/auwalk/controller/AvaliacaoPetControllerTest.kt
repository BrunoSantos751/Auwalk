package backend.auwalk.controller

import backend.auwalk.service.AvaliacaoPetService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Transactional
class AvaliacaoPetControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var avaliacaoPetService: AvaliacaoPetService

    private var avaliacaoId1: Int? = null
    private var avaliacaoId2: Int? = null

    @Test
    @Order(1)
    fun `deve criar uma avaliação de pet`() {
        val avaliacaoDTO = AvaliacaoPetService.AvaliacaoPetDTO(
            idPet = 13,
            idServico = 48,
            nota = 5,
            comentario = "Pet muito bem cuidado!"
        )

        val result = mockMvc.perform(
            post("/avaliacoes-pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avaliacaoDTO))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id_pet").value(13))
            .andExpect(jsonPath("$.id_servico").value(48))
            .andExpect(jsonPath("$.nota").value(5))
            .andExpect(jsonPath("$.comentario").value("Pet muito bem cuidado!"))
            .andExpect(jsonPath("$.id_avaliacao").exists())
            .andExpect(jsonPath("$.data").exists())
            .andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        avaliacaoId1 = response.get("id_avaliacao").asInt()
    }

    @Test
    @Order(2)
    fun `deve criar uma avaliação sem comentário`() {
        val avaliacaoDTO = AvaliacaoPetService.AvaliacaoPetDTO(
            idPet = 13,
            idServico = 49,
            nota = 4
        )

        val result = mockMvc.perform(
            post("/avaliacoes-pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avaliacaoDTO))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id_pet").value(13))
            .andExpect(jsonPath("$.id_servico").value(49))
            .andExpect(jsonPath("$.nota").value(4))
            .andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        avaliacaoId2 = response.get("id_avaliacao").asInt()
    }

    @Test
    @Order(3)
    fun `deve listar todas as avaliações`() {
        // Primeiro cria uma avaliação para garantir que há dados
        val avaliacaoDTO = AvaliacaoPetService.AvaliacaoPetDTO(
            idPet = 14,
            idServico = 48,
            nota = 5,
            comentario = "Excelente serviço"
        )
        mockMvc.perform(
            post("/avaliacoes-pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avaliacaoDTO))
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/avaliacoes-pet"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    @Order(4)
    fun `deve listar avaliações por pet`() {
        // Primeiro cria uma avaliação para o pet 1
        val avaliacaoDTO = AvaliacaoPetService.AvaliacaoPetDTO(
            idPet = 13,
            idServico = 50,
            nota = 5,
            comentario = "Muito bom"
        )
        mockMvc.perform(
            post("/avaliacoes-pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avaliacaoDTO))
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/avaliacoes-pet").param("idPet", "13"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].idPet").value(13))
    }

    @Test
    @Order(5)
    fun `deve listar avaliações por serviço`() {
        // Primeiro cria uma avaliação para o serviço 1
        val avaliacaoDTO = AvaliacaoPetService.AvaliacaoPetDTO(
            idPet = 14,
            idServico = 48,
            nota = 4,
            comentario = "Bom serviço"
        )
        mockMvc.perform(
            post("/avaliacoes-pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avaliacaoDTO))
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/avaliacoes-pet").param("idServico", "48"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].idServico").value(48))
    }

    @Test
    @Order(6)
    fun `deve buscar avaliação por ID`() {
        // Primeiro cria uma avaliação
        val avaliacaoDTO = AvaliacaoPetService.AvaliacaoPetDTO(
            idPet = 13,
            idServico = 48,
            nota = 5,
            comentario = "Teste de busca"
        )

        val createResult = mockMvc.perform(
            post("/avaliacoes-pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avaliacaoDTO))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val createdResponse = objectMapper.readTree(createResult.response.contentAsString)
        val avaliacaoId = createdResponse.get("id_avaliacao").asInt()

        // Busca a avaliação
        mockMvc.perform(get("/avaliacoes-pet/$avaliacaoId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.idAvaliacao").value(avaliacaoId))
            .andExpect(jsonPath("$.idPet").value(13))
            .andExpect(jsonPath("$.idServico").value(48))
            .andExpect(jsonPath("$.nota").value(5))
            .andExpect(jsonPath("$.comentario").value("Teste de busca"))
    }

    @Test
    @Order(7)
    fun `deve retornar 404 ao buscar avaliação inexistente`() {
        mockMvc.perform(get("/avaliacoes-pet/99999"))
            .andExpect(status().isNotFound)
    }

    @Test
    @Order(8)
    fun `deve atualizar uma avaliação`() {
        // Primeiro cria uma avaliação
        val avaliacaoDTO = AvaliacaoPetService.AvaliacaoPetDTO(
            idPet = 13,
            idServico = 48,
            nota = 3,
            comentario = "Comentário original"
        )

        val createResult = mockMvc.perform(
            post("/avaliacoes-pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avaliacaoDTO))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val createdResponse = objectMapper.readTree(createResult.response.contentAsString)
        val avaliacaoId = createdResponse.get("id_avaliacao").asInt()

        // Atualiza a avaliação
        val updateDTO = AvaliacaoPetService.AvaliacaoPetDTO(
            idPet = 13,
            idServico = 48,
            nota = 5,
            comentario = "Comentário atualizado"
        )

        mockMvc.perform(
            put("/avaliacoes-pet/$avaliacaoId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nota").value(5))
            .andExpect(jsonPath("$.comentario").value("Comentário atualizado"))
    }

    @Test
    @Order(9)
    fun `deve deletar uma avaliação`() {
        // Primeiro cria uma avaliação
        val avaliacaoDTO = AvaliacaoPetService.AvaliacaoPetDTO(
            idPet = 13,
            idServico = 48,
            nota = 4,
            comentario = "Será deletada"
        )

        val createResult = mockMvc.perform(
            post("/avaliacoes-pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avaliacaoDTO))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val createdResponse = objectMapper.readTree(createResult.response.contentAsString)
        val avaliacaoId = createdResponse.get("id_avaliacao").asInt()

        // Deleta a avaliação
        mockMvc.perform(delete("/avaliacoes-pet/$avaliacaoId"))
            .andExpect(status().isNoContent)

        // Verifica que foi deletada
        mockMvc.perform(get("/avaliacoes-pet/$avaliacaoId"))
            .andExpect(status().isNotFound)
    }

    @Test
    @Order(10)
    fun `deve calcular nota média geral`() {
        // Primeiro cria algumas avaliações
        val avaliacoes = listOf(
            AvaliacaoPetService.AvaliacaoPetDTO(
                idPet = 13,
                idServico = 48,
                nota = 5
            ),
            AvaliacaoPetService.AvaliacaoPetDTO(
                idPet = 14,
                idServico = 48,
                nota = 4
            ),
            AvaliacaoPetService.AvaliacaoPetDTO(
                idPet = 15,
                idServico = 48,
                nota = 3
            )
        )

        avaliacoes.forEach { avaliacao ->
            mockMvc.perform(
                post("/avaliacoes-pet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(avaliacao))
            ).andExpect(status().isCreated)
        }

        mockMvc.perform(get("/avaliacoes-pet/media"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nota_media").exists())
            .andExpect(jsonPath("$.total_avaliacoes").exists())
    }

    @Test
    @Order(11)
    fun `deve calcular nota média por pet`() {
        // Primeiro cria avaliações para o pet 1
        val avaliacoes = listOf(
            AvaliacaoPetService.AvaliacaoPetDTO(
                idPet = 13,
                idServico = 48,
                nota = 5
            ),
            AvaliacaoPetService.AvaliacaoPetDTO(
                idPet = 13,
                idServico = 49,
                nota = 4
            )
        )

        avaliacoes.forEach { avaliacao ->
            mockMvc.perform(
                post("/avaliacoes-pet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(avaliacao))
            ).andExpect(status().isCreated)
        }

        mockMvc.perform(get("/avaliacoes-pet/media").param("idPet", "13"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nota_media").exists())
            .andExpect(jsonPath("$.total_avaliacoes").exists())
    }

    @Test
    @Order(12)
    fun `deve calcular nota média por serviço`() {
        // Primeiro cria avaliações para o serviço 1
        val avaliacoes = listOf(
            AvaliacaoPetService.AvaliacaoPetDTO(
                idPet = 13,
                idServico = 48,
                nota = 5
            ),
            AvaliacaoPetService.AvaliacaoPetDTO(
                idPet = 14,
                idServico = 48,
                nota = 3
            )
        )

        avaliacoes.forEach { avaliacao ->
            mockMvc.perform(
                post("/avaliacoes-pet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(avaliacao))
            ).andExpect(status().isCreated)
        }

        mockMvc.perform(get("/avaliacoes-pet/media").param("idServico", "48"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nota_media").exists())
            .andExpect(jsonPath("$.total_avaliacoes").exists())
    }

    @Test
    @Order(13)
    fun `deve retornar erro ao criar avaliação com dados inválidos`() {
        val avaliacaoDTO = mapOf(
            "idPet" to "invalido",
            "idServico" to 48,
            "nota" to 5
        )

        mockMvc.perform(
            post("/avaliacoes-pet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avaliacaoDTO))
        )
            .andExpect(status().isBadRequest)
    }
}

