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
}