package backend.auwalk.controller

import backend.auwalk.service.AgendamentoService
import com.fasterxml.jackson.annotation.JsonFormat // <-- 1. NOVO IMPORT
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import backend.auwalk.security.JwtUtil

data class AgendamentoSlotRequest(
    val idCliente: Int,
    val idServico: Int,
    val idPet: Int,

    // 2. NOVA ANOTAÇÃO: Informa ao backend para aceitar o formato sem segundos.
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    val dataHora: LocalDateTime,

    val observacoes: String? = null
)

@RestController
@RequestMapping("/schedule")
class AgendamentoController(
    private val agendamentoService: AgendamentoService
) {

    @PostMapping("/walk")
    fun agendarPasseio(
        @RequestBody request: AgendamentoSlotRequest
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val novoAgendamento = agendamentoService.criarAgendamentoPasseio(
                idCliente = request.idCliente,
                idServico = request.idServico,
                idPet = request.idPet,
                dataHora = request.dataHora
            )
            ResponseEntity.status(HttpStatus.CREATED)
                .body(mapOf("success" to true, "data" to novoAgendamento))

        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("success" to false, "message" to "Ocorreu um erro inesperado: ${e.message}"))
        }
    }

    @PostMapping("/sitter")
    fun agendarPetSitting(
        @RequestBody request: AgendamentoSlotRequest
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val novoAgendamento = agendamentoService.criarAgendamentoSitter(
                idCliente = request.idCliente,
                idServico = request.idServico,
                idPet = request.idPet,
                dataHora = request.dataHora,
                observacoes = request.observacoes
            )
            ResponseEntity.status(HttpStatus.CREATED)
                .body(mapOf("success" to true, "data" to novoAgendamento))

        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("success" to false, "message" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("success" to false, "message" to "Ocorreu um erro inesperado: ${e.message}"))
        }
    }
    @GetMapping("/my-appointments/client")
    fun getAgendamentosComoCliente(@RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        return try {
            val idUsuario = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("success" to false, "message" to "Token inválido"))

            val agendamentos = agendamentoService.buscarComoCliente(idUsuario)
            ResponseEntity.ok(mapOf("success" to true, "data" to agendamentos))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("success" to false, "message" to e.message))
        }
    }

    @GetMapping("/my-appointments/provider")
    fun getAgendamentosComoPrestador(@RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        return try {
            val idUsuario = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("success" to false, "message" to "Token inválido"))

            val agendamentos = agendamentoService.buscarComoPrestador(idUsuario)
            ResponseEntity.ok(mapOf("success" to true, "data" to agendamentos))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("success" to false, "message" to e.message))
        }
    }
}
