package backend.auwalk.controller

import backend.auwalk.service.TrajetoService
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
class TrajetoControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var trajetoService: TrajetoService

    private var trajetoId1: Int? = null
    private var trajetoId2: Int? = null

    @Test
    @Order(1)
    fun `deve criar um trajeto para o passeio 39`() {
        val trajetoDTO = TrajetoService.TrajetoDTO(
            idPasseio = 39,
            ordem = 1,
            latitude = -23.5505,
            longitude = -46.6333
        )

        val result = mockMvc.perform(
            post("/trajetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetoDTO))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id_passeio").value(39))
            .andExpect(jsonPath("$.ordem").value(1))
            .andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        trajetoId1 = response.get("id_trajeto").asInt()
    }

    @Test
    @Order(2)
    fun `deve criar um trajeto para o passeio 40`() {
        val trajetoDTO = TrajetoService.TrajetoDTO(
            idPasseio = 40,
            ordem = 1,
            latitude = -23.5510,
            longitude = -46.6340
        )

        val result = mockMvc.perform(
            post("/trajetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetoDTO))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id_passeio").value(40))
            .andExpect(jsonPath("$.ordem").value(1))
            .andReturn()

        val response = objectMapper.readTree(result.response.contentAsString)
        trajetoId2 = response.get("id_trajeto").asInt()
    }

    @Test
    @Order(3)
    fun `deve criar múltiplos trajetos em lote para o passeio 39`() {
        val trajetos = listOf(
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 2,
                latitude = -23.5515,
                longitude = -46.6345
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 3,
                latitude = -23.5520,
                longitude = -46.6350
            )
        )

        mockMvc.perform(
            post("/trajetos/lote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetos))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id_passeio").value(39))
            .andExpect(jsonPath("$[0].ordem").value(2))
            .andExpect(jsonPath("$[1].id_passeio").value(39))
            .andExpect(jsonPath("$[1].ordem").value(3))
    }

    @Test
    @Order(4)
    fun `deve listar todos os trajetos`() {
        mockMvc.perform(get("/trajetos"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    @Order(5)
    fun `deve listar trajetos do passeio 39`() {
        // Primeiro cria um trajeto para garantir que há dados
        val trajetoDTO = TrajetoService.TrajetoDTO(
            idPasseio = 39,
            ordem = 100,
            latitude = -23.5505,
            longitude = -46.6333
        )
        mockMvc.perform(
            post("/trajetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetoDTO))
        ).andExpect(status().isOk)

        // Agora lista os trajetos
        mockMvc.perform(get("/trajetos").param("idPasseio", "39"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].idPasseio").value(39))
    }

    @Test
    @Order(6)
    fun `deve listar trajetos do passeio 40`() {
        // Primeiro cria um trajeto para garantir que há dados
        val trajetoDTO = TrajetoService.TrajetoDTO(
            idPasseio = 40,
            ordem = 100,
            latitude = -23.5510,
            longitude = -46.6340
        )
        mockMvc.perform(
            post("/trajetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetoDTO))
        ).andExpect(status().isOk)

        // Agora lista os trajetos
        mockMvc.perform(get("/trajetos").param("idPasseio", "40"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].idPasseio").value(40))
    }

    @Test
    @Order(7)
    fun `deve atualizar um trajeto`() {
        // Primeiro cria um trajeto
        val trajetoDTO = TrajetoService.TrajetoDTO(
            idPasseio = 39,
            ordem = 10,
            latitude = -23.5600,
            longitude = -46.6400
        )

        val createResult = mockMvc.perform(
            post("/trajetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetoDTO))
        )
            .andExpect(status().isOk)
            .andReturn()

        val createdResponse = objectMapper.readTree(createResult.response.contentAsString)
        val trajetoId = createdResponse.get("id_trajeto").asInt()

        // Atualiza o trajeto
        val updateDTO = TrajetoService.TrajetoDTO(
            idPasseio = 39,
            ordem = 11,
            latitude = -23.5700,
            longitude = -46.6500
        )

        mockMvc.perform(
            put("/trajetos/$trajetoId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(1)) // Retorna número de linhas afetadas
    }

    @Test
    @Order(8)
    fun `deve deletar um trajeto específico`() {
        // Primeiro cria um trajeto
        val trajetoDTO = TrajetoService.TrajetoDTO(
            idPasseio = 40,
            ordem = 20,
            latitude = -23.5800,
            longitude = -46.6600
        )

        val createResult = mockMvc.perform(
            post("/trajetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetoDTO))
        )
            .andExpect(status().isOk)
            .andReturn()

        val createdResponse = objectMapper.readTree(createResult.response.contentAsString)
        val trajetoId = createdResponse.get("id_trajeto").asInt()

        // Deleta o trajeto
        mockMvc.perform(delete("/trajetos/$trajetoId"))
            .andExpect(status().isNoContent)
    }

    @Test
    @Order(9)
    fun `deve deletar todos os trajetos de um passeio`() {
        // Primeiro cria alguns trajetos para o passeio 40
        val trajetos = listOf(
            TrajetoService.TrajetoDTO(
                idPasseio = 40,
                ordem = 30,
                latitude = -23.5900,
                longitude = -46.6700
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 40,
                ordem = 31,
                latitude = -23.6000,
                longitude = -46.6800
            )
        )

        mockMvc.perform(
            post("/trajetos/lote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetos))
        )
            .andExpect(status().isOk)

        // Deleta todos os trajetos do passeio 40
        mockMvc.perform(delete("/trajetos/passeio/40"))
            .andExpect(status().isNoContent)

        // Verifica que não há mais trajetos do passeio 40
        mockMvc.perform(get("/trajetos").param("idPasseio", "40"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty)
    }

    @Test
    @Order(10)
    fun `deve simplificar trajeto do passeio 39`() {
        // Primeiro cria vários pontos para formar um trajeto
        val trajetos = listOf(
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 1,
                latitude = -23.5505,
                longitude = -46.6333
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 2,
                latitude = -23.5510,
                longitude = -46.6340
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 3,
                latitude = -23.5515,
                longitude = -46.6345
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 4,
                latitude = -23.5520,
                longitude = -46.6350
            )
        )

        mockMvc.perform(
            post("/trajetos/lote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetos))
        ).andExpect(status().isOk)

        // Simplifica o trajeto
        mockMvc.perform(
            post("/trajetos/simplificar/39")
                .param("epsilonMetros", "10.0")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id_passeio").value(39))
            .andExpect(jsonPath("$.trajeto_wkt").exists())
    }

    @Test
    @Order(11)
    fun `deve simplificar trajeto do passeio 40 com epsilon customizado`() {
        // Primeiro cria vários pontos para formar um trajeto
        val trajetos = listOf(
            TrajetoService.TrajetoDTO(
                idPasseio = 40,
                ordem = 1,
                latitude = -23.5600,
                longitude = -46.6400
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 40,
                ordem = 2,
                latitude = -23.5610,
                longitude = -46.6410
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 40,
                ordem = 3,
                latitude = -23.5620,
                longitude = -46.6420
            )
        )

        mockMvc.perform(
            post("/trajetos/lote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetos))
        ).andExpect(status().isOk)

        // Simplifica o trajeto com epsilon maior
        mockMvc.perform(
            post("/trajetos/simplificar/40")
                .param("epsilonMetros", "50.0")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id_passeio").value(40))
            .andExpect(jsonPath("$.trajeto_wkt").exists())
    }

    @Test
    @Order(12)
    fun `deve retornar erro ao simplificar trajeto sem pontos`() {
        mockMvc.perform(
            post("/trajetos/simplificar/999")
                .param("epsilonMetros", "10.0")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @Order(13)
    fun `deve buscar trajeto simplificado do passeio 39`() {
        // Primeiro cria e simplifica um trajeto
        val trajetos = listOf(
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 1,
                latitude = -23.5505,
                longitude = -46.6333
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 2,
                latitude = -23.5510,
                longitude = -46.6340
            )
        )

        mockMvc.perform(
            post("/trajetos/lote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetos))
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/trajetos/simplificar/39")
                .param("epsilonMetros", "10.0")
        ).andExpect(status().isOk)

        // Busca o trajeto simplificado
        mockMvc.perform(get("/trajetos/simplificado/39"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id_passeio").value(39))
            .andExpect(jsonPath("$.trajeto_wkt").exists())
            .andExpect(jsonPath("$.trajeto_geojson").exists())
            .andExpect(jsonPath("$.num_pontos").exists())
    }

    @Test
    @Order(14)
    fun `deve retornar 404 ao buscar trajeto simplificado inexistente`() {
        mockMvc.perform(get("/trajetos/simplificado/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    @Order(15)
    fun `deve usar epsilon padrão quando não fornecido`() {
        // Primeiro cria pontos
        val trajetos = listOf(
            TrajetoService.TrajetoDTO(
                idPasseio = 40,
                ordem = 1,
                latitude = -23.5600,
                longitude = -46.6400
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 40,
                ordem = 2,
                latitude = -23.5610,
                longitude = -46.6410
            )
        )

        mockMvc.perform(
            post("/trajetos/lote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trajetos))
        ).andExpect(status().isOk)

        // Simplifica sem fornecer epsilon (deve usar padrão 10.0)
        mockMvc.perform(post("/trajetos/simplificar/40"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id_passeio").value(40))
    }
}

