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

    fun editarOuInserirPerfil(idUsuario: Int, bio: String?, experiencia: String?, documento: String): Boolean {
        // Primeiro, verifique se o usuário já existe na tabela
        val countSql = "SELECT COUNT(*) FROM prestador_servico WHERE id_usuario = ?"
        val count = jdbcTemplate.queryForObject(countSql, Int::class.java, idUsuario)

        val sql: String
        val result: Int

        return try {
            // 👇 CORREÇÃO AQUI 👇
            // Adicionamos a verificação 'count != null'
            if (count != null && count > 0) {
                // Se existe, faz o UPDATE
                println("Usuário existente. Executando UPDATE.")
                sql = "UPDATE prestador_servico SET bio = ?, experiencia = ?, documento = ? WHERE id_usuario = ?"
                result = jdbcTemplate.update(sql, bio, experiencia, documento, idUsuario)
            } else {
                // Se não existe (ou se count for 0), faz o INSERT
                println("Novo usuário. Executando INSERT.")
                sql = "INSERT INTO prestador_servico (id_usuario, bio, experiencia, documento) VALUES (?, ?, ?, ?)"
                result = jdbcTemplate.update(sql, idUsuario, bio, experiencia, documento)
            }
            println("$result linha(s) afetada(s)")
            result > 0
        } catch (e: Exception) {
            println("ERRO ao editar/inserir perfil: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}