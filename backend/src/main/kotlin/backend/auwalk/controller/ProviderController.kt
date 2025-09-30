package backend.auwalk.controller

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import backend.auwalk.service.ProviderService

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
        // Validação: documento é obrigatório
        if (request.documento.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "O campo documento é obrigatório"))
        }

        val sucesso = providerService.editarPerfil(request.idUsuario, request.bio, request.experiencia, request.documento)
        return if (sucesso) {
            ResponseEntity.ok(mapOf("success" to true, "message" to "Perfil atualizado com sucesso"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "Erro ao atualizar perfil"))
        }
    }
}