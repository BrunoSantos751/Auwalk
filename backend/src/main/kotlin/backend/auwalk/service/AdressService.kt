package backend.auwalk.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class EnderecoService(private val jdbcTemplate: JdbcTemplate) {

    // DTO interno
    data class EnderecoDTO(
        val idEndereco: Int,
        val idUsuario: Int,
        val logradouro: String?,
        val numero: String?,
        val cidade: String?,
        val estado: String?,
        val cep: String?,
        val rua: String?,
        val complemento: String?,
        val pais: String?,
        val latitude: Double,
        val longitude: Double
    )

    // Criar endereço
    fun criarEndereco(req: EnderecoDTO): Map<String, Any> {
        val sql = """
            INSERT INTO endereco (
                id_usuario, logradouro, numero, cidade, estado, cep,
                rua, complemento, pais, location
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)
            )
            RETURNING id_endereco, id_usuario, cidade, estado, pais
        """.trimIndent()

        return jdbcTemplate.queryForMap(
            sql,
            req.idUsuario,
            req.logradouro,
            req.numero,
            req.cidade,
            req.estado,
            req.cep,
            req.rua,
            req.complemento,
            req.pais,
            req.longitude,
            req.latitude
        )
    }

    // Listar endereços (todos ou por usuário)
    fun listarEnderecos(idUsuario: Int? = null): List<EnderecoDTO> {
        val sql = if (idUsuario != null) {
            """
            SELECT 
                id_endereco, id_usuario, logradouro, numero, cidade, estado,
                cep, rua, complemento, pais,
                ST_Y(location::geometry) AS latitude,
                ST_X(location::geometry) AS longitude
            FROM endereco
            WHERE id_usuario = ?
            """.trimIndent()
        } else {
            """
            SELECT 
                id_endereco, id_usuario, logradouro, numero, cidade, estado,
                cep, rua, complemento, pais,
                ST_Y(location::geometry) AS latitude,
                ST_X(location::geometry) AS longitude
            FROM endereco
            """.trimIndent()
        }

        return if (idUsuario != null) {
            jdbcTemplate.query(sql, arrayOf(idUsuario)) { rs, _ ->
                EnderecoDTO(
                    idEndereco = rs.getInt("id_endereco"),
                    idUsuario = rs.getInt("id_usuario"),
                    logradouro = rs.getString("logradouro"),
                    numero = rs.getString("numero"),
                    cidade = rs.getString("cidade"),
                    estado = rs.getString("estado"),
                    cep = rs.getString("cep"),
                    rua = rs.getString("rua"),
                    complemento = rs.getString("complemento"),
                    pais = rs.getString("pais"),
                    latitude = rs.getDouble("latitude"),
                    longitude = rs.getDouble("longitude")
                )
            }
        } else {
            jdbcTemplate.query(sql) { rs, _ ->
                EnderecoDTO(
                    idEndereco = rs.getInt("id_endereco"),
                    idUsuario = rs.getInt("id_usuario"),
                    logradouro = rs.getString("logradouro"),
                    numero = rs.getString("numero"),
                    cidade = rs.getString("cidade"),
                    estado = rs.getString("estado"),
                    cep = rs.getString("cep"),
                    rua = rs.getString("rua"),
                    complemento = rs.getString("complemento"),
                    pais = rs.getString("pais"),
                    latitude = rs.getDouble("latitude"),
                    longitude = rs.getDouble("longitude")
                )
            }
        }
    }

    // Atualizar endereço
    fun atualizarEndereco(id: Int, req: EnderecoDTO): Int {
        val sql = """
            UPDATE endereco SET
                logradouro = ?, numero = ?, cidade = ?, estado = ?, cep = ?,
                rua = ?, complemento = ?, pais = ?, location = ST_SetSRID(ST_MakePoint(?, ?), 4326)
            WHERE id_endereco = ?
        """.trimIndent()

        return jdbcTemplate.update(
            sql,
            req.logradouro,
            req.numero,
            req.cidade,
            req.estado,
            req.cep,
            req.rua,
            req.complemento,
            req.pais,
            req.longitude,
            req.latitude,
            id
        )
    }

    // Deletar endereço
    fun deletarEndereco(id: Int) {
        jdbcTemplate.update("DELETE FROM endereco WHERE id_endereco = ?", id)
    }

    // Buscar passeadores próximos
    fun buscarPasseadoresProximos(latitude: Double, longitude: Double, raioMetros: Int): List<Map<String, Any>> {
        val sql = """
            SELECT u.id_usuario, u.nome, e.cidade, e.estado, e.pais,
                   ST_Distance(
                       e.location::geography,
                       ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
                   ) AS distancia_metros
            FROM endereco e
            JOIN prestador_servico ps ON ps.id_usuario = e.id_usuario
            JOIN usuario u ON u.id_usuario = e.id_usuario
            WHERE ST_DWithin(
                e.location::geography,
                ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                ?
            )
            ORDER BY distancia_metros ASC
        """.trimIndent()

        return jdbcTemplate.queryForList(sql, longitude, latitude, longitude, latitude, raioMetros)
    }
}
