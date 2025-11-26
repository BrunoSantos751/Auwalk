package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AvaliacaoPetService(private val jdbcTemplate: JdbcTemplate) {

    // DTO interno
    data class AvaliacaoPetDTO(
        val idAvaliacao: Int? = null,
        val idPet: Int,
        val idServico: Int,
        val nota: Int,
        val comentario: String? = null,
        val data: LocalDateTime? = null
    )

    // Criar avaliação
    fun criarAvaliacao(req: AvaliacaoPetDTO): Map<String, Any> {
        val sql = """
            INSERT INTO avaliacao_pet (
                id_pet, id_servico, nota, comentario, data
            ) VALUES (
                ?, ?, ?, ?, COALESCE(?, NOW())
            )
            RETURNING id_avaliacao, id_pet, id_servico, nota, comentario, data
        """.trimIndent()

        return jdbcTemplate.queryForMap(
            sql,
            req.idPet,
            req.idServico,
            req.nota,
            req.comentario,
            req.data
        )
    }

    // Listar avaliações (todas ou por pet ou por serviço)
    fun listarAvaliacoes(idPet: Int? = null, idServico: Int? = null): List<AvaliacaoPetDTO> {
        val sql = when {
            idPet != null && idServico != null -> {
                """
                SELECT 
                    id_avaliacao, id_pet, id_servico, nota, comentario, data
                FROM avaliacao_pet
                WHERE id_pet = ? AND id_servico = ?
                ORDER BY data DESC
                """.trimIndent()
            }
            idPet != null -> {
                """
                SELECT 
                    id_avaliacao, id_pet, id_servico, nota, comentario, data
                FROM avaliacao_pet
                WHERE id_pet = ?
                ORDER BY data DESC
                """.trimIndent()
            }
            idServico != null -> {
                """
                SELECT 
                    id_avaliacao, id_pet, id_servico, nota, comentario, data
                FROM avaliacao_pet
                WHERE id_servico = ?
                ORDER BY data DESC
                """.trimIndent()
            }
            else -> {
                """
                SELECT 
                    id_avaliacao, id_pet, id_servico, nota, comentario, data
                FROM avaliacao_pet
                ORDER BY data DESC
                """.trimIndent()
            }
        }

        return when {
            idPet != null && idServico != null -> {
                jdbcTemplate.query(sql, arrayOf(idPet, idServico)) { rs, _ ->
                    AvaliacaoPetDTO(
                        idAvaliacao = rs.getInt("id_avaliacao"),
                        idPet = rs.getInt("id_pet"),
                        idServico = rs.getInt("id_servico"),
                        nota = rs.getInt("nota"),
                        comentario = rs.getString("comentario"),
                        data = rs.getTimestamp("data")?.toLocalDateTime()
                    )
                }
            }
            idPet != null -> {
                jdbcTemplate.query(sql, arrayOf(idPet)) { rs, _ ->
                    AvaliacaoPetDTO(
                        idAvaliacao = rs.getInt("id_avaliacao"),
                        idPet = rs.getInt("id_pet"),
                        idServico = rs.getInt("id_servico"),
                        nota = rs.getInt("nota"),
                        comentario = rs.getString("comentario"),
                        data = rs.getTimestamp("data")?.toLocalDateTime()
                    )
                }
            }
            idServico != null -> {
                jdbcTemplate.query(sql, arrayOf(idServico)) { rs, _ ->
                    AvaliacaoPetDTO(
                        idAvaliacao = rs.getInt("id_avaliacao"),
                        idPet = rs.getInt("id_pet"),
                        idServico = rs.getInt("id_servico"),
                        nota = rs.getInt("nota"),
                        comentario = rs.getString("comentario"),
                        data = rs.getTimestamp("data")?.toLocalDateTime()
                    )
                }
            }
            else -> {
                jdbcTemplate.query(sql) { rs, _ ->
                    AvaliacaoPetDTO(
                        idAvaliacao = rs.getInt("id_avaliacao"),
                        idPet = rs.getInt("id_pet"),
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
    fun buscarAvaliacaoPorId(id: Int): AvaliacaoPetDTO? {
        val sql = """
            SELECT 
                id_avaliacao, id_pet, id_servico, nota, comentario, data
            FROM avaliacao_pet
            WHERE id_avaliacao = ?
        """.trimIndent()

        return try {
            jdbcTemplate.queryForObject(sql, arrayOf(id)) { rs, _ ->
                AvaliacaoPetDTO(
                    idAvaliacao = rs.getInt("id_avaliacao"),
                    idPet = rs.getInt("id_pet"),
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
    fun atualizarAvaliacao(id: Int, req: AvaliacaoPetDTO): Int {
        val sql = """
            UPDATE avaliacao_pet SET
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
        jdbcTemplate.update("DELETE FROM avaliacao_pet WHERE id_avaliacao = ?", id)
    }

    // Calcular nota média por pet
    fun calcularNotaMediaPorPet(idPet: Int? = null): Map<String, Any> {
        val sql = if (idPet != null) {
            """
            SELECT 
                COALESCE(AVG(nota), 0) AS nota_media,
                COUNT(*) AS total_avaliacoes
            FROM avaliacao_pet
            WHERE id_pet = ?
            """.trimIndent()
        } else {
            """
            SELECT 
                COALESCE(AVG(nota), 0) AS nota_media,
                COUNT(*) AS total_avaliacoes
            FROM avaliacao_pet
            """.trimIndent()
        }

        return if (idPet != null) {
            jdbcTemplate.queryForMap(sql, idPet)
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
            FROM avaliacao_pet
            WHERE id_servico = ?
        """.trimIndent()

        return jdbcTemplate.queryForMap(sql, idServico)
    }
}

