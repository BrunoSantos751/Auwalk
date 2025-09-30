package backend.auwalk.controller

import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import backend.auwalk.service.PetService
import org.springframework.web.bind.annotation.PathVariable

data class PetRequest(
    val idUsuario: Int,
    val nome: String,
    val especie: String,
    val raca: String,
    val idade: Int,
    val observacoes: String?
)

@RestController
@RequestMapping("/pets")
class PetController(
    private val petService: PetService
) {

    @GetMapping
    fun listarPets(@RequestParam idUsuario: Int): ResponseEntity<Map<String, Any>> {
        val pets = petService.listarPets(idUsuario)
        return if (pets.isNotEmpty()) {
            ResponseEntity.ok(mapOf("success" to true, "data" to pets))
        } else {
            ResponseEntity.ok(mapOf("success" to true, "data" to emptyList<Any>(), "message" to "Nenhum pet encontrado"))
        }
    }

    @PostMapping("/register")
    fun registrarPet(@RequestBody request: PetRequest): ResponseEntity<Map<String, Any>> {
        // Validação: campos obrigatórios
        if (request.nome.isBlank() || request.especie.isBlank() || request.raca.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "Os campos nome, espécie e raça são obrigatórios"))
        }

        // Validação: idade positiva
        if (request.idade <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "A idade deve ser um valor positivo"))
        }

        // Validação: espécie válida
        val especiesValidas = listOf("cachorro", "gato", "ave", "roedor", "réptil", "outro")
        if (!especiesValidas.contains(request.especie.lowercase())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "Espécie inválida. Espécies válidas: ${especiesValidas.joinToString(", ")}"))
        }

        val sucesso = petService.registrarPet(
            request.idUsuario,
            request.nome,
            request.especie,
            request.raca,
            request.idade,
            request.observacoes
        )

        return if (sucesso) {
            ResponseEntity.status(HttpStatus.CREATED)
                .body(mapOf("success" to true, "message" to "Pet registrado com sucesso"))
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "Erro ao registrar pet no banco de dados"))
        }
    }
    @GetMapping("/{id}")
    fun buscarPetPorId(@PathVariable id: Int): ResponseEntity<Map<String, Any>> {
        val pet = petService.buscarPetPorId(id)
        return if (pet != null) {
            ResponseEntity.ok(mapOf("success" to true, "data" to pet))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Pet não encontrado"))
        }
    }
    @PutMapping("/update/{id}")
    fun updatePet(@PathVariable id: Int, @RequestBody request: PetRequest): ResponseEntity<Map<String, Any>> {
        val sucesso = petService.updatePet(
            id,
            request.nome,
            request.especie,
            request.raca,
            request.idade,
            request.observacoes
        )

        return if (sucesso) {
            ResponseEntity.ok(mapOf("success" to true, "message" to "Pet atualizado com sucesso"))
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("success" to false, "message" to "Erro ao atualizar pet no banco de dados"))
        }
    }
}