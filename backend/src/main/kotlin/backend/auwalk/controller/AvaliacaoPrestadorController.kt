package backend.auwalk.controller

import backend.auwalk.service.AvaliacaoPrestadorService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/avaliacoes-prestador")
class AvaliacaoPrestadorController(private val avaliacaoPrestadorService: AvaliacaoPrestadorService) {

    @PostMapping
    fun criarAvaliacao(@RequestBody request: AvaliacaoPrestadorService.AvaliacaoPrestadorDTO): ResponseEntity<Any> {
        return try {
            val avaliacao = avaliacaoPrestadorService.criarAvaliacao(request)
            ResponseEntity.status(HttpStatus.CREATED).body(avaliacao)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to e.message))
        }
    }

    @GetMapping
    fun listarAvaliacoes(
        @RequestParam(required = false) idUsuario: Int?,
        @RequestParam(required = false) idServico: Int?
    ): ResponseEntity<Any> {
        val avaliacoes = avaliacaoPrestadorService.listarAvaliacoes(idUsuario, idServico)
        return ResponseEntity.ok(avaliacoes)
    }

    @GetMapping("/{id}")
    fun buscarAvaliacaoPorId(@PathVariable id: Int): ResponseEntity<Any> {
        val avaliacao = avaliacaoPrestadorService.buscarAvaliacaoPorId(id)
        return if (avaliacao != null) {
            ResponseEntity.ok(avaliacao)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    fun atualizarAvaliacao(
        @PathVariable id: Int,
        @RequestBody request: AvaliacaoPrestadorService.AvaliacaoPrestadorDTO
    ): ResponseEntity<Any> {
        return try {
            val atualizado = avaliacaoPrestadorService.atualizarAvaliacao(id, request)
            if (atualizado > 0) {
                val avaliacao = avaliacaoPrestadorService.buscarAvaliacaoPorId(id)
                ResponseEntity.ok(avaliacao)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to e.message))
        }
    }

    @DeleteMapping("/{id}")
    fun deletarAvaliacao(@PathVariable id: Int): ResponseEntity<Void> {
        avaliacaoPrestadorService.deletarAvaliacao(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/media")
    fun calcularNotaMedia(
        @RequestParam(required = false) idUsuario: Int?,
        @RequestParam(required = false) idServico: Int?
    ): ResponseEntity<Any> {
        return try {
            val resultado = when {
                idUsuario != null -> avaliacaoPrestadorService.calcularNotaMediaPorUsuario(idUsuario)
                idServico != null -> avaliacaoPrestadorService.calcularNotaMediaPorServico(idServico)
                else -> avaliacaoPrestadorService.calcularNotaMediaPorUsuario()
            }
            ResponseEntity.ok(resultado)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("success" to false, "message" to e.message))
        }
    }
}

