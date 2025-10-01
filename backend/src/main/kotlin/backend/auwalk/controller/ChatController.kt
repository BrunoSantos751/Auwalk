package backend.auwalk.controller

import backend.auwalk.service.ChatService
import backend.auwalk.security.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CreateChatRequest(val idDestinatario: Int)

@RestController
@RequestMapping("/chats")
class ChatController(private val chatService: ChatService) {

    /**
     * Endpoint para iniciar um novo chat com outro usuário ou obter o chat existente.
     * O ID do usuário logado vem do token.
     */
    @PostMapping
    fun createOrGetChat(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: CreateChatRequest
    ): ResponseEntity<Any> {
        val idRemetente = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("sucesso" to false, "mensagem" to "Token inválido."))

        return try {
            val chat = chatService.createOrGetChat(idRemetente, request.idDestinatario)
            ResponseEntity.status(HttpStatus.CREATED).body(mapOf("sucesso" to true, "dados" to chat))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("sucesso" to false, "mensagem" to e.message))
        }
    }

    /**
     * Endpoint para listar todos os chats do usuário logado.
     */
    @GetMapping
    fun getChatsForCurrentUser(@RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        val idUsuario = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("sucesso" to false, "mensagem" to "Token inválido."))

        return try {
            val chats = chatService.getChatsByUserId(idUsuario)
            ResponseEntity.ok(mapOf("sucesso" to true, "dados" to chats))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("sucesso" to false, "mensagem" to e.message))
        }
    }
}