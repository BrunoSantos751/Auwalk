package backend.auwalk.controller

import backend.auwalk.service.EnderecoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/enderecos")
class EnderecoController(private val enderecoService: EnderecoService) {

    @PostMapping
    fun criarEndereco(@RequestBody request: EnderecoService.EnderecoDTO): ResponseEntity<Any> {
        val endereco = enderecoService.criarEndereco(request)
        return ResponseEntity.ok(endereco)
    }

    @GetMapping
    fun listarEnderecos(@RequestParam(required = false) idUsuario: Int?): ResponseEntity<Any> {
        val enderecos = enderecoService.listarEnderecos(idUsuario)
        return ResponseEntity.ok(enderecos)
    }

    @PutMapping("/{id}")
    fun atualizarEndereco(
        @PathVariable id: Int,
        @RequestBody request: EnderecoService.EnderecoDTO
    ): ResponseEntity<Any> {
        val atualizado = enderecoService.atualizarEndereco(id, request)
        return ResponseEntity.ok(atualizado)
    }

    @DeleteMapping("/{id}")
    fun deletarEndereco(@PathVariable id: Int): ResponseEntity<Void> {
        enderecoService.deletarEndereco(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/proximos")
    fun buscarPasseadoresProximos(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(defaultValue = "5000") raioMetros: Int
    ): ResponseEntity<Any> {
        val proximos = enderecoService.buscarPasseadoresProximos(latitude, longitude, raioMetros)
        return ResponseEntity.ok(proximos)
    }
}
