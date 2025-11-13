package backend.auwalk.Controller


import org.springframework.web.bind.annotation.*
import backend.auwalk.service.UserService

data class UserRequest(val nome: String, val email: String, val senha: String, val telefone: String)

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/register")
    fun registrar(@RequestBody request: UserRequest): Map<String, Any> {
        val sucesso = userService.registrarUsuario(request.nome, request.email, request.senha, request.telefone)
        return if (sucesso) {
            mapOf("success" to true, "message" to "Usuário registrado com sucesso")
        } else {
            mapOf("success" to false, "message" to "Erro ao registrar usuário")
        }
    }
}