package backend.auwalk.controller

import backend.auwalk.service.ServiceService
import backend.auwalk.security.JwtUtil
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@RestController
@RequestMapping("/services")
class ServiceController(private val serviceService: ServiceService) {

    @PostMapping
    fun createService(@RequestHeader("Authorization") token: String, @RequestBody request: Map<String, Any>): ResponseEntity<Map<String, Any?>> {
        val idPrestador = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
        if (idPrestador == null) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        val tipoServico = request["tipoServico"] as String
        val descricao = request["descricao"] as String?
        val preco = (request["preco"] as? Double)?.toBigDecimal() ?: BigDecimal.ZERO // Usar Double para garantir toBigDecimal
        val duracaoEstimada = request["duracaoEstimada"] as Int
        val disponibilidades = request["disponibilidades"] as List<Map<String, Any>>

        val newService = serviceService.createService(
            idPrestador,
            tipoServico,
            descricao,
            preco,
            duracaoEstimada,
            disponibilidades
        )
        return ResponseEntity(newService, HttpStatus.CREATED)
    }

    @GetMapping
    fun getServicesByPrestador(@RequestHeader("Authorization") token: String): ResponseEntity<List<Map<String, Any?>>> {
        val idPrestador = JwtUtil.validateToken(token.substringAfter("Bearer "))?.toIntOrNull()
        if (idPrestador == null) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
        val services = serviceService.getServicesByPrestadorId(idPrestador)
        return ResponseEntity(services, HttpStatus.OK)
    }
}

