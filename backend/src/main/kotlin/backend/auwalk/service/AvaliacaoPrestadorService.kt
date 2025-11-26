package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AvaliacaoPrestadorService(private val jdbcTemplate: JdbcTemplate) {

    // DTO interno
    data class AvaliacaoPrestadorDTO(
        val idAvaliacao: Int? = null,
        val idUsuario: Int,
        val idServico: Int,
        val nota: Int,
        val comentario: String? = null,
        val data: LocalDateTime? = null
    )

    // Criar avaliação
    fun criarAvaliacao(req: AvaliacaoPrestadorDTO): Map<String, Any> {
        val sql = """
            INSERT INTO avaliacao_prestador (
                id_usuario, id_servico, nota, comentario, data
            ) VALUES (
                ?, ?, ?, ?, COALESCE(?, NOW())
            )
            RETURNING id_avaliacao, id_usuario, id_servico, nota, comentario, data
        """.trimIndent()

        return jdbcTemplate.queryForMap(
            sql,
            req.idUsuario,
            req.idServico,
            req.nota,
            req.comentario,
            req.data
        )
    }

    // Listar avaliações (todas ou por usuário ou por serviço)
    fun listarAvaliacoes(idUsuario: Int? = null, idServico: Int? = null): List<AvaliacaoPrestadorDTO> {
        val sql = when {
            idUsuario != null && idServico != null -> {
                """
                SELECT 
                    id_avaliacao, id_usuario, id_servico, nota, comentario, data
                FROM avaliacao_prestador
                WHERE id_usuario = ? AND id_servico = ?
                ORDER BY data DESC
                """.trimIndent()
            }
            idUsuario != null -> {
                """
                SELECT 
                    id_avaliacao, id_usuario, id_servico, nota, comentario, data
                FROM avaliacao_prestador
                WHERE id_usuario = ?
                ORDER BY data DESC
                """.trimIndent()
            }
            idServico != null -> {
                """
                SELECT 
                    id_avaliacao, id_usuario, id_servico, nota, comentario, data
                FROM avaliacao_prestador
                WHERE id_servico = ?
                ORDER BY data DESC
                """.trimIndent()
            }
            else -> {
                """
                SELECT 
                    id_avaliacao, id_usuario, id_servico, nota, comentario, data
                FROM avaliacao_prestador
                ORDER BY data DESC
                """.trimIndent()
            }
        }

        return when {
            idUsuario != null && idServico != null -> {
                jdbcTemplate.query(sql, arrayOf(idUsuario, idServico)) { rs, _ ->
                    AvaliacaoPrestadorDTO(
                        idAvaliacao = rs.getInt("id_avaliacao"),
                        idUsuario = rs.getInt("id_usuario"),
                        idServico = rs.getInt("id_servico"),
                        nota = rs.getInt("nota"),
                        comentario = rs.getString("comentario"),
                        data = rs.getTimestamp("data")?.toLocalDateTime()
                    )
                }
            }
            idUsuario != null -> {
                jdbcTemplate.query(sql, arrayOf(idUsuario)) { rs, _ ->
                    AvaliacaoPrestadorDTO(
                        idAvaliacao = rs.getInt("id_avaliacao"),
                        idUsuario = rs.getInt("id_usuario"),
                        idServico = rs.getInt("id_servico"),
                        nota = rs.getInt("nota"),
                        comentario = rs.getString("comentario"),
                        data = rs.getTimestamp("data")?.toLocalDateTime()
                    )
                }
            }
            idServico != null -> {
                jdbcTemplate.query(sql, arrayOf(idServico)) { rs, _ ->
                    AvaliacaoPrestadorDTO(
                        idAvaliacao = rs.getInt("id_avaliacao"),
                        idUsuario = rs.getInt("id_usuario"),
                        idServico = rs.getInt("id_servico"),
                        nota = rs.getInt("nota"),
                        comentario = rs.getString("comentario"),
                        data = rs.getTimestamp("data")?.toLocalDateTime()
                    )
                }
            }
            else -> {
                jdbcTemplate.query(sql) { rs, _ ->
                    AvaliacaoPrestadorDTO(
                        idAvaliacao = rs.getInt("id_avaliacao"),
                        idUsuario = rs.getInt("id_usuario"),
                        idServico = rs.getInt("id_servico"),
                        nota = rs.getInt("nota"),
                        comentario = rs.getString("comentario"),
                        data = rs.getTimestamp("data")?.toLocalDateTime()
                    )
                }
            }
        }
    }

    // Buscar avaliação por ID
    fun buscarAvaliacaoPorId(id: Int): AvaliacaoPrestadorDTO? {
        val sql = """
            SELECT 
                id_avaliacao, id_usuario, id_servico, nota, comentario, data
            FROM avaliacao_prestador
            WHERE id_avaliacao = ?
        """.trimIndent()

        return try {
            jdbcTemplate.queryForObject(sql, arrayOf(id)) { rs, _ ->
                AvaliacaoPrestadorDTO(
                    idAvaliacao = rs.getInt("id_avaliacao"),
                    idUsuario = rs.getInt("id_usuario"),
                    idServico = rs.getInt("id_servico"),
                    nota = rs.getInt("nota"),
                    comentario = rs.getString("comentario"),
                    data = rs.getTimestamp("data")?.toLocalDateTime()
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    // Atualizar avaliação
    fun atualizarAvaliacao(id: Int, req: AvaliacaoPrestadorDTO): Int {
        val sql = """
            UPDATE avaliacao_prestador SET
                nota = ?, comentario = ?
            WHERE id_avaliacao = ?
        """.trimIndent()

        return jdbcTemplate.update(
            sql,
            req.nota,
            req.comentario,
            id
        )
    }

    // Deletar avaliação
    fun deletarAvaliacao(id: Int) {
        jdbcTemplate.update("DELETE FROM avaliacao_prestador WHERE id_avaliacao = ?", id)
    }

    // Calcular nota média por usuário (prestador)
    fun calcularNotaMediaPorUsuario(idUsuario: Int? = null): Map<String, Any> {
        val sql = if (idUsuario != null) {
            """
            SELECT 
                COALESCE(AVG(nota), 0) AS nota_media,
                COUNT(*) AS total_avaliacoes
            FROM avaliacao_prestador
            WHERE id_usuario = ?
            """.trimIndent()
        } else {
            """
            SELECT 
                COALESCE(AVG(nota), 0) AS nota_media,
                COUNT(*) AS total_avaliacoes
            FROM avaliacao_prestador
            """.trimIndent()
        }

        return if (idUsuario != null) {
            jdbcTemplate.queryForMap(sql, idUsuario)
        } else {
            jdbcTemplate.queryForMap(sql)
        }
    }

    // Calcular nota média por serviço
    fun calcularNotaMediaPorServico(idServico: Int): Map<String, Any> {
        val sql = """
            SELECT 
                COALESCE(AVG(nota), 0) AS nota_media,
                COUNT(*) AS total_avaliacoes
            FROM avaliacao_prestador
            WHERE id_servico = ?
        """.trimIndent()

        return jdbcTemplate.queryForMap(sql, idServico)
    }
}

