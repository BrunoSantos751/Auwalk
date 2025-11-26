package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AgendamentoService(
    private val jdbcTemplate: JdbcTemplate
) {

    @Transactional
    fun criarAgendamentoPasseio(idCliente: Int, idServico: Int, idPet: Int, dataHora: LocalDateTime): Map<String, Any> {
        // Lógica de validar pet e remover slot é a mesma para ambos os serviços
        validarPetEremoverSlotDisponivel(idCliente, idPet, idServico, dataHora)

        // Insere na tabela 'passeio'
        val insertPasseioSql = """
            INSERT INTO passeio (id_servico, id_pet, data_hora, status)
            VALUES (?, ?, ?, 'pendente')
            RETURNING id_passeio, id_servico, id_pet, data_hora, status
        """
        return jdbcTemplate.queryForMap(insertPasseioSql, idServico, idPet, dataHora)
    }

    @Transactional
    fun criarAgendamentoSitter(
        idCliente: Int, idServico: Int, idPet: Int,
        dataHora: LocalDateTime, observacoes: String?
    ): Map<String, Any> {
        // Lógica de validar pet e remover slot é a mesma para ambos os serviços
        validarPetEremoverSlotDisponivel(idCliente, idPet, idServico, dataHora)

        // A data_fim é calculada com base na duração do serviço
        val duracaoSql = "SELECT duracao_estimada FROM servico WHERE id_servico = ?"
        val duracaoEmMinutos = jdbcTemplate.queryForObject(duracaoSql, Int::class.java, idServico) ?: 0
        val dataFim = dataHora.plusMinutes(duracaoEmMinutos.toLong())

        // Insere na tabela 'pet_sitting'
        val insertSql = """
            INSERT INTO pet_sitting (id_servico, id_pet, data_inicio, data_fim, status, observacoes)
            VALUES (?, ?, ?, ?, 'pendente', ?)
            RETURNING id_petsitting, id_servico, id_pet, data_inicio, data_fim, status
        """
        return jdbcTemplate.queryForMap(insertSql, idServico, idPet, dataHora, dataFim, observacoes)
    }

    /**
     * Função auxiliar privada para evitar duplicação de código.
     * Valida se o pet pertence ao cliente e se o slot de horário está disponível.
     * Se estiver, remove o slot para evitar agendamentos duplicados.
     */
    private fun validarPetEremoverSlotDisponivel(idCliente: Int, idPet: Int, idServico: Int, dataHora: LocalDateTime) {
        // 1. Valida se o pet pertence ao cliente
        val petPertenceAoClienteSql = "SELECT COUNT(*) FROM pet WHERE id_pet = ? AND id_usuario = ?"
        val countPet = jdbcTemplate.queryForObject(petPertenceAoClienteSql, Int::class.java, idPet, idCliente)
        if (countPet == 0) {
            throw IllegalStateException("Este pet não pertence ao usuário autenticado.")
        }

        // 2. Tenta encontrar e remover o slot de disponibilidade
        val findSlotSql = "SELECT id_disponibilidade FROM disponibilidade WHERE id_servico = ? AND inicio_horario_atendimento = ?"
        val slotId: Int? = try {
            jdbcTemplate.queryForObject(findSlotSql, Int::class.java, idServico, dataHora)
        } catch (e: Exception) {
            null
        }

        if (slotId == null) {
            throw IllegalStateException("Este horário não está disponível para agendamento.")
        }

        // 3. Remove o slot para que não possa ser agendado novamente
        val deleteSlotSql = "DELETE FROM disponibilidade WHERE id_disponibilidade = ?"
        jdbcTemplate.update(deleteSlotSql, slotId)
    }

    fun buscarComoCliente(idCliente: Int): List<Map<String, Any>> {
        val passeiosSql = """
            SELECT 
                p.id_passeio AS id, 
                'Passeio' AS tipo, 
                p.status, 
                p.data_hora AS data_inicio, 
                (p.data_hora + s.duracao_estimada * interval '1 minute') AS data_fim,
                u_prestador.nome AS nome_contraparte,
                pet.nome AS nome_pet,
                s.id_servico AS id_servico,
                u_prestador.id_usuario AS id_usuario_prestador
            FROM passeio p
            JOIN pet ON p.id_pet = pet.id_pet
            JOIN servico s ON p.id_servico = s.id_servico
            JOIN prestador_servico ps ON s.id_prestador = ps.id_prestador
            JOIN usuario u_prestador ON ps.id_usuario = u_prestador.id_usuario
            WHERE pet.id_usuario = ?
        """
        val passeios = jdbcTemplate.queryForList(passeiosSql, idCliente)

        val sittersSql = """
            SELECT 
                ps.id_petsitting AS id,
                'PetSitting' AS tipo,
                ps.status,
                ps.data_inicio,
                ps.data_fim,
                u_prestador.nome AS nome_contraparte,
                pet.nome AS nome_pet,
                s.id_servico AS id_servico,
                u_prestador.id_usuario AS id_usuario_prestador
            FROM pet_sitting ps
            JOIN pet ON ps.id_pet = pet.id_pet
            JOIN servico s ON ps.id_servico = s.id_servico
            JOIN prestador_servico p_serv ON s.id_prestador = p_serv.id_prestador
            JOIN usuario u_prestador ON p_serv.id_usuario = u_prestador.id_usuario
            WHERE pet.id_usuario = ?
        """
        val sitters = jdbcTemplate.queryForList(sittersSql, idCliente)

        return passeios + sitters
    }
    fun buscarComoPrestador(idUsuario: Int): List<Map<String, Any>> {
        // Primeiro, obtemos o id_prestador a partir do id_usuario
        val idPrestador: Int? = try {
            jdbcTemplate.queryForObject("SELECT id_prestador FROM prestador_servico WHERE id_usuario = ?", Int::class.java, idUsuario)
        } catch (e: Exception) {
            return emptyList() // Se não for um prestador, retorna lista vazia
        }

        if (idPrestador == null) return emptyList()

        val passeiosSql = """
            SELECT 
                p.id_passeio AS id,
                'Passeio' AS tipo,
                p.status,
                p.data_hora AS data_inicio,
                (p.data_hora + s.duracao_estimada * interval '1 minute') AS data_fim,
                u_cliente.nome AS nome_contraparte,
                pet.nome AS nome_pet,
                s.id_servico AS id_servico,
                u_cliente.id_usuario AS id_usuario_cliente
            FROM passeio p
            JOIN servico s ON p.id_servico = s.id_servico
            JOIN pet ON p.id_pet = pet.id_pet
            JOIN usuario u_cliente ON pet.id_usuario = u_cliente.id_usuario
            WHERE s.id_prestador = ?
        """
        val passeios = jdbcTemplate.queryForList(passeiosSql, idPrestador)

        val sittersSql = """
            SELECT
                ps.id_petsitting AS id,
                'PetSitting' AS tipo,
                ps.status,
                ps.data_inicio,
                ps.data_fim,
                u_cliente.nome AS nome_contraparte,
                pet.nome AS nome_pet,
                s.id_servico AS id_servico,
                u_cliente.id_usuario AS id_usuario_cliente
            FROM pet_sitting ps
            JOIN servico s ON ps.id_servico = s.id_servico
            JOIN pet ON ps.id_pet = pet.id_pet
            JOIN usuario u_cliente ON pet.id_usuario = u_cliente.id_usuario
            WHERE s.id_prestador = ?
        """
        val sitters = jdbcTemplate.queryForList(sittersSql, idPrestador)

        return passeios + sitters
    }

    fun atualizarStatusPasseio(idPasseio: Int, novoStatus: String): Boolean {
        return try {
            val sql = "UPDATE passeio SET status = ? WHERE id_passeio = ?"
            val linhasAfetadas = jdbcTemplate.update(sql, novoStatus, idPasseio)
            linhasAfetadas > 0
        } catch (e: Exception) {
            println("Erro ao atualizar status do passeio: ${e.message}")
            false
        }
    }

    fun atualizarStatusPetSitting(idPetSitting: Int, novoStatus: String): Boolean {
        return try {
            val sql = "UPDATE pet_sitting SET status = ? WHERE id_petsitting = ?"
            val linhasAfetadas = jdbcTemplate.update(sql, novoStatus, idPetSitting)
            linhasAfetadas > 0
        } catch (e: Exception) {
            println("Erro ao atualizar status do pet sitting: ${e.message}")
            false
        }
    }
}