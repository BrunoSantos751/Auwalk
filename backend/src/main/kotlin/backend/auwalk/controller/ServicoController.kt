package backend.auwalk.controller

import backend.auwalk.service.ServicoUnificadoService // Importa o novo serviço unificado
import backend.auwalk.service.ServicoDisponivelResponse
import backend.auwalk.security.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

data class ServicoRequest(
    val data: String? = null,
    val tipoServico: String? = null
)

data class SearchResponse(
    val data: String? = null,
    val servicos: List<ServicoDisponivelResponse>
)

@RestController
@RequestMapping
class ServicoUnificadoController(private val servicoService: ServicoUnificadoService) {

    // Endpoint de controller.kt
    @PostMapping("/services")
    fun createService(@RequestHeader("Authorization") token: String, @RequestBody request: Map<String, Any>): ResponseEntity<Map<String, Any?>> {
        val idPrestador = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
            ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val tipoServico = request["tipoServico"] as String
        val descricao = request["descricao"] as String?
        val preco = (request["preco"] as? Number)?.toDouble()?.toBigDecimal() ?: BigDecimal.ZERO
        val duracaoEstimada = request["duracaoEstimada"] as Int
        val disponibilidades = request["disponibilidades"] as List<Map<String, Any>>

        val newService = servicoService.createService(
            idPrestador,
            tipoServico,
            descricao,
            preco,
            duracaoEstimada,
            disponibilidades
        )
        return ResponseEntity(newService, HttpStatus.CREATED)
    }

    // Endpoint de controller.kt
    @GetMapping("/services")
    fun getServicesByPrestador(@RequestHeader("Authorization") token: String): ResponseEntity<List<Map<String, Any?>>> {
        val idPrestador = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
            ?: return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val services = servicoService.getServicesByPrestadorId(idPrestador)
        return ResponseEntity(services, HttpStatus.OK)
    }

    @PostMapping("/search")
    fun buscarServicos(@RequestBody request: ServicoRequest?): Map<String, Any> {
        return try {
            val dataConvertida = request?.data?.let { java.time.LocalDate.parse(it) }
            val servicos: List<ServicoDisponivelResponse> =
                servicoService.buscarServicosDisponiveis(dataConvertida, request?.tipoServico)

            val response = SearchResponse(
                data = request?.data,
                servicos = servicos
            )
            mapOf("success" to true, "data" to response)
        } catch (e: Exception) {
            mapOf("success" to false, "message" to "Erro ao buscar serviços: ${e.message}")
        }
    }
}