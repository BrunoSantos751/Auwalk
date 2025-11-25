package backend.auwalk.controller

import backend.auwalk.service.AvaliacaoPetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/avaliacoes-pet")
class AvaliacaoPetController(private val avaliacaoPetService: AvaliacaoPetService) {

    @PostMapping
    fun criarAvaliacao(@RequestBody request: AvaliacaoPetService.AvaliacaoPetDTO): ResponseEntity<Any> {
        return try {
            val avaliacao = avaliacaoPetService.criarAvaliacao(request)
            ResponseEntity.status(HttpStatus.CREATED).body(avaliacao)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to e.message))
        }
    }

    @GetMapping
    fun listarAvaliacoes(
        @RequestParam(required = false) idPet: Int?,
        @RequestParam(required = false) idServico: Int?
    ): ResponseEntity<Any> {
        val avaliacoes = avaliacaoPetService.listarAvaliacoes(idPet, idServico)
        return ResponseEntity.ok(avaliacoes)
    }

    @GetMapping("/{id}")
    fun buscarAvaliacaoPorId(@PathVariable id: Int): ResponseEntity<Any> {
        val avaliacao = avaliacaoPetService.buscarAvaliacaoPorId(id)
        return if (avaliacao != null) {
            ResponseEntity.ok(avaliacao)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    fun atualizarAvaliacao(
        @PathVariable id: Int,
        @RequestBody request: AvaliacaoPetService.AvaliacaoPetDTO
    ): ResponseEntity<Any> {
        return try {
            val atualizado = avaliacaoPetService.atualizarAvaliacao(id, request)
            if (atualizado > 0) {
                val avaliacao = avaliacaoPetService.buscarAvaliacaoPorId(id)
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
        avaliacaoPetService.deletarAvaliacao(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/media")
    fun calcularNotaMedia(
        @RequestParam(required = false) idPet: Int?,
        @RequestParam(required = false) idServico: Int?
    ): ResponseEntity<Any> {
        return try {
            val resultado = when {
                idPet != null -> avaliacaoPetService.calcularNotaMediaPorPet(idPet)
                idServico != null -> avaliacaoPetService.calcularNotaMediaPorServico(idServico)
                else -> avaliacaoPetService.calcularNotaMediaPorPet()
            }
            ResponseEntity.ok(resultado)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("success" to false, "message" to e.message))
        }
    }
}

