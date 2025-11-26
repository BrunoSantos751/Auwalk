package backend.auwalk.service

import kotlin.math.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class TrajetoService(private val jdbcTemplate: JdbcTemplate) {

    // DTO interno
    data class TrajetoDTO(
        val idTrajeto: Int? = null,
        val idPasseio: Int,
        val ordem: Int,
        val latitude: Double,
        val longitude: Double
    )

    // Criar trajeto
    fun criarTrajeto(req: TrajetoDTO): Map<String, Any> {
        val sql = """
            INSERT INTO trajeto (
                id_passeio, ordem, geom
            ) VALUES (
                ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)
            )
            RETURNING id_trajeto, id_passeio, ordem
        """.trimIndent()

        return jdbcTemplate.queryForMap(
            sql,
            req.idPasseio,
            req.ordem,
            req.longitude,
            req.latitude
        )
    }

    // Listar trajetos (todos ou por passeio)
    fun listarTrajetos(idPasseio: Int? = null): List<TrajetoDTO> {
        val sql = if (idPasseio != null) {
            """
            SELECT 
                id_trajeto, id_passeio, ordem,
                ST_Y(geom::geometry) AS latitude,
                ST_X(geom::geometry) AS longitude
            FROM trajeto
            WHERE id_passeio = ?
            ORDER BY ordem ASC
            """.trimIndent()
        } else {
            """
            SELECT 
                id_trajeto, id_passeio, ordem,
                ST_Y(geom::geometry) AS latitude,
                ST_X(geom::geometry) AS longitude
            FROM trajeto
            ORDER BY id_passeio, ordem ASC
            """.trimIndent()
        }

        return if (idPasseio != null) {
            jdbcTemplate.query(sql, arrayOf(idPasseio)) { rs, _ ->
                TrajetoDTO(
                    idTrajeto = rs.getInt("id_trajeto"),
                    idPasseio = rs.getInt("id_passeio"),
                    ordem = rs.getInt("ordem"),
                    latitude = rs.getDouble("latitude"),
                    longitude = rs.getDouble("longitude")
                )
            }
        } else {
            jdbcTemplate.query(sql) { rs, _ ->
                TrajetoDTO(
                    idTrajeto = rs.getInt("id_trajeto"),
                    idPasseio = rs.getInt("id_passeio"),
                    ordem = rs.getInt("ordem"),
                    latitude = rs.getDouble("latitude"),
                    longitude = rs.getDouble("longitude")
                )
            }
        }
    }

    // Criar múltiplos trajetos (útil para salvar vários pontos de uma vez)
    fun criarTrajetos(trajetos: List<TrajetoDTO>): List<Map<String, Any>> {
        val sql = """
            INSERT INTO trajeto (
                id_passeio, ordem, geom
            ) VALUES (
                ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)
            )
            RETURNING id_trajeto, id_passeio, ordem
        """.trimIndent()

        return trajetos.map { req ->
            jdbcTemplate.queryForMap(
                sql,
                req.idPasseio,
                req.ordem,
                req.longitude,
                req.latitude
            )
        }
    }

    // Atualizar trajeto
    fun atualizarTrajeto(id: Int, req: TrajetoDTO): Int {
        val sql = """
            UPDATE trajeto SET
                ordem = ?, geom = ST_SetSRID(ST_MakePoint(?, ?), 4326)
            WHERE id_trajeto = ?
        """.trimIndent()

        return jdbcTemplate.update(
            sql,
            req.ordem,
            req.longitude,
            req.latitude,
            id
        )
    }

    // Deletar trajeto
    fun deletarTrajeto(id: Int) {
        jdbcTemplate.update("DELETE FROM trajeto WHERE id_trajeto = ?", id)
    }

    // Deletar todos os trajetos de um passeio
    fun deletarTrajetosPorPasseio(idPasseio: Int) {
        jdbcTemplate.update("DELETE FROM trajeto WHERE id_passeio = ?", idPasseio)
    }

    // Classe para representar um ponto
    data class Ponto(
        val latitude: Double,
        val longitude: Double
    )

    // Calcular distância perpendicular de um ponto a uma linha
    private fun distanciaPerpendicular(ponto: Ponto, linhaInicio: Ponto, linhaFim: Ponto): Double {
        val dx = linhaFim.longitude - linhaInicio.longitude
        val dy = linhaFim.latitude - linhaInicio.latitude
        
        if (dx == 0.0 && dy == 0.0) {
            // Linha é um ponto, calcular distância euclidiana
            val dx2 = ponto.longitude - linhaInicio.longitude
            val dy2 = ponto.latitude - linhaInicio.latitude
            return sqrt(dx2 * dx2 + dy2 * dy2)
        }
        
        val t = ((ponto.longitude - linhaInicio.longitude) * dx + 
                 (ponto.latitude - linhaInicio.latitude) * dy) / 
                (dx * dx + dy * dy)
        
        val projecaoLongitude = linhaInicio.longitude + t * dx
        val projecaoLatitude = linhaInicio.latitude + t * dy
        
        val distLongitude = ponto.longitude - projecaoLongitude
        val distLatitude = ponto.latitude - projecaoLatitude
        
        // Converter para metros aproximados (1 grau ≈ 111km)
        val distLongitudeMetros = distLongitude * 111000 * cos(ponto.latitude * PI / 180.0)
        val distLatitudeMetros = distLatitude * 111000
        
        return sqrt(distLongitudeMetros * distLongitudeMetros + distLatitudeMetros * distLatitudeMetros)
    }

    // Algoritmo de Douglas-Peucker recursivo
    private fun douglasPeucker(pontos: List<Ponto>, epsilon: Double): List<Ponto> {
        if (pontos.size <= 2) {
            return pontos
        }
        
        // Encontrar o ponto mais distante da linha entre o primeiro e último ponto
        var maxDistancia = 0.0
        var indiceMaxDistancia = 0
        
        for (i in 1 until pontos.size - 1) {
            val distancia = distanciaPerpendicular(
                pontos[i],
                pontos[0],
                pontos[pontos.size - 1]
            )
            
            if (distancia > maxDistancia) {
                maxDistancia = distancia
                indiceMaxDistancia = i
            }
        }
        
        // Se a distância máxima é maior que epsilon, recursivamente simplificar
        if (maxDistancia > epsilon) {
            // Recursivamente simplificar as duas partes
            val parte1 = douglasPeucker(pontos.subList(0, indiceMaxDistancia + 1), epsilon)
            val parte2 = douglasPeucker(pontos.subList(indiceMaxDistancia, pontos.size), epsilon)
            
            // Combinar resultados (remover ponto duplicado no meio)
            return parte1.dropLast(1) + parte2
        } else {
            // Todos os pontos estão próximos da linha, retornar apenas os extremos
            return listOf(pontos[0], pontos[pontos.size - 1])
        }
    }

    // Buscar pontos de um passeio e criar polilinha simplificada
    fun simplificarTrajeto(idPasseio: Int, epsilonMetros: Double = 10.0): Map<String, Any> {
        // Buscar todos os pontos do passeio ordenados
        val trajetos = listarTrajetos(idPasseio)
        
        if (trajetos.isEmpty()) {
            throw IllegalArgumentException("Nenhum trajeto encontrado para o passeio $idPasseio")
        }
        
        // Converter para lista de pontos
        val pontos = trajetos.map { Ponto(it.latitude, it.longitude) }
        
        // Aplicar algoritmo de Douglas-Peucker
        val pontosSimplificados = douglasPeucker(pontos, epsilonMetros)
        
        // Criar polilinha usando PostGIS
        if (pontosSimplificados.size < 2) {
            throw IllegalArgumentException("Polilinha precisa de pelo menos 2 pontos")
        }
        
        // Construir a geometria usando ST_MakeLine com valores diretos
        // Criar uma string WKT (Well-Known Text) para a polilinha
        val coordenadasWKT = pontosSimplificados.joinToString(", ") { ponto ->
            "${ponto.longitude} ${ponto.latitude}"
        }
        
        val sql = """
            UPDATE passeio 
            SET trajeto_simplificado = ST_SetSRID(
                ST_GeomFromText('LINESTRING($coordenadasWKT)', 4326),
                4326
            )
            WHERE id_passeio = ?
            RETURNING id_passeio, ST_AsText(trajeto_simplificado) AS trajeto_wkt
        """.trimIndent()
        
        return try {
            jdbcTemplate.queryForMap(sql, idPasseio)
        } catch (e: Exception) {
            throw RuntimeException("Erro ao salvar trajeto simplificado: ${e.message}", e)
        }
    }

    // Buscar trajeto simplificado de um passeio
    fun buscarTrajetoSimplificado(idPasseio: Int): Map<String, Any>? {
        val sql = """
            SELECT 
                id_passeio,
                ST_AsText(trajeto_simplificado) AS trajeto_wkt,
                ST_AsGeoJSON(trajeto_simplificado) AS trajeto_geojson,
                ST_NumPoints(trajeto_simplificado) AS num_pontos
            FROM passeio
            WHERE id_passeio = ? AND trajeto_simplificado IS NOT NULL
        """.trimIndent()
        
        return try {
            jdbcTemplate.queryForMap(sql, idPasseio)
        } catch (e: Exception) {
            null
        }
    }
}

