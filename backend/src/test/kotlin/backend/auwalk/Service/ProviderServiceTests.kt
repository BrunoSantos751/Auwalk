package backend.auwalk.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate

class ProviderServiceTests {

    private val jdbcTemplate = mockk<JdbcTemplate>()
    private val service = ProviderService(jdbcTemplate)

    @Test
    fun `deve retornar perfil quando encontrado`() {
        val idUsuario = 1
        val fakePerfil = mapOf("id_usuario" to 1, "bio" to "Teste")

        every { jdbcTemplate.queryForMap(any(), idUsuario) } returns fakePerfil

        val resultado = service.buscarPerfil(idUsuario)

        assertNotNull(resultado)
        assertEquals("Teste", resultado?.get("bio"))
    }

    @Test
    fun `deve retornar null quando nao encontrar perfil`() {
        val idUsuario = 2

        every { jdbcTemplate.queryForMap(any(), idUsuario) } throws RuntimeException("Not found")

        val resultado = service.buscarPerfil(idUsuario)

        assertNull(resultado)
    }

    @Test
    fun `deve fazer update quando usuario ja existe`() {
        val idUsuario = 1
        every { jdbcTemplate.queryForObject(any(), Int::class.java, idUsuario) } returns 1
        every { jdbcTemplate.update(any(), any(), any(), any(), any<Int>()) } returns 1

        val result = service.editarOuInserirPerfil(idUsuario, "bio", "exp", "doc")

        assertTrue(result)
    }

    @Test
    fun `deve fazer insert quando usuario nao existe`() {
        val idUsuario = 2
        every { jdbcTemplate.queryForObject(any(), Int::class.java, idUsuario) } returns 0
        every { jdbcTemplate.update(any(), any(), any(), any(), any<Int>()) } returns 1

        val result = service.editarOuInserirPerfil(idUsuario, "bio", "exp", "doc")

        assertTrue(result)
    }

    @Test
    fun `deve retornar false em caso de erro`() {
        val idUsuario = 3
        every { jdbcTemplate.queryForObject(any(), Int::class.java, idUsuario) } throws RuntimeException("Erro no banco")

        val result = service.editarOuInserirPerfil(idUsuario, "bio", "exp", "doc")

        assertFalse(result)
    }
}
