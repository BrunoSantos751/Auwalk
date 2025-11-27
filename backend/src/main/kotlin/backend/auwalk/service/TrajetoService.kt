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

    // Adicionar pontos estratégicos quando a simplificação removeu muitos pontos
    // (útil para trajetos que fazem "volta no quarteirão")
    // GARANTE que o primeiro e último ponto original são sempre preservados
    private fun adicionarPontosEstrategicos(
        pontosOriginais: List<Ponto>,
        pontosSimplificados: List<Ponto>,
        numeroDesejado: Int
    ): List<Ponto> {
        // Primeiro e último ponto original (sempre devem ser preservados)
        val primeiroOriginal = pontosOriginais.first()
        val ultimoOriginal = pontosOriginais.last()
        
        // Separar pontos intermediários (excluindo primeiro e último)
        val pontosIntermediarios = pontosSimplificados.filter { simplificado ->
            calcularDistanciaEuclidiana(simplificado, primeiroOriginal) > 0.1 &&
            calcularDistanciaEuclidiana(simplificado, ultimoOriginal) > 0.1
        }
        
        // Calcular quantos pontos precisamos adicionar (já contando com primeiro e último)
        val pontosParaAdicionar = numeroDesejado - pontosSimplificados.size
        
        if (pontosParaAdicionar <= 0) {
            // Mesmo sem adicionar, garantir que primeiro e último estão presentes
            // IMPORTANTE: Não usar distinctBy aqui porque pode remover o último se tiver coordenadas iguais ao primeiro
            val resultado = mutableListOf<Ponto>()
            resultado.add(primeiroOriginal) // Sempre primeiro
            resultado.addAll(pontosIntermediarios) // Intermediários
            resultado.add(ultimoOriginal) // Sempre último, mesmo se coordenadas iguais ao primeiro
            return resultado // Retornar sem distinctBy para preservar primeiro e último
        }
        
        // Selecionar pontos uniformemente distribuídos do trajeto original (sem primeiro e último)
        val pontosOriginaisIntermediarios = pontosOriginais.drop(1).dropLast(1)
        val pontosAdicionais = mutableListOf<Ponto>()
        
        if (pontosOriginaisIntermediarios.isNotEmpty()) {
            val intervalo = pontosOriginaisIntermediarios.size.toDouble() / (pontosParaAdicionar + 1)
            
            for (i in 1..pontosParaAdicionar) {
                val indice = (i * intervalo).toInt().coerceIn(0, pontosOriginaisIntermediarios.size - 1)
                val ponto = pontosOriginaisIntermediarios[indice]
                
                // Verificar se o ponto não está muito próximo dos já simplificados (5 metros)
                val estaMuitoProximo = pontosSimplificados.any { existente ->
                    calcularDistanciaEuclidiana(ponto, existente) < 5.0
                }
                
                // Verificar se não está na lista de adicionais
                val jaEstaNaLista = pontosAdicionais.any { 
                    calcularDistanciaEuclidiana(ponto, it) < 5.0
                }
                
                if (!estaMuitoProximo && !jaEstaNaLista) {
                    pontosAdicionais.add(ponto)
                }
            }
        }
        
        // Combinar: primeiro + intermediários existentes + adicionais + último
        val resultado = mutableListOf<Ponto>()
        resultado.add(primeiroOriginal)
        resultado.addAll(pontosIntermediarios)
        resultado.addAll(pontosAdicionais)
        resultado.add(ultimoOriginal)
        
        // Ordenar pela posição no trajeto original (mas garantir que primeiro e último são preservados)
        val pontosIntermediariosComAdicionais = pontosIntermediarios + pontosAdicionais
        
        // Ordenar pontos intermediários pela ordem no trajeto original
        val intermediariosOrdenados = pontosIntermediariosComAdicionais.sortedBy { ponto ->
            val indice = pontosOriginais.indexOfFirst { original ->
                calcularDistanciaEuclidiana(original, ponto) < 0.1
            }
            if (indice == -1) Int.MAX_VALUE else indice
        }
        
        // Construir resultado final garantindo ordem: primeiro + intermediários + último
        // CRÍTICO: O último ponto original DEVE ser sempre o último elemento
        val resultadoFinal = mutableListOf<Ponto>()
        resultadoFinal.add(primeiroOriginal) // Sempre primeiro
        
        // Adicionar pontos intermediários ordenados, removendo duplicatas consecutivas
        var ultimoAdicionado = primeiroOriginal
        for (pontoInter in intermediariosOrdenados) {
            if (calcularDistanciaEuclidiana(pontoInter, ultimoAdicionado) > 0.1) {
                resultadoFinal.add(pontoInter)
                ultimoAdicionado = pontoInter
            }
        }
        
        // SEMPRE adicionar o último ponto original como último elemento
        // Mesmo que tenha coordenadas iguais ao primeiro ou ao último adicionado
        resultadoFinal.add(ultimoOriginal)
        
        return resultadoFinal
    }
    
    // Calcular distância euclidiana simples entre dois pontos (em metros aproximados)
    private fun calcularDistanciaEuclidiana(p1: Ponto, p2: Ponto): Double {
        val dx = (p2.longitude - p1.longitude) * 111000 * cos(p1.latitude * PI / 180.0)
        val dy = (p2.latitude - p1.latitude) * 111000
        return sqrt(dx * dx + dy * dy)
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
    // Epsilon padrão reduzido para 2.5 metros para simplificação mais conservadora
    fun simplificarTrajeto(idPasseio: Int, epsilonMetros: Double = 1.0): Map<String, Any> {
        // Buscar todos os pontos do passeio ordenados
        val trajetos = listarTrajetos(idPasseio)
        
        if (trajetos.isEmpty()) {
            throw IllegalArgumentException("Nenhum trajeto encontrado para o passeio $idPasseio")
        }
        
        // Converter para lista de pontos
        val pontos = trajetos.map { Ponto(it.latitude, it.longitude) }
        
        // Aplicar algoritmo de Douglas-Peucker
        var pontosSimplificados = douglasPeucker(pontos, epsilonMetros)
        
        // Garantir que sempre temos o primeiro e último ponto
        val primeiroPonto = pontos.first()
        val ultimoPonto = pontos.last()
        
        // Garantir número mínimo de pontos para trajetos que fazem "volta no quarteirão"
        // Se o trajeto simplificado tem muito poucos pontos, adicionar pontos estratégicos
        val numeroMinimoPontos = maxOf(
            5, // Mínimo absoluto de 5 pontos
            (pontos.size * 0.15).toInt().coerceAtMost(30) // 15% dos pontos originais, máximo 30
        )
        
        // Garantir que primeiro e último estão presentes
        val temPrimeiro = pontosSimplificados.any { 
            calcularDistanciaEuclidiana(it, primeiroPonto) < 0.1 
        }
        val temUltimo = pontosSimplificados.any { 
            calcularDistanciaEuclidiana(it, ultimoPonto) < 0.1 
        }
        
        // Se não tiver primeiro ou último, adicionar
        if (!temPrimeiro) {
            pontosSimplificados = listOf(primeiroPonto) + pontosSimplificados
        }
        if (!temUltimo) {
            pontosSimplificados = pontosSimplificados + listOf(ultimoPonto)
        }
        
        if (pontosSimplificados.size < numeroMinimoPontos && pontos.size >= numeroMinimoPontos) {
            // Adicionar pontos estratégicos uniformemente distribuídos
            pontosSimplificados = adicionarPontosEstrategicos(pontos, pontosSimplificados, numeroMinimoPontos)
        }
        
        // Reconstruir lista final garantindo ordem correta e preservando primeiro e último ORIGINAIS
        // CRÍTICO: O último ponto ORIGINAL DEVE ser o último elemento, mesmo se tiver coordenadas iguais ao primeiro
        val resultadoFinal = mutableListOf<Ponto>()
        
        // Separar pontos intermediários (excluindo primeiro e último originais)
        val pontosIntermediarios = pontosSimplificados.filter { ponto ->
            val distPrimeiro = calcularDistanciaEuclidiana(ponto, primeiroPonto)
            val distUltimo = calcularDistanciaEuclidiana(ponto, ultimoPonto)
            distPrimeiro > 0.1 && distUltimo > 0.1
        }
        
        // Ordenar pontos intermediários pela ordem no trajeto original
        val pontosIntermediariosOrdenados = pontosIntermediarios.sortedBy { ponto ->
            val indice = pontos.indexOfFirst { original ->
                calcularDistanciaEuclidiana(original, ponto) < 0.1
            }
            if (indice == -1) Int.MAX_VALUE else indice
        }
        
        // Construir lista final garantindo ordem: primeiro + intermediários + último
        resultadoFinal.add(primeiroPonto) // SEMPRE o primeiro ponto original
        
        // Adicionar pontos intermediários ordenados, removendo duplicatas consecutivas
        var ultimoAdicionado = primeiroPonto
        for (pontoInter in pontosIntermediariosOrdenados) {
            if (calcularDistanciaEuclidiana(pontoInter, ultimoAdicionado) > 0.1) {
                resultadoFinal.add(pontoInter)
                ultimoAdicionado = pontoInter
            }
        }
        
        // SEMPRE adicionar o último ponto ORIGINAL como último elemento
        // CRÍTICO: O último ponto original DEVE estar presente como último elemento,
        // mesmo que tenha coordenadas iguais ao primeiro ou ao último adicionado.
        // Isso preserva a informação de que são momentos diferentes do passeio (início e fim)
        // e permite fechar corretamente trajetos que começam e terminam no mesmo local.
        resultadoFinal.add(ultimoPonto)
        
        pontosSimplificados = resultadoFinal
        
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

