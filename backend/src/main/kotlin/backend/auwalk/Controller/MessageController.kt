package backend.auwalk.Controller

import backend.auwalk.service.MessageService
import backend.auwalk.security.JwtUtil
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/mensagens")
class MessageController(private val messageService: MessageService) {

    /**
     * Endpoint para criar uma nova mensagem em um chat.
     */
    @PostMapping
    fun createMessage(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: Map<String, Any>
    ): ResponseEntity<Map<String, Any?>> {
        val idRemetente = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
            ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val idChat = request["id_chat"] as? Int
        val conteudo = request["conteudo"] as? String

        if (idChat == null || conteudo.isNullOrBlank()) {
            return ResponseEntity
                .badRequest()
                .body(mapOf("erro" to "Os campos 'id_chat' e 'conteudo' são obrigatórios."))
        }

        val novaMensagem = messageService.createMessage(idChat, idRemetente, conteudo)
        return ResponseEntity(novaMensagem, HttpStatus.CREATED)
    }

    /**
     * Endpoint para listar todas as mensagens de um chat específico.
     */
    @GetMapping("/{id_chat}")
    fun getMessagesByChat(
        @RequestHeader("Authorization") token: String,
        @PathVariable("id_chat") idChat: Int
    ): ResponseEntity<Any> {
        val idUsuarioLogado = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
            ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)
        
        return try {
            val mensagens = messageService.getMessagesByChatId(idChat, idUsuarioLogado)
            ResponseEntity.ok(mensagens)
        } catch (e: NoSuchElementException) {
            // Retorna 404 Not Found se o chat não existir.
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("erro" to e.message))
        } catch (e: IllegalAccessException) {
            // Retorna 403 Forbidden se o usuário não tiver permissão.
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("erro" to e.message))
        } catch (e: Exception) {
            // Retorna 500 para outros erros inesperados.
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("erro" to "Ocorreu um erro interno."))
        }
    }
}