package backend.auwalk.Controller

import backend.auwalk.service.ServicoUnificadoService
import backend.auwalk.service.ServicoDisponivelResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

// --- Data Classes para as requisições e respostas deste controller ---

data class ServicoRequest(
    val data: String? = null,
    val tipoServico: String? = null
)

data class SearchResponse(
    val data: String? = null,
    val servicos: List<ServicoDisponivelResponse>
)

data class DisponibilidadeRequest(
    val inicioHorarioAtendimento: String,
    val fimHorarioAtendimento: String
)

data class CreateServiceRequest(
    val idPrestador: Int,
    val tipoServico: String,
    val descricao: String?,
    val preco: BigDecimal,
    val duracaoEstimada: Int,
    val disponibilidades: List<DisponibilidadeRequest>
)

@RestController
class ServicoUnificadoController(private val servicoService: ServicoUnificadoService) {

    @PostMapping("/services")
    fun createService(@RequestBody request: CreateServiceRequest): ResponseEntity<Map<String, Any?>> {
        val newService = servicoService.createService(
            idPrestador = request.idPrestador,
            tipoServico = request.tipoServico,
            descricao = request.descricao,
            preco = request.preco,
            duracaoEstimada = request.duracaoEstimada,
            disponibilidades = request.disponibilidades
        )
        return ResponseEntity(mapOf("success" to true, "data" to newService), HttpStatus.CREATED)
    }

    @GetMapping("/services")
    fun getServicesByPrestador(@RequestParam idPrestador: Int): ResponseEntity<Any> {
        val services = servicoService.getServicesByPrestadorId(idPrestador)
        return ResponseEntity.ok(mapOf("success" to true, "data" to services))
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
