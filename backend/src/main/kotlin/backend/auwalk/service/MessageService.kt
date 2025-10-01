package backend.auwalk.service

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime

@Service
class MessageService(
    private val jdbcTemplate: JdbcTemplate
) {

    @Transactional
    fun createMessage(idChat: Int, idRemetente: Int, conteudo: String): Map<String, Any?> {
        val dataEnvio = LocalDateTime.now()
        val sql = """
            INSERT INTO mensagem (id_chat, id_remetente, conteudo, data_envio) 
            VALUES (?, ?, ?, ?) 
            RETURNING id_mensagem, id_chat, id_remetente, conteudo, data_envio
        """
        // Usamos queryForMap para retornar a mensagem completa
        return jdbcTemplate.queryForMap(sql, idChat, idRemetente, conteudo, dataEnvio)
    }

    fun getMessagesByChatId(idChat: Int, idUsuarioLogado: Int): List<Map<String, Any?>> {
        // --- CORREÇÃO AQUI ---
        // A consulta agora usa as colunas corretas 'id_usuario1' e 'id_usuario2'
        val checkUserInChatSql = "SELECT COUNT(*) FROM chat WHERE id_chat = ? AND (id_usuario1 = ? OR id_usuario2 = ?)"

        try {
            val count = jdbcTemplate.queryForObject(checkUserInChatSql, Int::class.java, idChat, idUsuarioLogado, idUsuarioLogado) ?: 0

            if (count == 0) {
                // Se o usuário não está no chat, verificamos se o chat ao menos existe antes de negar o acesso.
                val checkChatExistsSql = "SELECT COUNT(*) FROM chat WHERE id_chat = ?"
                val chatExists = (jdbcTemplate.queryForObject(checkChatExistsSql, Int::class.java, idChat) ?: 0) > 0

                if (!chatExists) {
                    throw NoSuchElementException("Chat com ID $idChat não encontrado.")
                } else {
                    throw IllegalAccessException("Usuário não tem permissão para acessar este chat.")
                }
            }
        } catch (e: EmptyResultDataAccessException) {
            throw NoSuchElementException("Chat com ID $idChat não encontrado.")
        }

        val getMessagesSql = """
            SELECT id_mensagem, id_chat, id_remetente, conteudo, data_envio
            FROM mensagem
            WHERE id_chat = ?
            ORDER BY data_envio ASC
        """
        // Usamos queryForList que é mais direto para mapear os resultados
        return jdbcTemplate.queryForList(getMessagesSql, idChat)
    }
}