package backend.auwalk.controller

import backend.auwalk.service.ProviderService
import backend.auwalk.security.JwtUtil // Certifique-se que o caminho para seu JwtUtil está correto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

data class ProviderProfileRequest(
    val idUsuario: Int,
    val bio: String?,
    val experiencia: String?,
    val documento: String
)

@RestController
@RequestMapping("/provider")
class ProviderController(
    private val providerService: ProviderService
) {

    @GetMapping("/profile")
    fun visualizarPerfil(@RequestParam idUsuario: Int): ResponseEntity<Map<String, Any>> {
        val perfil = providerService.buscarPerfil(idUsuario)
        return if (perfil != null) {
            ResponseEntity.ok(mapOf("success" to true, "data" to perfil))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Perfil não encontrado"))
        }
    }

    @PutMapping("/profile")
    fun editarPerfil(@RequestBody request: ProviderProfileRequest): ResponseEntity<Map<String, Any>> {
        println("Controller recebeu a requisição para o idUsuario: ${request.idUsuario}")
        if (request.documento.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "O campo documento é obrigatório"))
        }

        val sucesso = providerService.editarOuInserirPerfil(request.idUsuario, request.bio, request.experiencia, request.documento)
        return if (sucesso) {
            ResponseEntity.ok(mapOf("success" to true, "message" to "Perfil atualizado com sucesso"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "Erro ao atualizar perfil"))
        }
    }
    @GetMapping("/profile/me")
    fun checkMyProfile(@RequestHeader("Authorization") token: String): ResponseEntity<Map<String, Any>> {
        try {
            val idUsuario = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()

            if (idUsuario == null) {
                return ResponseEntity.ok(mapOf("isPrestador" to false, "message" to "Token inválido."))
            }

            val perfil = providerService.buscarPerfil(idUsuario)
            if (perfil != null) {
                return ResponseEntity.ok(mapOf("isPrestador" to true, "data" to perfil))
            } else {
                return ResponseEntity.ok(mapOf("isPrestador" to false))
            }
        } catch (e: Exception) {
            println("Erro ao verificar perfil do prestador: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("isPrestador" to false, "message" to "Erro interno ao verificar perfil."))
        }
    }
}