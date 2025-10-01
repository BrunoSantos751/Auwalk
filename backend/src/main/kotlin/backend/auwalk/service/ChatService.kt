package backend.auwalk.service

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ChatService(private val jdbcTemplate: JdbcTemplate) {

    @Transactional
    fun createOrGetChat(idUsuario1: Int, idUsuario2: Int): Map<String, Any?> {
        val user1 = minOf(idUsuario1, idUsuario2)
        val user2 = maxOf(idUsuario1, idUsuario2)

        val findSql = "SELECT * FROM chat WHERE id_usuario1 = ? AND id_usuario2 = ?"

        return try {
            jdbcTemplate.queryForMap(findSql, user1, user2)
        } catch (e: EmptyResultDataAccessException) {
            val insertSql = "INSERT INTO chat (id_usuario1, id_usuario2, data_inicio) VALUES (?, ?, ?) RETURNING id_chat, id_usuario1, id_usuario2, data_inicio"
            jdbcTemplate.queryForMap(insertSql, user1, user2, LocalDateTime.now())
        }
    }

    fun getChatsByUserId(idUsuario: Int): List<Map<String, Any?>> {
        val sql = """
            SELECT 
                c.id_chat,
                c.data_inicio,
                -- Lógica para identificar o "outro" usuário no chat
                CASE WHEN c.id_usuario1 = ? THEN u2.nome ELSE u1.nome END as nome_contraparte,
                CASE WHEN c.id_usuario1 = ? THEN c.id_usuario2 ELSE c.id_usuario1 END as id_contraparte
            FROM chat c
            JOIN usuario u1 ON c.id_usuario1 = u1.id_usuario
            JOIN usuario u2 ON c.id_usuario2 = u2.id_usuario
            WHERE c.id_usuario1 = ? OR c.id_usuario2 = ?
            ORDER BY c.data_inicio DESC
        """
        return jdbcTemplate.queryForList(sql, idUsuario, idUsuario, idUsuario, idUsuario)
    }
}