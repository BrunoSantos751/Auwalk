import { useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './MapaPrestadores.css';

// Corrigir ícones padrão do Leaflet (necessário porque o webpack não resolve os caminhos automaticamente)
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
    iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

// Interface para os dados de endereço
interface EnderecoPrestador {
    idEndereco: number;
    idUsuario: number;
    logradouro: string | null;
    numero: string | null;
    cidade: string;
    estado: string;
    cep: string | null;
    rua: string | null;
    complemento: string | null;
    pais: string | null;
    latitude: number;
    longitude: number;
}

// Interface para os serviços
interface Servico {
    idServico: number;
    idPrestador: number;
    tipoServico: string;
    descricao: string | null;
    preco: number | null;
    nomePrestador: string;
    duracaoEstimada: number;
}

interface MapaPrestadoresProps {
    servicos: Servico[];
    onClose: () => void;
}

const MapaPrestadores: React.FC<MapaPrestadoresProps> = ({ servicos, onClose }) => {
    const mapRef = useRef<L.Map | null>(null);
    const mapContainerRef = useRef<HTMLDivElement>(null);
    const markersRef = useRef<L.Marker[]>([]);
    const [carregando, setCarregando] = useState(true);
    const [marcadoresEncontrados, setMarcadoresEncontrados] = useState(0);

    useEffect(() => {
        if (!mapContainerRef.current) return;

        // Inicializar o mapa
        const map = L.map(mapContainerRef.current).setView([-9.66625, -35.7351], 14);
        mapRef.current = map;

        // Camada base
        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap contributors",
        }).addTo(map);

        // Carregar camadas GeoJSON
        const carregarCamadasGeoJSON = async () => {
            try {

            } catch (error) {
                console.error("Erro ao carregar camadas GeoJSON:", error);
            }
        };

        carregarCamadasGeoJSON();

        // Limpar ao desmontar
        return () => {
            if (mapRef.current) {
                mapRef.current.remove();
                mapRef.current = null;
            }
        };
    }, []);

    // Buscar endereços dos prestadores e adicionar marcadores
    useEffect(() => {
        if (!mapRef.current) {
            console.warn('⚠️ Mapa não está inicializado ainda');
            return;
        }
        
        if (servicos.length === 0) {
            console.warn('⚠️ Nenhum serviço fornecido para exibir no mapa');
            return;
        }

        console.log(`🗺️ Iniciando busca de endereços para ${servicos.length} serviços`);
        setCarregando(true);

        const buscarEnderecosEAdicionarMarcadores = async () => {
            const enderecosMap = new Map<number, EnderecoPrestador>();
            
            // Limpar marcadores anteriores
            markersRef.current.forEach(marker => {
                if (mapRef.current) {
                    mapRef.current.removeLayer(marker);
                }
            });
            markersRef.current = [];

            // Buscar endereço para cada prestador único
            // NOTA: O backend retorna idUsuario no campo idPrestador, então usamos diretamente
            const idUsuariosUnicos = [...new Set(servicos.map(s => s.idPrestador))];
            console.log(`📍 Usuários únicos encontrados: ${idUsuariosUnicos.length}`, idUsuariosUnicos);
            console.log(`ℹ️ NOTA: O campo idPrestador na verdade contém o idUsuario do backend`);
            
            let marcadoresCriados = 0;
            let erros = 0;
            
            for (const idUsuario of idUsuariosUnicos) {
                console.log(`🔍 Buscando endereço para usuário ${idUsuario}...`);
                try {
                    // Buscar endereço diretamente pelo idUsuario (que vem no campo idPrestador)
                    const enderecoResponse = await fetch(
                        `https://auwalk-redirect.santosmoraes79.workers.dev/enderecos?idUsuario=${idUsuario}`
                    );
                    if (!enderecoResponse.ok) {
                        console.warn(`⚠️ Endereço não encontrado para usuário ${idUsuario} (status: ${enderecoResponse.status})`);
                        continue;
                    }
                    
                    const enderecos = await enderecoResponse.json();
                    console.log(`📦 Endereços retornados para usuário ${idUsuario}:`, enderecos);
                    
                    if (!Array.isArray(enderecos) || enderecos.length === 0) {
                        console.warn(`⚠️ Nenhum endereço encontrado para usuário ${idUsuario}`);
                        continue;
                    }
                    
                    if (Array.isArray(enderecos) && enderecos.length > 0) {
                        const endereco = enderecos[0] as EnderecoPrestador;
                        
                        // Buscar serviços deste usuário/prestador
                        const servicosPrestador = servicos.filter(s => s.idPrestador === idUsuario);
                        if (servicosPrestador.length === 0) {
                            console.warn(`⚠️ Nenhum serviço encontrado para usuário ${idUsuario}`);
                            continue;
                        }
                        
                        console.log(`📍 Endereço encontrado para usuário ${idUsuario}:`, {
                            rua: endereco.rua || endereco.logradouro,
                            numero: endereco.numero,
                            cidade: endereco.cidade,
                            estado: endereco.estado,
                            latitude: endereco.latitude,
                            longitude: endereco.longitude
                        });
                        
                        // Validar coordenadas
                        if (!endereco.latitude || !endereco.longitude || 
                            isNaN(endereco.latitude) || isNaN(endereco.longitude)) {
                            console.error(`❌ Coordenadas inválidas para usuário ${idUsuario}:`, endereco);
                            continue;
                        }
                        
                        // Validar se as coordenadas estão no Brasil
                        if (endereco.latitude < -35 || endereco.latitude > 5 || 
                            endereco.longitude < -75 || endereco.longitude > -30) {
                            console.warn(`⚠️ Coordenadas fora do Brasil para usuário ${idUsuario}:`, {
                                lat: endereco.latitude,
                                lon: endereco.longitude
                            });
                        }
                        
                        enderecosMap.set(idUsuario, endereco);
                        
                        // Criar marcador
                        console.log(`📍 Criando marcador em: [${endereco.latitude}, ${endereco.longitude}]`);
                        const marker = L.marker([endereco.latitude, endereco.longitude]).addTo(mapRef.current!);
                        
                        // Criar popup com informações do prestador
                        // Verificar tanto 'rua' quanto 'logradouro' para o endereço
                        const ruaOuLogradouro = endereco.rua || endereco.logradouro || '';
                        const partesEndereco = [];
                        
                        if (ruaOuLogradouro) {
                            const enderecoCompleto = endereco.numero 
                                ? `${ruaOuLogradouro}, ${endereco.numero}`
                                : ruaOuLogradouro;
                            partesEndereco.push(enderecoCompleto);
                        }
                        
                        if (endereco.cidade) partesEndereco.push(endereco.cidade);
                        if (endereco.estado) partesEndereco.push(endereco.estado);
                        if (endereco.cep) partesEndereco.push(`CEP: ${endereco.cep}`);
                        
                        
                        const tiposServico = [...new Set(servicosPrestador.map(s => s.tipoServico))].join(', ');
                        
                        const popupContent = `
                            <div style="min-width: 200px;">
                                <strong>${servicosPrestador[0].nomePrestador}</strong><br/>
                                <small>Serviços: ${tiposServico}</small>
                            </div>
                        `;
                        
                        marker.bindPopup(popupContent);
                        markersRef.current.push(marker);
                        marcadoresCriados++;
                        console.log(`✅ Marcador criado com sucesso para usuário ${idUsuario}`);
                    }
                } catch (error) {
                    erros++;
                    console.error(`❌ Erro ao buscar endereço do usuário ${idUsuario}:`, error);
                }
            }

            console.log(`✅ Processamento concluído: ${marcadoresCriados} marcadores criados, ${erros} erros`);
            console.log(`📊 Total de marcadores no mapa: ${markersRef.current.length}`);
            setMarcadoresEncontrados(marcadoresCriados);
            setCarregando(false);

            // Ajustar visão do mapa para mostrar todos os marcadores
            if (markersRef.current.length > 0 && mapRef.current) {
                console.log('🗺️ Ajustando visualização do mapa para mostrar todos os marcadores');
                const group = new L.FeatureGroup(markersRef.current);
                try {
                    mapRef.current.fitBounds(group.getBounds().pad(0.1));
                } catch (error) {
                    console.error('Erro ao ajustar bounds do mapa:', error);
                    // Se houver erro, pelo menos centralizar no primeiro marcador
                    if (markersRef.current.length > 0) {
                        const firstMarker = markersRef.current[0];
                        const latlng = firstMarker.getLatLng();
                        mapRef.current.setView(latlng, 13);
                    }
                }
            } else {
                console.warn('⚠️ Nenhum marcador foi criado! Verifique os logs acima para mais detalhes.');
            }
        };

        buscarEnderecosEAdicionarMarcadores();
    }, [servicos]);

    return (
        <div className="mapa-overlay" onClick={onClose}>
            <div className="mapa-container" onClick={(e) => e.stopPropagation()}>
                <div className="mapa-header">
                    <h2>Localização dos Prestadores</h2>
                    <button className="mapa-close-btn" onClick={onClose}>✕</button>
                </div>
                {carregando && (
                    <div style={{ padding: '20px', textAlign: 'center', color: '#666', backgroundColor: '#f0f0f0' }}>
                        <p>Buscando endereços dos prestadores...</p>
                    </div>
                )}
                {!carregando && marcadoresEncontrados === 0 && servicos.length > 0 && (
                    <div style={{ padding: '20px', textAlign: 'center', color: '#d32f2f', backgroundColor: '#ffebee' }}>
                        <p><strong>Nenhum marcador encontrado.</strong></p>
                        <p style={{ fontSize: '0.9em', marginTop: '10px' }}>
                            Verifique se os prestadores têm endereços cadastrados com coordenadas válidas.
                            <br/>Abra o console do navegador (F12) para ver detalhes.
                        </p>
                    </div>
                )}
                <div ref={mapContainerRef} className="mapa-content" />
            </div>
        </div>
    );
};

export default MapaPrestadores;

