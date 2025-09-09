package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class UserService(
    private val jdbcTemplate: JdbcTemplate
) {

    fun registrarUsuario(nome: String, email: String, senha: String, telefone: String): Boolean {
        return try {
            val sql = "INSERT INTO usuario (nome, email, senha, telefone) VALUES (?, ?, ?, ?)"
            jdbcTemplate.update(sql, nome, email, senha, telefone)
            true
        } catch (e: Exception) {
            false // pode ser erro de conexão ou email duplicado, etc
        }
    }
}