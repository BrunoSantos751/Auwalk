import { useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import '../pesquisa/MapaPrestadores.css';

// Corrigir ícones padrão do Leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
    iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

interface MapaTrajetoProps {
    idPasseio: number;
    onClose: () => void;
}

type ModoVisualizacao = 'simplificado' | 'original' | 'ambos';

const MapaTrajeto: React.FC<MapaTrajetoProps> = ({ idPasseio, onClose }) => {
    const mapRef = useRef<L.Map | null>(null);
    const mapContainerRef = useRef<HTMLDivElement>(null);
    const polylineRef = useRef<L.LayerGroup | null>(null);
    const [carregando, setCarregando] = useState(true);
    const [erro, setErro] = useState<string | null>(null);
    const [info, setInfo] = useState<string | null>(null);
    const [modoVisualizacao, setModoVisualizacao] = useState<ModoVisualizacao>('simplificado');
    const [trajetoOriginal, setTrajetoOriginal] = useState<any[] | null>(null);
    const [trajetoSimplificado, setTrajetoSimplificado] = useState<any | null>(null);
    const [numPontosOriginal, setNumPontosOriginal] = useState<number>(0);
    const [numPontosSimplificado, setNumPontosSimplificado] = useState<number>(0);

    useEffect(() => {
        if (!mapContainerRef.current) return;

        // Inicializar o mapa
        const map = L.map(mapContainerRef.current);
        mapRef.current = map;

        // Camada base
        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap contributors",
        }).addTo(map);

        // Limpar ao desmontar
        return () => {
            if (mapRef.current) {
                mapRef.current.remove();
                mapRef.current = null;
            }
        };
    }, []);

    // Carregar trajetos (original e simplificado)
    useEffect(() => {
        const carregarTrajetos = async () => {
            try {
                setCarregando(true);
                setErro(null);
                
                let trajetoOriginalCarregado = false;
                let trajetoSimplificadoCarregado = false;
                
                // Carregar trajeto original (todos os pontos)
                const trajetoOriginalResponse = await fetch(`http://localhost:8080/trajetos?idPasseio=${idPasseio}`);
                if (trajetoOriginalResponse.ok) {
                    const trajetosOriginais = await trajetoOriginalResponse.json();
                    if (Array.isArray(trajetosOriginais) && trajetosOriginais.length > 0) {
                        setTrajetoOriginal(trajetosOriginais);
                        setNumPontosOriginal(trajetosOriginais.length);
                        trajetoOriginalCarregado = true;
                    }
                }
                
                // Carregar trajeto simplificado
                const trajetoSimplificadoResponse = await fetch(`http://localhost:8080/trajetos/simplificado/${idPasseio}`);
                if (trajetoSimplificadoResponse.ok) {
                    const dataSimplificado = await trajetoSimplificadoResponse.json();
                    if (dataSimplificado.trajeto_geojson) {
                        setTrajetoSimplificado(dataSimplificado);
                        setNumPontosSimplificado(dataSimplificado.num_pontos || 0);
                        trajetoSimplificadoCarregado = true;
                    }
                }
                
                // Verificar se pelo menos um trajeto foi carregado
                if (!trajetoOriginalCarregado && !trajetoSimplificadoCarregado) {
                    setErro('Trajeto não encontrado para este passeio.');
                }
                
                setCarregando(false);
            } catch (error) {
                console.error('Erro ao carregar trajetos:', error);
                setErro('Erro ao carregar trajetos. Tente novamente.');
                setCarregando(false);
            }
        };
        
        carregarTrajetos();
    }, [idPasseio]);

    // Renderizar trajetos no mapa baseado no modo de visualização
    useEffect(() => {
        if (!mapRef.current || carregando) return;
        
        const renderizarTrajetos = () => {
            // Remover trajetos anteriores
            if (polylineRef.current && mapRef.current) {
                mapRef.current.removeLayer(polylineRef.current);
                polylineRef.current = null;
            }

            const layerGroup = L.layerGroup();
            let todasCoordenadas: [number, number][] = [];

            // Renderizar trajeto original
            if ((modoVisualizacao === 'original' || modoVisualizacao === 'ambos') && trajetoOriginal) {
                const pontosOriginais: [number, number][] = trajetoOriginal
                    .sort((a: any, b: any) => a.ordem - b.ordem)
                    .map((t: any) => [t.latitude, t.longitude] as [number, number]);
                
                const polylineOriginal = L.polyline(pontosOriginais, {
                    color: '#ff6b35',
                    weight: 3,
                    opacity: 0.6,
                    dashArray: '5, 5'
                });
                
                polylineOriginal.bindPopup('📊 Trajeto Original (Todos os pontos)');
                polylineOriginal.addTo(layerGroup);
                
                todasCoordenadas = todasCoordenadas.concat(pontosOriginais);
            }

            // Renderizar trajeto simplificado
            if ((modoVisualizacao === 'simplificado' || modoVisualizacao === 'ambos') && trajetoSimplificado) {
                const trajetoGeoJSON = trajetoSimplificado.trajeto_geojson;
                
                if (trajetoGeoJSON) {
                    let geojsonData;
                    if (typeof trajetoGeoJSON === 'string') {
                        try {
                            geojsonData = JSON.parse(trajetoGeoJSON);
                        } catch (e) {
                            console.error('Erro ao fazer parse do GeoJSON:', e);
                            return;
                        }
                    } else {
                        geojsonData = trajetoGeoJSON;
                    }

                    const geojsonLayer = L.geoJSON(geojsonData, {
                        style: {
                            color: '#079fce',
                            weight: modoVisualizacao === 'ambos' ? 4 : 5,
                            opacity: 0.8
                        }
                    });

                    geojsonLayer.bindPopup('📉 Trajeto Simplificado');
                    geojsonLayer.addTo(layerGroup);

                    // Extrair coordenadas do simplificado
                    let coordenadasSimplificado: [number, number][] = [];
                    if (geojsonData.type === 'FeatureCollection' && geojsonData.features) {
                        geojsonData.features.forEach((feature: any) => {
                            if (feature.geometry.type === 'LineString' && feature.geometry.coordinates) {
                                coordenadasSimplificado = feature.geometry.coordinates.map((coord: number[]) => [coord[1], coord[0]]);
                            }
                        });
                    } else if (geojsonData.type === 'LineString' && geojsonData.coordinates) {
                        coordenadasSimplificado = geojsonData.coordinates.map((coord: number[]) => [coord[1], coord[0]]);
                    } else if (geojsonData.geometry && geojsonData.geometry.type === 'LineString') {
                        coordenadasSimplificado = geojsonData.geometry.coordinates.map((coord: number[]) => [coord[1], coord[0]]);
                    }
                    
                    todasCoordenadas = todasCoordenadas.concat(coordenadasSimplificado);
                }
            }

            // Se não houver trajeto simplificado mas houver original, usar apenas original
            if (modoVisualizacao === 'simplificado' && !trajetoSimplificado && trajetoOriginal) {
                const pontosOriginais: [number, number][] = trajetoOriginal
                    .sort((a: any, b: any) => a.ordem - b.ordem)
                    .map((t: any) => [t.latitude, t.longitude] as [number, number]);
                
                const polylineOriginal = L.polyline(pontosOriginais, {
                    color: '#079fce',
                    weight: 5,
                    opacity: 0.8
                });
                
                polylineOriginal.bindPopup('📊 Trajeto Original (sem simplificação disponível)');
                polylineOriginal.addTo(layerGroup);
                todasCoordenadas = pontosOriginais;
            }
            
            // Verificar se há algo para mostrar
            if (todasCoordenadas.length === 0) {
                setErro('Nenhum trajeto disponível para visualização.');
                setCarregando(false);
                return;
            }

            // Adicionar marcadores de início e fim
            if (todasCoordenadas.length > 0) {
                const coordenadasParaMarcadores = modoVisualizacao === 'original' && trajetoOriginal
                    ? trajetoOriginal
                          .sort((a: any, b: any) => a.ordem - b.ordem)
                          .map((t: any) => [t.latitude, t.longitude] as [number, number])
                    : todasCoordenadas;
                
                const inicio = coordenadasParaMarcadores[0];
                const fim = coordenadasParaMarcadores[coordenadasParaMarcadores.length - 1];

                // Marcador de início (verde)
                const markerInicio = L.marker(inicio, {
                    icon: L.icon({
                        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png',
                        iconSize: [25, 41],
                        iconAnchor: [12, 41],
                        popupAnchor: [1, -34],
                        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
                        shadowSize: [41, 41]
                    })
                }).bindPopup('📍 Início do Passeio');
                markerInicio.addTo(layerGroup);

                // Marcador de fim (vermelho) - apenas se for diferente do início
                if (coordenadasParaMarcadores.length > 1 && (inicio[0] !== fim[0] || inicio[1] !== fim[1])) {
                    const markerFim = L.marker(fim, {
                        icon: L.icon({
                            iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
                            iconSize: [25, 41],
                            iconAnchor: [12, 41],
                            popupAnchor: [1, -34],
                            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
                            shadowSize: [41, 41]
                        })
                    }).bindPopup('🏁 Fim do Passeio');
                    markerFim.addTo(layerGroup);
                }
            }

            // Adicionar ao mapa
            layerGroup.addTo(mapRef.current!);
            polylineRef.current = layerGroup;

            // Ajustar visualização
            if (mapRef.current && todasCoordenadas.length > 0) {
                const bounds = L.latLngBounds(todasCoordenadas);
                if (bounds.isValid()) {
                    mapRef.current.fitBounds(bounds, { padding: [50, 50] });
                } else {
                    mapRef.current.setView(todasCoordenadas[0], 15);
                }
            }

            // Atualizar informações
            let mensagemInfo = '';
            if (modoVisualizacao === 'simplificado') {
                mensagemInfo = `Trajeto simplificado com ${numPontosSimplificado} ponto${numPontosSimplificado > 1 ? 's' : ''}`;
            } else if (modoVisualizacao === 'original') {
                mensagemInfo = `Trajeto original com ${numPontosOriginal} pontos`;
            } else if (modoVisualizacao === 'ambos') {
                mensagemInfo = `Comparação: Original (${numPontosOriginal} pts) vs Simplificado (${numPontosSimplificado} pts)`;
            }
            setInfo(mensagemInfo);
        };

        renderizarTrajetos();
    }, [modoVisualizacao, trajetoOriginal, trajetoSimplificado, numPontosOriginal, numPontosSimplificado, carregando]);


    return (
        <div className="mapa-overlay" onClick={onClose}>
            <div className="mapa-container" onClick={(e) => e.stopPropagation()}>
                <div className="mapa-header">
                    <h2>Trajeto do Passeio</h2>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        {/* Botões de alternância de visualização */}
                        <div style={{ display: 'flex', gap: '5px', marginRight: '10px' }}>
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    setModoVisualizacao('simplificado');
                                }}
                                style={{
                                    padding: '5px 10px',
                                    border: 'none',
                                    borderRadius: '5px',
                                    backgroundColor: modoVisualizacao === 'simplificado' ? '#079fce' : '#e0e0e0',
                                    color: modoVisualizacao === 'simplificado' ? 'white' : '#333',
                                    cursor: 'pointer',
                                    fontSize: '0.85rem',
                                    fontWeight: modoVisualizacao === 'simplificado' ? 'bold' : 'normal'
                                }}
                            >
                                Simplificado
                            </button>
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    setModoVisualizacao('original');
                                }}
                                style={{
                                    padding: '5px 10px',
                                    border: 'none',
                                    borderRadius: '5px',
                                    backgroundColor: modoVisualizacao === 'original' ? '#ff6b35' : '#e0e0e0',
                                    color: modoVisualizacao === 'original' ? 'white' : '#333',
                                    cursor: 'pointer',
                                    fontSize: '0.85rem',
                                    fontWeight: modoVisualizacao === 'original' ? 'bold' : 'normal'
                                }}
                            >
                                Original
                            </button>
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    setModoVisualizacao('ambos');
                                }}
                                style={{
                                    padding: '5px 10px',
                                    border: 'none',
                                    borderRadius: '5px',
                                    backgroundColor: modoVisualizacao === 'ambos' ? '#28a745' : '#e0e0e0',
                                    color: modoVisualizacao === 'ambos' ? 'white' : '#333',
                                    cursor: 'pointer',
                                    fontSize: '0.85rem',
                                    fontWeight: modoVisualizacao === 'ambos' ? 'bold' : 'normal'
                                }}
                            >
                                Ambos
                            </button>
                        </div>
                        <button className="mapa-close-btn" onClick={onClose}>✕</button>
                    </div>
                </div>
                {carregando && (
                    <div style={{ padding: '20px', textAlign: 'center', color: '#666', backgroundColor: '#f0f0f0' }}>
                        <p>Carregando trajeto do passeio...</p>
                    </div>
                )}
                {erro && (
                    <div style={{ padding: '20px', textAlign: 'center', color: '#d32f2f', backgroundColor: '#ffebee' }}>
                        <p><strong>{erro}</strong></p>
                    </div>
                )}
                {info && !carregando && !erro && (
                    <div style={{ padding: '10px 20px', textAlign: 'center', color: '#079fce', backgroundColor: '#e0f7ff', fontSize: '0.9rem' }}>
                        <p>{info}</p>
                    </div>
                )}
                <div ref={mapContainerRef} className="mapa-content" />
            </div>
        </div>
    );
};

export default MapaTrajeto;

