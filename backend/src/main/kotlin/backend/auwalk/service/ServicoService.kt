package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate

data class DisponibilidadeResponse(
    val inicio: String,
    val fim: String
)

data class ServicoDisponivelResponse(
    val tipoServico: String,
    val descricao: String?,
    val preco: Double?,
    val disponibilidades: List<DisponibilidadeResponse>
)

@Service
class ServicoService(
    private val jdbcTemplate: JdbcTemplate
) {

    fun buscarServicosDisponiveis(data: LocalDate?, tipoServico: String?): List<ServicoDisponivelResponse> {
        val sqlBuilder = StringBuilder("""
            SELECT s.id_servico, s.tipo_servico, s.descricao, s.preco
            FROM servico s
            LEFT JOIN disponibilidade d ON s.id_servico = d.id_servico
        """.trimIndent())

        val conditions = mutableListOf<String>()
        val params = mutableListOf<Any>()

        // Adiciona filtro por data se fornecida
        data?.let {
            conditions.add("d.inicio_horario_atendimento::date = ?")
            params.add(it)
        }

        // Adiciona filtro por tipo de serviço se fornecido
        tipoServico?.let {
            conditions.add("s.tipo_servico = ?")
            params.add(it)
        }

        // Adiciona cláusula WHERE apenas se houver condições
        if (conditions.isNotEmpty()) {
            sqlBuilder.append(" WHERE ").append(conditions.joinToString(" AND "))
        }

        sqlBuilder.append(" GROUP BY s.id_servico, s.tipo_servico, s.descricao, s.preco ORDER BY s.preco ASC")

        return jdbcTemplate.query(sqlBuilder.toString(), params.toTypedArray()) { rs, _ ->
            val idServico = rs.getInt("id_servico")
            val tipo = rs.getString("tipo_servico")
            val descricao = rs.getString("descricao")
            val preco = rs.getDouble("preco")
            val disponibilidades = if (data != null) buscarDisponibilidades(idServico, data) else emptyList()
            ServicoDisponivelResponse(
                tipoServico = tipo,
                descricao = descricao,
                preco = preco,
                disponibilidades = disponibilidades
            )
        }
    }

    private fun buscarDisponibilidades(idServico: Int, data: LocalDate): List<DisponibilidadeResponse> {
        val sql = """
            SELECT inicio_horario_atendimento, fim_horario_atendimento
            FROM disponibilidade
            WHERE id_servico = ? AND inicio_horario_atendimento::date = ?
            ORDER BY inicio_horario_atendimento
        """.trimIndent()

        return jdbcTemplate.query(sql, arrayOf(idServico, data)) { rs, _ ->
            DisponibilidadeResponse(
                inicio = rs.getTimestamp("inicio_horario_atendimento").toLocalDateTime().toString(),
                fim = rs.getTimestamp("fim_horario_atendimento").toLocalDateTime().toString()
            )
        }
    }
}
