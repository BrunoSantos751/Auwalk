package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDateTime

@Service
class ServiceService(
    private val jdbcTemplate: JdbcTemplate
) {

    @Transactional
    fun createService(
        idPrestador: Int,
        tipoServico: String,
        descricao: String?,
        preco: BigDecimal,
        duracaoEstimada: Int,
        disponibilidades: List<Map<String, Any>>
    ): Map<String, Any?> {
        val insertServiceSql = "INSERT INTO servico (id_prestador, tipo_servico, descricao, preco, duracao_estimada) VALUES (?, ?, ?, ?, ?) RETURNING id_servico"
        val serviceId = jdbcTemplate.queryForObject(insertServiceSql, Int::class.java,
            idPrestador,
            tipoServico,
            descricao,
            preco,
            duracaoEstimada
        )

        val disponibilidadesResponse = mutableListOf<Map<String, Any?>>()
        disponibilidades.forEach { dispRequest ->
            // ALTERADO: Faz o parse da String para LocalDateTime
            val inicioHorarioAtendimento = LocalDateTime.parse(dispRequest["inicioHorarioAtendimento"] as String)
            // ALTERADO: Faz o parse da String para LocalDateTime
            val fimHorarioAtendimento = LocalDateTime.parse(dispRequest["fimHorarioAtendimento"] as String)
            
            val insertDisponibilidadeSql = "INSERT INTO disponibilidade (id_servico, inicio_horario_atendimento, fim_horario_atendimento) VALUES (?, ?, ?) RETURNING id_disponibilidade"
            val disponibilidadeId = jdbcTemplate.queryForObject(insertDisponibilidadeSql, Int::class.java,
                serviceId,
                inicioHorarioAtendimento,
                fimHorarioAtendimento
            )
            disponibilidadesResponse.add(mapOf(
                "idDisponibilidade" to disponibilidadeId,
                "inicioHorarioAtendimento" to inicioHorarioAtendimento,
                "fimHorarioAtendimento" to fimHorarioAtendimento
            ))
        }

        return mapOf(
            "idServico" to serviceId,
            "idPrestador" to idPrestador,
            "tipoServico" to tipoServico,
            "descricao" to descricao,
            "preco" to preco,
            "duracaoEstimada" to duracaoEstimada,
            "disponibilidades" to disponibilidadesResponse
        )
    }

    fun getServicesByPrestadorId(idPrestador: Int): List<Map<String, Any?>> {
        val sql = """
            SELECT
                s.id_servico,
                s.id_prestador,
                s.tipo_servico,
                s.descricao,
                s.preco,
                s.duracao_estimada,
                d.id_disponibilidade,
                d.inicio_horario_atendimento,
                d.fim_horario_atendimento
            FROM servico s
            LEFT JOIN disponibilidade d ON s.id_servico = d.id_servico
            WHERE s.id_prestador = ?
        """

        val serviceMap = mutableMapOf<Int, MutableMap<String, Any?>>()

        jdbcTemplate.query(sql, { rs: ResultSet, _ ->
            val serviceId = rs.getInt("id_servico")
            val service = serviceMap.getOrPut(serviceId) {
                mutableMapOf(
                    "idServico" to serviceId,
                    "idPrestador" to rs.getInt("id_prestador"),
                    "tipoServico" to rs.getString("tipo_servico"),
                    "descricao" to rs.getString("descricao"),
                    "preco" to rs.getBigDecimal("preco"),
                    "duracaoEstimada" to rs.getInt("duracao_estimada"),
                    "disponibilidades" to mutableListOf<Map<String, Any?>>()
                )
            }

            val disponibilidadeId = rs.getObject("id_disponibilidade") as? Int
            if (disponibilidadeId != null) {
                val disponibilidadesList = service["disponibilidades"] as MutableList<Map<String, Any?>>
                disponibilidadesList.add(
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
}