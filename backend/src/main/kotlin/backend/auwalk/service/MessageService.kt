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
            RETURNING id_mensagem
        """
        
        val messageId = jdbcTemplate.queryForObject(sql, Int::class.java,
            idChat,
            idRemetente,
            conteudo,
            dataEnvio
        )

        return mapOf(
            "idMensagem" to messageId,
            "idChat" to idChat,
            "idRemetente" to idRemetente,
            "conteudo" to conteudo,
            "dataEnvio" to dataEnvio
        )
    }

    fun getMessagesByChatId(idChat: Int, idUsuarioLogado: Int): List<Map<String, Any?>> {
        val checkUserInChatSql = "SELECT COUNT(*) FROM chat WHERE id_chat = ? AND (id_prestador = ? OR id_cliente = ?)"
        
        try {
            // ALTERADO: Adicionado o operador Elvis (?: 0) para tratar o caso de retorno nulo.
            val count = jdbcTemplate.queryForObject(checkUserInChatSql, Int::class.java, idChat, idUsuarioLogado, idUsuarioLogado) ?: 0
            
            if (count == 0) {
                val checkChatExistsSql = "SELECT COUNT(*) FROM chat WHERE id_chat = ?"
                // ALTERADO: Adicionado o operador Elvis (?: 0) aqui também.
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

        val messages = mutableListOf<Map<String, Any?>>()
        jdbcTemplate.query(getMessagesSql, { rs: ResultSet, _ ->
            messages.add(mapOf(
                "idMensagem" to rs.getInt("id_mensagem"),
                "idChat" to rs.getInt("id_chat"),
                "idRemetente" to rs.getInt("id_remetente"),
                "conteudo" to rs.getString("conteudo"),
                "dataEnvio" to rs.getObject("data_envio", LocalDateTime::class.java)
            ))
        }, idChat)

        return messages
    }
}