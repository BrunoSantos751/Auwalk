package backend.auwalk.service

import backend.auwalk.controller.DisponibilidadeRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime

data class DisponibilidadeResponse(
    val inicio: String,
    val fim: String
)

data class ServicoDisponivelResponse(
    val idServico: Int,
    val tipoServico: String,
    val descricao: String?,
    val preco: Double?,
    val nomePrestador: String,
    val duracaoEstimada: Int,
    val disponibilidades: List<DisponibilidadeResponse>
)

@Service
class ServicoUnificadoService(
    private val jdbcTemplate: JdbcTemplate
) {

    @Transactional
    fun createService(
        idPrestador: Int,
        tipoServico: String,
        descricao: String?,
        preco: BigDecimal,
        duracaoEstimada: Int,
        disponibilidades: List<DisponibilidadeRequest>
    ): Map<String, Any?> {
        val insertServiceSql = "INSERT INTO servico (id_prestador, tipo_servico, descricao, preco, duracao_estimada) VALUES (?, ?, ?, ?, ?) RETURNING id_servico"
        val serviceId = jdbcTemplate.queryForObject(insertServiceSql, Int::class.java,
            idPrestador, tipoServico, descricao, preco, duracaoEstimada
        )

        val disponibilidadesResponse = mutableListOf<Map<String, Any?>>()
        val insertSlotSql = "INSERT INTO disponibilidade (id_servico, inicio_horario_atendimento, fim_horario_atendimento) VALUES (?, ?, ?)"

        // --- LÓGICA DE "PICOTAR" UNIFICADA E CORRIGIDA ---
        // A condição IF foi removida para que a lógica se aplique a TODOS os serviços
        if (duracaoEstimada > 0) {
            disponibilidades.forEach { janelaDeTempo ->
                val janelaInicio = LocalDateTime.parse(janelaDeTempo.inicioHorarioAtendimento)
                val janelaFim = LocalDateTime.parse(janelaDeTempo.fimHorarioAtendimento)
                val duracaoEmLong = duracaoEstimada.toLong()

                var slotAtualInicio = janelaInicio
                while (slotAtualInicio.plusMinutes(duracaoEmLong) <= janelaFim) {
                    val slotAtualFim = slotAtualInicio.plusMinutes(duracaoEmLong)

                    jdbcTemplate.update(insertSlotSql, serviceId, slotAtualInicio, slotAtualFim)

                    val lastIdSql = "SELECT id_disponibilidade FROM disponibilidade WHERE id_servico = ? AND inicio_horario_atendimento = ?"
                    val disponibilidadeId = jdbcTemplate.queryForObject(lastIdSql, Int::class.java, serviceId, slotAtualInicio)

                    disponibilidadesResponse.add(mapOf(
                        "idDisponibilidade" to disponibilidadeId,
                        "inicioHorarioAtendimento" to slotAtualInicio,
                        "fimHorarioAtendimento" to slotAtualFim
                    ))
                    slotAtualInicio = slotAtualFim
                }
            }
        }
        // --- FIM DA LÓGICA CORRIGIDA ---

        return mapOf(
            "idServico" to serviceId, "idPrestador" to idPrestador, "tipoServico" to tipoServico,
            "descricao" to descricao, "preco" to preco, "duracaoEstimada" to duracaoEstimada,
            "disponibilidades" to disponibilidadesResponse
        )
    }

    fun getServicesByPrestadorId(idPrestador: Int): List<Map<String, Any?>> {
        val sql = """
            SELECT s.*, d.id_disponibilidade, d.inicio_horario_atendimento, d.fim_horario_atendimento
            FROM servico s
            LEFT JOIN disponibilidade d ON s.id_servico = d.id_servico
            WHERE s.id_prestador = ?
        """
        val serviceMap = mutableMapOf<Int, MutableMap<String, Any?>>()

        jdbcTemplate.query(sql, { rs: ResultSet, _ ->
            val serviceId = rs.getInt("id_servico")
            val service = serviceMap.getOrPut(serviceId) {
                mutableMapOf(
                    "idServico" to serviceId, "idPrestador" to rs.getInt("id_prestador"),
                    "tipoServico" to rs.getString("tipo_servico"), "descricao" to rs.getString("descricao"),
                    "preco" to rs.getBigDecimal("preco"), "duracaoEstimada" to rs.getInt("duracao_estimada"),
                    "disponibilidades" to mutableListOf<Map<String, Any?>>()
                )
            }

            val disponibilidadeId = rs.getObject("id_disponibilidade") as? Int
            if (disponibilidadeId != null) {
                (service["disponibilidades"] as MutableList<Map<String, Any?>>).add(
                    mapOf(
                        "idDisponibilidade" to disponibilidadeId,
                        "inicioHorarioAtendimento" to rs.getObject("inicio_horario_atendimento", LocalDateTime::class.java),
                        "fimHorarioAtendimento" to rs.getObject("fim_horario_atendimento", LocalDateTime::class.java)
                    )
                )
            }
        }, idPrestador)

        return serviceMap.values.toList()
    }

    fun buscarServicosDisponiveis(data: LocalDate?, tipoServico: String?): List<ServicoDisponivelResponse> {
        // --- CONSULTA SQL CORRIGIDA PARA EVITAR DUPLICAÇÃO ---
        val sqlBuilder = StringBuilder("""
            SELECT s.id_servico, s.tipo_servico, s.descricao, s.preco, s.duracao_estimada, u.nome as nome_prestador
            FROM servico s
            JOIN prestador_servico ps ON s.id_prestador = ps.id_prestador
            JOIN usuario u ON ps.id_usuario = u.id_usuario
        """)
        val params = mutableListOf<Any>()
        val conditions = mutableListOf<String>()

        // Filtro de tipo de serviço é aplicado na consulta principal
        tipoServico?.let {
            if (it.isNotBlank()) {
                conditions.add("s.tipo_servico ILIKE ?") // ILIKE para não diferenciar maiúsculas/minúsculas
                params.add(it)
            }
        }

        // Filtro de data agora usa uma subconsulta com EXISTS para não duplicar os serviços
        data?.let {
            conditions.add("""
                EXISTS (SELECT 1 FROM disponibilidade d
                        WHERE d.id_servico = s.id_servico
                        AND d.inicio_horario_atendimento::date = ?)
            """)
            params.add(it)
        }

        if (conditions.isNotEmpty()) {
            sqlBuilder.append(" WHERE ").append(conditions.joinToString(" AND "))
        }

        sqlBuilder.append(" ORDER BY s.preco ASC")
        // --- FIM DA CORREÇÃO NA SQL ---

        return jdbcTemplate.query(sqlBuilder.toString(), params.toTypedArray()) { rs, _ ->
            val idServico = rs.getInt("id_servico")

            val todasDisponibilidades = buscarTodasDisponibilidades(idServico)

            ServicoDisponivelResponse(
                idServico = idServico,
                tipoServico = rs.getString("tipo_servico"),
                descricao = rs.getString("descricao"),
                preco = rs.getDouble("preco"),
                nomePrestador = rs.getString("nome_prestador"),
                duracaoEstimada = rs.getInt("duracao_estimada"),
                disponibilidades = todasDisponibilidades
            )
        }
    }

    private fun buscarTodasDisponibilidades(idServico: Int): List<DisponibilidadeResponse> {
        val sql = """
            SELECT inicio_horario_atendimento, fim_horario_atendimento
            FROM disponibilidade
            WHERE id_servico = ? AND inicio_horario_atendimento >= NOW()
            ORDER BY inicio_horario_atendimento
        """.trimIndent()
        return jdbcTemplate.query(sql, arrayOf(idServico)) { rs, _ ->
            DisponibilidadeResponse(
                inicio = rs.getTimestamp("inicio_horario_atendimento").toLocalDateTime().toString(),
                fim = rs.getTimestamp("fim_horario_atendimento").toLocalDateTime().toString()
            )
        }
    }
}