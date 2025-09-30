package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class PetService(
    private val jdbcTemplate: JdbcTemplate
) {

    fun listarPets(idUsuario: Int): List<Map<String, Any>> {
        return try {
            val sql = "SELECT * FROM pet WHERE id_usuario = ?"
            println("Executando SQL: $sql com idUsuario=$idUsuario")

            val pets = jdbcTemplate.queryForList(sql, idUsuario)
            println("Pets encontrados: ${pets.size}")
            pets
        } catch (e: Exception) {
            println("ERRO ao listar pets: ${e::class.simpleName} - ${e.message}")
            emptyList()
        }
    }

    fun registrarPet(
        idUsuario: Int,
        nome: String,
        especie: String,
        raca: String,
        idade: Int,
        observacoes: String?
    ): Boolean {
        return try {
            val sql = "INSERT INTO pet (id_usuario, nome, especie, raca, idade, observacoes) VALUES (?, ?, ?, ?, ?, ?)"
            println("Executando SQL: $sql")
            println("Valores: idUsuario=$idUsuario, nome=$nome, especie=$especie, raca=$raca, idade=$idade, observacoes=$observacoes")

            val result = jdbcTemplate.update(sql, idUsuario, nome, especie, raca, idade, observacoes)
            println("Resultado: $result linha(s) afetada(s)")
            true
        } catch (e: Exception) {
            println("ERRO ao registrar pet: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            false
        }
    }
}