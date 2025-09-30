package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class ProviderService(
    private val jdbcTemplate: JdbcTemplate
) {

    fun buscarPerfil(idUsuario: Int): Map<String, Any>? {
        return try {
            val sql = "SELECT * FROM prestador_servico WHERE id_usuario = ?"
            println("Executando SQL: $sql com idUsuario=$idUsuario")

            val perfil = jdbcTemplate.queryForMap(sql, idUsuario)
            println("Perfil encontrado: $perfil")
            perfil
        } catch (e: Exception) {
            println("ERRO ao buscar perfil: ${e::class.simpleName} - ${e.message}")
            null
        }
    }

    fun editarPerfil(idUsuario: Int, bio: String?, experiencia: String?, documento: String): Boolean {
        return try {
            val sql = """
                UPDATE prestador_servico 
                SET bio = ?, experiencia = ?, documento = ?
                WHERE id_usuario = ?
            """
            println("Executando SQL: $sql")
            println("Valores: bio=$bio, experiencia=$experiencia, documento=$documento, idUsuario=$idUsuario")

            val result = jdbcTemplate.update(sql, bio, experiencia, documento, idUsuario)
            println("Resultado: $result linha(s) afetada(s)")

            result > 0
        } catch (e: Exception) {
            println("ERRO ao editar perfil: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            false
        }
    }
}