package backend.auwalk.controller

import backend.auwalk.service.TrajetoService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/trajetos")
class TrajetoController(private val trajetoService: TrajetoService) {

    @PostMapping
    fun criarTrajeto(@RequestBody request: TrajetoService.TrajetoDTO): ResponseEntity<Any> {
        val trajeto = trajetoService.criarTrajeto(request)
        return ResponseEntity.ok(trajeto)
    }

    @PostMapping("/lote")
    fun criarTrajetos(@RequestBody requests: List<TrajetoService.TrajetoDTO>): ResponseEntity<Any> {
        val trajetos = trajetoService.criarTrajetos(requests)
        return ResponseEntity.ok(trajetos)
    }

    @GetMapping
    fun listarTrajetos(@RequestParam(required = false) idPasseio: Int?): ResponseEntity<Any> {
        val trajetos = trajetoService.listarTrajetos(idPasseio)
        return ResponseEntity.ok(trajetos)
    }

    @PutMapping("/{id}")
    fun atualizarTrajeto(
        @PathVariable id: Int,
        @RequestBody request: TrajetoService.TrajetoDTO
    ): ResponseEntity<Any> {
        val atualizado = trajetoService.atualizarTrajeto(id, request)
        return ResponseEntity.ok(atualizado)
    }

    @DeleteMapping("/{id}")
    fun deletarTrajeto(@PathVariable id: Int): ResponseEntity<Void> {
        trajetoService.deletarTrajeto(id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/passeio/{idPasseio}")
    fun deletarTrajetosPorPasseio(@PathVariable idPasseio: Int): ResponseEntity<Void> {
        trajetoService.deletarTrajetosPorPasseio(idPasseio)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/simplificar/{idPasseio}")
    fun simplificarTrajeto(
        @PathVariable idPasseio: Int,
        @RequestParam(defaultValue = "2.5") epsilonMetros: Double
    ): ResponseEntity<Any> {
        return try {
            val resultado = trajetoService.simplificarTrajeto(idPasseio, epsilonMetros)
            ResponseEntity.ok(resultado)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to e.message))
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("success" to false, "message" to e.message))
        }
    }

    @GetMapping("/simplificado/{idPasseio}")
    fun buscarTrajetoSimplificado(@PathVariable idPasseio: Int): ResponseEntity<Any> {
        val trajeto = trajetoService.buscarTrajetoSimplificado(idPasseio)
        return if (trajeto != null) {
            ResponseEntity.ok(trajeto)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

