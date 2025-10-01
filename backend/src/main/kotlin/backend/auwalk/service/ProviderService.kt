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
            // É normal não encontrar um perfil, então podemos tratar isso de forma mais silenciosa
            // se for o caso de EmptyResultDataAccessException
            println("Perfil não encontrado para o usuário $idUsuario: ${e.message}")
            null
        }
    }

    fun editarOuInserirPerfil(idUsuario: Int, bio: String?, experiencia: String?, documento: String): Boolean {
        return try {
            // 1. Verifique se o usuário já existe na tabela
            val countSql = "SELECT COUNT(*) FROM prestador_servico WHERE id_usuario = ?"
            val count = jdbcTemplate.queryForObject(countSql, Int::class.java, idUsuario)

            count?.let {
                if (it > 0) {
                    // Se existe, faz o UPDATE
                    println("Usuário existente (count=$count). Executando UPDATE.")
                    val sql = "UPDATE prestador_servico SET bio = ?, experiencia = ?, documento = ? WHERE id_usuario = ?"
                    val result = jdbcTemplate.update(sql, bio, experiencia, documento, idUsuario)
                    println("$result linha(s) afetada(s) no UPDATE")
                    result > 0
                } else {
                    // Se não existe (count é 0), faz o INSERT
                    println("Novo usuário (count=$count). Executando INSERT.")
                    val sql = "INSERT INTO prestador_servico (id_usuario, bio, experiencia, documento) VALUES (?, ?, ?, ?)"
                    val result = jdbcTemplate.update(sql, idUsuario, bio, experiencia, documento)
                    println("$result linha(s) afetada(s) no INSERT")
                    result > 0
                }
            }
        } catch (e: Exception) {
            println("ERRO ao editar/inserir perfil: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            false
        } == true
    }
}