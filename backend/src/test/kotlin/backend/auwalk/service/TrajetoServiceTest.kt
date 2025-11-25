package backend.auwalk.service

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Transactional
class TrajetoServiceTest {

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

        val resultado = trajetoService.criarTrajeto(trajetoDTO)

        Assertions.assertNotNull(resultado)
        Assertions.assertEquals(39, resultado["id_passeio"])
        Assertions.assertEquals(1, resultado["ordem"])
        Assertions.assertNotNull(resultado["id_trajeto"])
        
        trajetoId1 = resultado["id_trajeto"] as Int
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

        val resultado = trajetoService.criarTrajeto(trajetoDTO)

        Assertions.assertNotNull(resultado)
        Assertions.assertEquals(40, resultado["id_passeio"])
        Assertions.assertEquals(1, resultado["ordem"])
        Assertions.assertNotNull(resultado["id_trajeto"])
        
        trajetoId2 = resultado["id_trajeto"] as Int
    }

    @Test
    @Order(3)
    fun `deve criar múltiplos trajetos em lote`() {
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
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 40,
                ordem = 2,
                latitude = -23.5525,
                longitude = -46.6355
            )
        )

        val resultados = trajetoService.criarTrajetos(trajetos)

        Assertions.assertEquals(3, resultados.size)
        Assertions.assertEquals(39, resultados[0]["id_passeio"])
        Assertions.assertEquals(2, resultados[0]["ordem"])
        Assertions.assertEquals(39, resultados[1]["id_passeio"])
        Assertions.assertEquals(3, resultados[1]["ordem"])
        Assertions.assertEquals(40, resultados[2]["id_passeio"])
        Assertions.assertEquals(2, resultados[2]["ordem"])
    }

    @Test
    @Order(4)
    fun `deve listar todos os trajetos`() {
        // Primeiro cria alguns trajetos para garantir que há dados
        trajetoService.criarTrajeto(
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 50,
                latitude = -23.5505,
                longitude = -46.6333
            )
        )
        
        val trajetos = trajetoService.listarTrajetos()

        Assertions.assertNotNull(trajetos)
        Assertions.assertTrue(trajetos.isNotEmpty())
    }

    @Test
    @Order(5)
    fun `deve listar trajetos do passeio 39`() {
        val trajetos = trajetoService.listarTrajetos(39)

        Assertions.assertNotNull(trajetos)
        trajetos.forEach { trajeto ->
            Assertions.assertEquals(39, trajeto.idPasseio)
            Assertions.assertNotNull(trajeto.idTrajeto)
            Assertions.assertNotNull(trajeto.ordem)
            Assertions.assertNotNull(trajeto.latitude)
            Assertions.assertNotNull(trajeto.longitude)
        }
    }

    @Test
    @Order(6)
    fun `deve listar trajetos do passeio 40`() {
        val trajetos = trajetoService.listarTrajetos(40)

        Assertions.assertNotNull(trajetos)
        trajetos.forEach { trajeto ->
            Assertions.assertEquals(40, trajeto.idPasseio)
            Assertions.assertNotNull(trajeto.idTrajeto)
            Assertions.assertNotNull(trajeto.ordem)
            Assertions.assertNotNull(trajeto.latitude)
            Assertions.assertNotNull(trajeto.longitude)
        }
    }

    @Test
    @Order(7)
    fun `deve listar trajetos ordenados por ordem`() {
        val trajetos = trajetoService.listarTrajetos(39)

        if (trajetos.size > 1) {
            for (i in 0 until trajetos.size - 1) {
                Assertions.assertTrue(
                    trajetos[i].ordem <= trajetos[i + 1].ordem,
                    "Trajetos devem estar ordenados por ordem"
                )
            }
        }
    }

    @Test
    @Order(8)
    fun `deve atualizar um trajeto`() {
        // Primeiro cria um trajeto
        val trajetoDTO = TrajetoService.TrajetoDTO(
            idPasseio = 39,
            ordem = 10,
            latitude = -23.5600,
            longitude = -46.6400
        )

        val resultado = trajetoService.criarTrajeto(trajetoDTO)
        val trajetoId = resultado["id_trajeto"] as Int

        // Atualiza o trajeto
        val updateDTO = TrajetoService.TrajetoDTO(
            idPasseio = 39,
            ordem = 11,
            latitude = -23.5700,
            longitude = -46.6500
        )

        val linhasAfetadas = trajetoService.atualizarTrajeto(trajetoId, updateDTO)

        Assertions.assertEquals(1, linhasAfetadas)

        // Verifica se foi atualizado
        val trajetos = trajetoService.listarTrajetos(39)
        val trajetoAtualizado = trajetos.find { it.idTrajeto == trajetoId }

        Assertions.assertNotNull(trajetoAtualizado)
        Assertions.assertEquals(11, trajetoAtualizado?.ordem)
    }

    @Test
    @Order(9)
    fun `deve deletar um trajeto específico`() {
        // Primeiro cria um trajeto
        val trajetoDTO = TrajetoService.TrajetoDTO(
            idPasseio = 40,
            ordem = 20,
            latitude = -23.5800,
            longitude = -46.6600
        )

        val resultado = trajetoService.criarTrajeto(trajetoDTO)
        val trajetoId = resultado["id_trajeto"] as Int

        // Deleta o trajeto
        trajetoService.deletarTrajeto(trajetoId)

        // Verifica se foi deletado
        val trajetos = trajetoService.listarTrajetos(40)
        val trajetoDeletado = trajetos.find { it.idTrajeto == trajetoId }

        Assertions.assertNull(trajetoDeletado)
    }

    @Test
    @Order(10)
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

        trajetoService.criarTrajetos(trajetos)

        // Deleta todos os trajetos do passeio 40
        trajetoService.deletarTrajetosPorPasseio(40)

        // Verifica que não há mais trajetos do passeio 40
        val trajetosDepois = trajetoService.listarTrajetos(40)
        Assertions.assertEquals(0, trajetosDepois.size)
    }

    @Test
    @Order(11)
    fun `deve validar coordenadas geográficas`() {
        val trajetoDTO = TrajetoService.TrajetoDTO(
            idPasseio = 39,
            ordem = 100,
            latitude = -23.5505,
            longitude = -46.6333
        )

        val resultado = trajetoService.criarTrajeto(trajetoDTO)
        val trajetos = trajetoService.listarTrajetos(39)
        val trajetoCriado = requireNotNull(trajetos.find { it.idTrajeto == resultado["id_trajeto"] as Int })

        Assertions.assertEquals(-23.5505, trajetoCriado.latitude, 0.0001)
        Assertions.assertEquals(-46.6333, trajetoCriado.longitude, 0.0001)
    }

    @Test
    @Order(12)
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
            ),
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = 5,
                latitude = -23.5525,
                longitude = -46.6355
            )
        )

        trajetoService.criarTrajetos(trajetos)

        // Simplifica o trajeto
        val resultado = trajetoService.simplificarTrajeto(39, 10.0)

        Assertions.assertNotNull(resultado)
        Assertions.assertEquals(39, resultado["id_passeio"])
        Assertions.assertNotNull(resultado["trajeto_wkt"])
    }

    @Test
    @Order(13)
    fun `deve simplificar trajeto do passeio 40`() {
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

        trajetoService.criarTrajetos(trajetos)

        // Simplifica o trajeto com epsilon maior
        val resultado = trajetoService.simplificarTrajeto(40, 50.0)

        Assertions.assertNotNull(resultado)
        Assertions.assertEquals(40, resultado["id_passeio"])
        Assertions.assertNotNull(resultado["trajeto_wkt"])
    }

    @Test
    @Order(14)
    fun `deve lançar exceção ao simplificar trajeto sem pontos`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            trajetoService.simplificarTrajeto(999, 10.0)
        }
    }

    @Test
    @Order(15)
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

        trajetoService.criarTrajetos(trajetos)
        trajetoService.simplificarTrajeto(39, 10.0)

        // Busca o trajeto simplificado
        val trajetoSimplificado = trajetoService.buscarTrajetoSimplificado(39)

        Assertions.assertNotNull(trajetoSimplificado)
        Assertions.assertEquals(39, trajetoSimplificado!!["id_passeio"])
        Assertions.assertNotNull(trajetoSimplificado["trajeto_wkt"])
        Assertions.assertNotNull(trajetoSimplificado["trajeto_geojson"])
        Assertions.assertNotNull(trajetoSimplificado["num_pontos"])
    }

    @Test
    @Order(16)
    fun `deve retornar null ao buscar trajeto simplificado inexistente`() {
        val trajetoSimplificado = trajetoService.buscarTrajetoSimplificado(999)
        Assertions.assertNull(trajetoSimplificado)
    }

    @Test
    @Order(17)
    fun `deve simplificar trajeto mantendo pontos importantes`() {
        // Cria um trajeto com muitos pontos próximos
        val trajetos = (1..10).map { i ->
            TrajetoService.TrajetoDTO(
                idPasseio = 39,
                ordem = i,
                latitude = -23.5505 + (i * 0.0001),
                longitude = -46.6333 + (i * 0.0001)
            )
        }

        trajetoService.criarTrajetos(trajetos)

        // Simplifica com epsilon pequeno (mantém mais pontos)
        val resultado = trajetoService.simplificarTrajeto(39, 1.0)

        Assertions.assertNotNull(resultado)
        
        // Busca o trajeto simplificado e verifica que tem pontos
        val trajetoSimplificado = trajetoService.buscarTrajetoSimplificado(39)
        Assertions.assertNotNull(trajetoSimplificado)
        val numPontos = trajetoSimplificado!!["num_pontos"] as Number
        Assertions.assertTrue(numPontos.toInt() >= 2)
    }
}

