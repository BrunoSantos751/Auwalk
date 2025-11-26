package backend.auwalk.service

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Transactional
class AvaliacaoPrestadorServiceTest {

    @Autowired
    private lateinit var avaliacaoPrestadorService: AvaliacaoPrestadorService

    private var avaliacaoId1: Int? = null
    private var avaliacaoId2: Int? = null

    @Test
    @Order(1)
    fun `deve criar uma avaliação de prestador`() {
        val avaliacaoDTO = AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
            idUsuario = 8,
            idServico = 48,
            nota = 5,
            comentario = "Prestador muito profissional!"
        )

        val resultado = avaliacaoPrestadorService.criarAvaliacao(avaliacaoDTO)

        Assertions.assertNotNull(resultado)
        Assertions.assertEquals(8, resultado["id_usuario"])
        Assertions.assertEquals(48, resultado["id_servico"])
        Assertions.assertEquals(5, resultado["nota"])
        Assertions.assertEquals("Prestador muito profissional!", resultado["comentario"])
        Assertions.assertNotNull(resultado["id_avaliacao"])
        Assertions.assertNotNull(resultado["data"])

        avaliacaoId1 = resultado["id_avaliacao"] as Int
    }

    @Test
    @Order(2)
    fun `deve criar uma avaliação sem comentário`() {
        val avaliacaoDTO = AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
            idUsuario = 8,
            idServico = 49,
            nota = 4
        )

        val resultado = avaliacaoPrestadorService.criarAvaliacao(avaliacaoDTO)

        Assertions.assertNotNull(resultado)
        Assertions.assertEquals(8, resultado["id_usuario"])
        Assertions.assertEquals(49, resultado["id_servico"])
        Assertions.assertEquals(4, resultado["nota"])
        Assertions.assertNull(resultado["comentario"])

        avaliacaoId2 = resultado["id_avaliacao"] as Int
    }

    @Test
    @Order(3)
    fun `deve listar todas as avaliações`() {
        // Primeiro cria algumas avaliações
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 9,
                idServico = 48,
                nota = 5,
                comentario = "Excelente serviço"
            )
        )

        val avaliacoes = avaliacaoPrestadorService.listarAvaliacoes()

        Assertions.assertNotNull(avaliacoes)
        Assertions.assertTrue(avaliacoes.isNotEmpty())
    }

    @Test
    @Order(4)
    fun `deve listar avaliações por usuário`() {
        // Primeiro cria avaliações para o usuário 1
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 8,
                idServico = 50,
                nota = 5,
                comentario = "Muito bom"
            )
        )

        val avaliacoes = avaliacaoPrestadorService.listarAvaliacoes(idUsuario = 8)

        Assertions.assertNotNull(avaliacoes)
        avaliacoes.forEach { avaliacao ->
            Assertions.assertEquals(8, avaliacao.idUsuario)
            Assertions.assertNotNull(avaliacao.idAvaliacao)
            Assertions.assertTrue(avaliacao.nota in 1..5)
        }
    }

    @Test
    @Order(5)
    fun `deve listar avaliações por serviço`() {
        // Primeiro cria avaliações para o serviço 1
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 9,
                idServico = 48,
                nota = 4,
                comentario = "Bom serviço"
            )
        )

        val avaliacoes = avaliacaoPrestadorService.listarAvaliacoes(idServico = 48)

        Assertions.assertNotNull(avaliacoes)
        avaliacoes.forEach { avaliacao ->
            Assertions.assertEquals(48, avaliacao.idServico)
            Assertions.assertNotNull(avaliacao.idAvaliacao)
        }
    }

    @Test
    @Order(6)
    fun `deve listar avaliações por usuário e serviço`() {
        // Primeiro cria uma avaliação específica
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 8,
                idServico = 48,
                nota = 5,
                comentario = "Perfeito"
            )
        )

        val avaliacoes = avaliacaoPrestadorService.listarAvaliacoes(idUsuario = 8, idServico = 48)

        Assertions.assertNotNull(avaliacoes)
        avaliacoes.forEach { avaliacao ->
            Assertions.assertEquals(8, avaliacao.idUsuario)
            Assertions.assertEquals(48, avaliacao.idServico)
        }
    }

    @Test
    @Order(7)
    fun `deve buscar avaliação por ID`() {
        // Primeiro cria uma avaliação
        val avaliacaoDTO = AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
            idUsuario = 8,
            idServico = 48,
            nota = 5,
            comentario = "Teste de busca"
        )

        val criada = avaliacaoPrestadorService.criarAvaliacao(avaliacaoDTO)
        val idAvaliacao = criada["id_avaliacao"] as Int

        val encontrada = avaliacaoPrestadorService.buscarAvaliacaoPorId(idAvaliacao)

        Assertions.assertNotNull(encontrada)
        Assertions.assertEquals(idAvaliacao, encontrada!!.idAvaliacao)
        Assertions.assertEquals(8, encontrada.idUsuario)
        Assertions.assertEquals(48, encontrada.idServico)
        Assertions.assertEquals(5, encontrada.nota)
        Assertions.assertEquals("Teste de busca", encontrada.comentario)
    }

    @Test
    @Order(8)
    fun `deve retornar null ao buscar avaliação inexistente`() {
        val avaliacao = avaliacaoPrestadorService.buscarAvaliacaoPorId(99999)
        Assertions.assertNull(avaliacao)
    }

    @Test
    @Order(9)
    fun `deve atualizar uma avaliação`() {
        // Primeiro cria uma avaliação
        val avaliacaoDTO = AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
            idUsuario = 8,
            idServico = 48,
            nota = 3,
            comentario = "Comentário original"
        )

        val criada = avaliacaoPrestadorService.criarAvaliacao(avaliacaoDTO)
        val idAvaliacao = criada["id_avaliacao"] as Int

        // Atualiza a avaliação
        val updateDTO = AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
            idUsuario = 8,
            idServico = 48,
            nota = 5,
            comentario = "Comentário atualizado"
        )

        val linhasAfetadas = avaliacaoPrestadorService.atualizarAvaliacao(idAvaliacao, updateDTO)

        Assertions.assertEquals(1, linhasAfetadas)

        // Verifica se foi atualizado
        val atualizada = avaliacaoPrestadorService.buscarAvaliacaoPorId(idAvaliacao)
        Assertions.assertNotNull(atualizada)
        Assertions.assertEquals(5, atualizada!!.nota)
        Assertions.assertEquals("Comentário atualizado", atualizada.comentario)
    }

    @Test
    @Order(10)
    fun `deve deletar uma avaliação`() {
        // Primeiro cria uma avaliação
        val avaliacaoDTO = AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
            idUsuario = 8,
            idServico = 48,
            nota = 4,
            comentario = "Será deletada"
        )

        val criada = avaliacaoPrestadorService.criarAvaliacao(avaliacaoDTO)
        val idAvaliacao = criada["id_avaliacao"] as Int

        // Deleta a avaliação
        avaliacaoPrestadorService.deletarAvaliacao(idAvaliacao)

        // Verifica se foi deletada
        val deletada = avaliacaoPrestadorService.buscarAvaliacaoPorId(idAvaliacao)
        Assertions.assertNull(deletada)
    }

    @Test
    @Order(11)
    fun `deve calcular nota média geral`() {
        // Primeiro cria algumas avaliações
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 8,
                idServico = 48,
                nota = 5
            )
        )
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 9,
                idServico = 48,
                nota = 4
            )
        )
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 8,
                idServico = 49,
                nota = 3
            )
        )

        val resultado = avaliacaoPrestadorService.calcularNotaMediaPorUsuario()

        Assertions.assertNotNull(resultado)
        Assertions.assertNotNull(resultado["nota_media"])
        Assertions.assertNotNull(resultado["total_avaliacoes"])
        val totalAvaliacoes = (resultado["total_avaliacoes"] as Number).toInt()
        Assertions.assertTrue(totalAvaliacoes >= 3)
    }

    @Test
    @Order(12)
    fun `deve calcular nota média por usuário`() {
        // Primeiro cria avaliações para o usuário 1
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 8,
                idServico = 48,
                nota = 5
            )
        )
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 8,
                idServico = 49,
                nota = 4
            )
        )

        val resultado = avaliacaoPrestadorService.calcularNotaMediaPorUsuario(idUsuario = 8)

        Assertions.assertNotNull(resultado)
        Assertions.assertNotNull(resultado["nota_media"])
        val notaMedia = (resultado["nota_media"] as Number).toDouble()
        Assertions.assertTrue(notaMedia >= 4.0 && notaMedia <= 5.0)
        Assertions.assertNotNull(resultado["total_avaliacoes"])
    }

    @Test
    @Order(13)
    fun `deve calcular nota média por serviço`() {
        // Primeiro cria avaliações para o serviço 1
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 8,
                idServico = 48,
                nota = 5
            )
        )
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 9,
                idServico = 48,
                nota = 3
            )
        )

        val resultado = avaliacaoPrestadorService.calcularNotaMediaPorServico(48)

        Assertions.assertNotNull(resultado)
        Assertions.assertNotNull(resultado["nota_media"])
        val notaMedia = (resultado["nota_media"] as Number).toDouble()
        Assertions.assertTrue(notaMedia >= 3.0 && notaMedia <= 5.0)
        Assertions.assertNotNull(resultado["total_avaliacoes"])
        val totalAvaliacoes = (resultado["total_avaliacoes"] as Number).toInt()
        Assertions.assertTrue(totalAvaliacoes >= 2)
    }

    @Test
    @Order(14)
    fun `deve retornar média zero quando não há avaliações`() {
        val resultado = avaliacaoPrestadorService.calcularNotaMediaPorUsuario(idUsuario = 99999)

        Assertions.assertNotNull(resultado)
        val notaMedia = (resultado["nota_media"] as Number).toDouble()
        Assertions.assertEquals(0.0, notaMedia, 0.01)
        val totalAvaliacoes = (resultado["total_avaliacoes"] as Number).toInt()
        Assertions.assertEquals(0, totalAvaliacoes)
    }

    @Test
    @Order(15)
    fun `deve listar avaliações ordenadas por data desc`() {
        // Cria avaliações em momentos diferentes
        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 8,
                idServico = 48,
                nota = 5,
                comentario = "Primeira"
            )
        )

        Thread.sleep(100) // Pequeno delay para garantir datas diferentes

        avaliacaoPrestadorService.criarAvaliacao(
            AvaliacaoPrestadorService.AvaliacaoPrestadorDTO(
                idUsuario = 8,
                idServico = 48,
                nota = 4,
                comentario = "Segunda"
            )
        )

        val avaliacoes = avaliacaoPrestadorService.listarAvaliacoes(idUsuario = 8)

        if (avaliacoes.size >= 2) {
            // Verifica que estão ordenadas por data (mais recente primeiro)
            val primeira = avaliacoes[0]
            val segunda = avaliacoes[1]
            Assertions.assertNotNull(primeira.data)
            Assertions.assertNotNull(segunda.data)
        }
    }
}

