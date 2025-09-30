package backend.auwalk.controller

import backend.auwalk.service.ServicoService
import backend.auwalk.service.ServicoDisponivelResponse
import org.springframework.web.bind.annotation.*

data class ServicoRequest(
    val data: String? = null,         // opcional
    val tipoServico: String? = null   // opcional
)

data class SearchResponse(
    val data: String? = null,
    val servicos: List<ServicoDisponivelResponse>
)

@RestController
@RequestMapping("/search")
class ServicoController(
    private val servicoService: ServicoService
) {

    @PostMapping
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
