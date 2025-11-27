import React, { useState, useEffect } from "react";

import "./dadosTotal.css";
import avatarExemplo from "../../../assets/dog3.webp";

interface DadosTotalProps {
  name?: string;
  email?: string;
  cpf?: string;
  nascimento?: string;
  celular?: string;
  pais?: string;
  estado?: string;
  bairro?: string;
  cep?: string;
  cidade?: string;
  endereco?: string;
  numero?: string;
  complemento?: string;
  avatarUrl?: string;
  onEditProfile?: (data: any) => void;
}

const DadosTotal: React.FC<DadosTotalProps> = ({
  name = "",
  email = "",
  cpf = "",
  nascimento = "",
  celular = "",
  pais = "",
  estado = "",
  bairro = "",
  cep = "",
  cidade = "",
  endereco = "",
  numero = "",
  complemento = "",
  avatarUrl,
  onEditProfile,
}) => {
  //  Estado para edição
  const [isEditing, setIsEditing] = useState(false);

  //  Estado do formulário (inicia com valores das props)
  const [formData, setFormData] = useState({
    name,
    email,
    cpf,
    nascimento,
    celular,
    pais,
    estado,
    bairro,
    cep,
    cidade,
    endereco,
    numero,
    complemento,
  });

  const [loading, setLoading] = useState(false);
  const [loadingCep, setLoadingCep] = useState(false);
  const [mensagem, setMensagem] = useState<{ type: 'success' | 'error', text: string } | null>(null);
  const [idUsuario, setIdUsuario] = useState<number | null>(null);
  const [idEndereco, setIdEndereco] = useState<number | null>(null);

  // Formatar CEP
  const formatarCep = (cep: string) => {
    const cepLimpo = cep.replace(/\D/g, '');
    if (cepLimpo.length <= 5) {
      return cepLimpo;
    }
    return `${cepLimpo.slice(0, 5)}-${cepLimpo.slice(5, 8)}`;
  };

  // Carregar dados do usuário e endereço ao montar o componente
  useEffect(() => {
    const carregarDados = async () => {
      try {
        const token = localStorage.getItem('authToken');
        if (!token) {
          setMensagem({ type: 'error', text: 'Você precisa estar logado para ver seus dados.' });
          return;
        }

        const payload = JSON.parse(atob(token.split(".")[1]));
        const idUsuarioToken = parseInt(payload.sub, 10);
        setIdUsuario(idUsuarioToken);

        // Buscar endereço existente
        const enderecoResponse = await fetch(`http://auwalk.us-east-2.elasticbeanstalk.com/enderecos?idUsuario=${idUsuarioToken}`);
        if (enderecoResponse.ok) {
          const enderecos = await enderecoResponse.json();
          if (Array.isArray(enderecos) && enderecos.length > 0) {
            const endereco = enderecos[0];
            setIdEndereco(endereco.idEndereco);
            
            // Formatar CEP se existir
            const cepFormatado = endereco.cep ? formatarCep(endereco.cep) : '';
            
            setFormData(prev => ({
              ...prev,
              pais: endereco.pais || prev.pais,
              estado: endereco.estado || prev.estado,
              cidade: endereco.cidade || prev.cidade,
              cep: cepFormatado || prev.cep,
              endereco: endereco.rua || endereco.logradouro || prev.endereco,
              numero: endereco.numero || prev.numero,
              complemento: endereco.complemento || prev.complemento,
              bairro: prev.bairro, // bairro não está no backend, manter o valor atual
            }));
          }
        }
      } catch (error) {
        console.error("Erro ao carregar dados:", error);
      }
    };

    carregarDados();
  }, []);

  // Função para buscar CEP via ViaCEP
  const buscarCep = async (cep: string) => {
    const cepLimpo = cep.replace(/\D/g, '');
    if (cepLimpo.length !== 8) return;

    setLoadingCep(true);
    try {
      const response = await fetch(`https://viacep.com.br/ws/${cepLimpo}/json/`);
      const data = await response.json();

      if (!data.erro) {
        const cepFormatado = formatarCep(cepLimpo);
        setFormData(prev => ({
          ...prev,
          cep: cepFormatado,
          endereco: data.logradouro || prev.endereco,
          bairro: data.bairro || prev.bairro,
          cidade: data.localidade || prev.cidade,
          estado: data.uf || prev.estado,
          pais: 'Brasil',
        }));

        setMensagem({ type: 'success', text: 'CEP encontrado! Endereço preenchido automaticamente.' });
        setTimeout(() => setMensagem(null), 3000);
      } else {
        setMensagem({ type: 'error', text: 'CEP não encontrado.' });
        setTimeout(() => setMensagem(null), 3000);
      }
    } catch (error) {
      console.error("Erro ao buscar CEP:", error);
      setMensagem({ type: 'error', text: 'Erro ao buscar CEP. Tente novamente.' });
      setTimeout(() => setMensagem(null), 3000);
    } finally {
      setLoadingCep(false);
    }
  };

  // Chave da API DistanceMatrix.ai (Geocoding Accurate)
  // Esta versão da API fornece geocodificação mais precisa
  const DISTANCEMATRIX_API_KEY = 'lL08D9LoaSEJwklv6yrNETb0jvxuPIopqx4P80LydXS3Nc1TCKKwTM4e8OjepWIn';

  // Função para obter coordenadas (geocodificação) usando DistanceMatrix.ai API
  const buscarCoordenadas = async (rua: string, cidade: string, estado: string, numero?: string, cep?: string) => {
    try {
      // Se tiver CEP, tentar obter dados completos do ViaCEP primeiro
      let bairroViaCep = '';
      if (cep && cep.replace(/\D/g, '').length === 8) {
        try {
          const viaCepResponse = await fetch(`https://viacep.com.br/ws/${cep.replace(/\D/g, '')}/json/`);
          const viaCepData = await viaCepResponse.json();
          
          if (!viaCepData.erro && viaCepData.localidade) {
            cidade = viaCepData.localidade || cidade;
            estado = viaCepData.uf || estado;
            bairroViaCep = viaCepData.bairro || '';
            if (viaCepData.logradouro && !rua) {
              rua = viaCepData.logradouro;
            }
          }
        } catch (error) {
          console.log('Erro ao buscar dados do ViaCEP:', error);
        }
      }

      // Construir variações do endereço para tentar
      const tentativas: string[] = [];
      const ruaLimpa = rua.trim();
      const numeroLimpo = numero?.trim();
      
      // PRIORIDADE 1: Endereço completo com número
      if (numeroLimpo && ruaLimpa) {
        tentativas.push(`${ruaLimpa}, ${numeroLimpo}, ${cidade}, ${estado}, Brasil`);
        tentativas.push(`${ruaLimpa} ${numeroLimpo}, ${cidade}, ${estado}, Brasil`);
      }
      
      // PRIORIDADE 2: Com bairro
      if (bairroViaCep && ruaLimpa) {
        if (numeroLimpo) {
          tentativas.push(`${ruaLimpa}, ${numeroLimpo}, ${bairroViaCep}, ${cidade}, ${estado}, Brasil`);
        }
        tentativas.push(`${ruaLimpa}, ${bairroViaCep}, ${cidade}, ${estado}, Brasil`);
      }
      
      // PRIORIDADE 3: Apenas rua
      if (ruaLimpa) {
        tentativas.push(`${ruaLimpa}, ${cidade}, ${estado}, Brasil`);
      }
      
      // PRIORIDADE 4: Com CEP
      if (cep && cep.replace(/\D/g, '').length === 8 && ruaLimpa) {
        tentativas.push(`${ruaLimpa}, ${cidade}, ${estado}, ${cep.replace(/\D/g, '')}, Brasil`);
      }
      
      // PRIORIDADE 5: Apenas cidade/estado (fallback)
      tentativas.push(`${cidade}, ${estado}, Brasil`);

      console.log(`🌍 Buscando coordenadas com DistanceMatrix.ai API (${tentativas.length} tentativas)`);

      // Tentar cada variação do endereço usando DistanceMatrix.ai
      for (const enderecoCompleto of tentativas) {
        if (!enderecoCompleto) continue;
        
        console.log(`🔍 Tentando geocodificar: ${enderecoCompleto}`);

        try {
          // URL base da API DistanceMatrix.ai (Geocoding Accurate)
          // Documentação: https://api.distancematrix.ai/maps/api/geocode/json
          const url = `https://api.distancematrix.ai/maps/api/geocode/json?address=${encodeURIComponent(enderecoCompleto)}&key=${DISTANCEMATRIX_API_KEY}&language=pt-BR`;
          
          console.log(`📡 Fazendo requisição para: ${url.replace(DISTANCEMATRIX_API_KEY, 'KEY_HIDDEN')}`);
          
          const response = await fetch(url);
          
          if (!response.ok) {
            console.warn(`⚠️ Erro HTTP ${response.status} ao buscar: ${enderecoCompleto}`);
            const errorText = await response.text();
            console.warn(`⚠️ Resposta de erro:`, errorText);
            continue;
          }
          
          const data = await response.json();
          console.log(`📥 Resposta da API:`, data);
          
          // A API DistanceMatrix.ai retorna resultados em diferentes formatos
          // Verificar ambos os formatos possíveis: 'results' ou 'result'
          let resultados = [];
          
          if (data.status === 'OK') {
            // Formato padrão da API: data.results (array)
            if (Array.isArray(data.results) && data.results.length > 0) {
              resultados = data.results;
              console.log(`📊 Usando formato 'results': ${resultados.length} resultado(s)`);
            } 
            // Formato alternativo: data.result (array)
            else if (Array.isArray(data.result) && data.result.length > 0) {
              resultados = data.result;
              console.log(`📊 Usando formato 'result': ${resultados.length} resultado(s)`);
            } else {
              console.warn(`⚠️ Nenhum resultado encontrado na resposta da API`);
            }
          } else {
            console.warn(`⚠️ Status da API: ${data.status}`);
            if (data.error_message) {
              console.warn(`⚠️ Mensagem de erro: ${data.error_message}`);
            }
          }
          
          if (resultados.length > 0) {
            // Usar o primeiro resultado (geralmente o mais relevante)
            const resultado = resultados[0];
            
            if (resultado.geometry && resultado.geometry.location) {
              const lat = resultado.geometry.location.lat;
              const lng = resultado.geometry.location.lng;
              const locationType = resultado.geometry.location_type || 'UNKNOWN';
              
              // Verificar a precisão da localização
              // ROOFTOP e RANGE_INTERPOLATED são mais precisos
              // APPROXIMATE é menos preciso
              const isPreciso = locationType === 'ROOFTOP' || locationType === 'RANGE_INTERPOLATED';
              
              console.log(`✅ Coordenadas encontradas!`, {
                latitude: lat,
                longitude: lng,
                enderecoEncontrado: resultado.formatted_address || enderecoCompleto,
                tipoPrecisao: locationType,
                isPreciso: isPreciso,
                enderecoBuscado: enderecoCompleto
              });
              
              // Validar se as coordenadas estão no Brasil
              if (lat >= -35 && lat <= 5 && lng >= -75 && lng <= -30) {
                // Se for preciso ou for a última tentativa, retornar
                if (isPreciso) {
                  console.log(`🎯 Coordenadas precisas encontradas (${locationType})!`);
                  return {
                    latitude: lat,
                    longitude: lng,
                  };
                } else {
                  // Se não for preciso, continuar tentando outras variações mais específicas
                  const indiceAtual = tentativas.indexOf(enderecoCompleto);
                  const restamTentativas = indiceAtual < tentativas.length - 1;
                  
                  if (restamTentativas && indiceAtual < 3) {
                    // Só continuar se ainda tiver tentativas mais específicas
                    console.log(`⚠️ Coordenadas encontradas mas não muito precisas (${locationType}). Tentando variação mais específica...`);
                    continue;
                  } else {
                    // Se não tiver mais tentativas específicas, usar mesmo assim
                    console.log(`⚠️ Usando coordenadas mesmo com precisão ${locationType} (última tentativa)`);
                    return {
                      latitude: lat,
                      longitude: lng,
                    };
                  }
                }
              } else {
                console.warn(`⚠️ Coordenadas fora do Brasil: lat=${lat}, lng=${lng}`);
                // Continuar tentando outras variações
                continue;
              }
            }
          } else {
            console.log(`❌ Nenhum resultado para: ${enderecoCompleto} (status: ${data.status})`);
            // Continuar para próxima tentativa
            continue;
          }
        } catch (error) {
          console.error(`❌ Erro ao buscar coordenadas para "${enderecoCompleto}":`, error);
          // Continuar para próxima tentativa
          continue;
        }
      }
      
      console.warn('⚠️ Não foi possível encontrar coordenadas para o endereço fornecido');
      
    } catch (error) {
      console.error("❌ Erro geral ao buscar coordenadas:", error);
    }
    
    return null;
  };

  //  Atualizar estado ao digitar
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    
    // Formatar CEP automaticamente
    if (name === 'cep') {
      const cepFormatado = formatarCep(value);
      setFormData((prev) => ({ ...prev, [name]: cepFormatado }));
      
      // Buscar CEP automaticamente quando tiver 8 dígitos
      const cepLimpo = cepFormatado.replace(/\D/g, '');
      if (cepLimpo.length === 8) {
        buscarCep(cepLimpo);
      }
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  // Salvar endereço
  const handleSave = async () => {
    if (!idUsuario) {
      setMensagem({ type: 'error', text: 'Erro: usuário não identificado.' });
      return;
    }

    setLoading(true);
    setMensagem(null);

    try {
      // Buscar coordenadas se ainda não tiver
      let latitude = -9.66625; // Coordenadas padrão de Maceió
      let longitude = -35.7351;

      if (formData.cidade && formData.estado) {
        setMensagem({ type: 'success', text: 'Buscando coordenadas do endereço...' });
        const cepLimpo = formData.cep ? formData.cep.replace(/\D/g, '') : undefined;
        const coords = await buscarCoordenadas(
          formData.endereco || '',
          formData.cidade,
          formData.estado,
          formData.numero || undefined,
          cepLimpo
        );
        if (coords) {
          latitude = coords.latitude;
          longitude = coords.longitude;
          console.log('✅ Coordenadas encontradas e serão salvas:', { 
            latitude, 
            longitude,
            endereco: `${formData.endereco || ''}, ${formData.numero || ''}, ${formData.cidade}, ${formData.estado}`
          });
        } else {
          console.warn('⚠️ Não foi possível encontrar coordenadas precisas, usando coordenadas padrão de Maceió');
          setMensagem({ type: 'error', text: 'Não foi possível encontrar coordenadas precisas. Usando localização padrão.' });
        }
      } else {
        console.log('Dados insuficientes para geocodificação, usando coordenadas padrão');
        setMensagem({ type: 'error', text: 'Preencha pelo menos cidade e estado para obter coordenadas precisas.' });
      }

      // Limpar CEP para salvar apenas números
      const cepLimpo = formData.cep ? formData.cep.replace(/\D/g, '') : null;

      const enderecoPayload = {
        idEndereco: idEndereco || 0,
        idUsuario: idUsuario,
        logradouro: formData.endereco || null,
        numero: formData.numero || null,
        cidade: formData.cidade || null,
        estado: formData.estado || null,
        cep: cepLimpo,
        rua: formData.endereco || null,
        complemento: formData.complemento || null,
        pais: formData.pais || 'Brasil',
        latitude: latitude,
        longitude: longitude,
      };

      console.log('📤 Enviando endereço para API:', {
        ...enderecoPayload,
        coordenadas: `(${latitude}, ${longitude})`
      });

      const endpoint = idEndereco
        ? `http://auwalk.us-east-2.elasticbeanstalk.com/enderecos/${idEndereco}`
        : 'http://auwalk.us-east-2.elasticbeanstalk.com/enderecos';
      const method = idEndereco ? 'PUT' : 'POST';

      const response = await fetch(endpoint, {
        method: method,
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(enderecoPayload),
      });

      const data = await response.json();

      if (response.ok) {
        setIsEditing(false);
        setMensagem({ type: 'success', text: 'Endereço salvo com sucesso!' });
        
        // Se criou um novo endereço, salvar o ID retornado
        if (!idEndereco && data.id_endereco) {
          setIdEndereco(data.id_endereco);
        }
        
        if (onEditProfile) {
          onEditProfile(formData);
        }
        
        // Limpar mensagem após 3 segundos
        setTimeout(() => setMensagem(null), 3000);
      } else {
        setMensagem({ type: 'error', text: data.message || 'Erro ao salvar endereço.' });
        setTimeout(() => setMensagem(null), 5000);
      }
    } catch (error) {
      console.error("Erro ao salvar endereço:", error);
      setMensagem({ type: 'error', text: 'Erro ao salvar endereço. Tente novamente.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dados-prestador-container">
      <header className="dados-prestador-header">
        <h2>
          <span className="palavra-destaque-dados">Primeira vez aqui?</span>
          Complete seu cadastro e aproveite todos os recursos.
        </h2>
        <img
          src={avatarUrl || avatarExemplo}
          alt={`${formData.name} avatar`}
          className="dados-prestador-avatar"
        />
      </header>

      <form className="dados-prestador-form">
        <div className="dados-prestador-group dados-prestador-group-up">
          <label>
            Nome completo
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            CPF
            <input
              type="text"
              name="cpf"
              value={formData.cpf}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            E-mail
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Celular
            <input
              type="tel"
              name="celular"
              value={formData.celular}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Nascimento
            <input
              type="date"
              name="nascimento"
              onChange={handleChange}
              value={formData.nascimento}
              disabled={!isEditing}
            />
          </label>
        </div>
        <div className="dados-h2">
          <h2>Endereço de Residência</h2>
        </div>
        <div className="dados-prestador-group dados-prestador-group-down">
          <label>
            País
            <input
              type="text"
              name="pais"
              value={formData.pais}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Estado
            <input
              type="text"
              name="estado"
              value={formData.estado}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Cidade
            <input
              type="text"
              name="cidade"
              value={formData.cidade}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Bairro
            <input
              type="text"
              name="bairro"
              value={formData.bairro}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            CEP
            <input
              type="text"
              name="cep"
              value={formData.cep}
              onChange={handleChange}
              disabled={!isEditing}
              placeholder="00000-000"
              maxLength={9}
            />
            {loadingCep && <small style={{ color: '#079fce' }}>Buscando CEP...</small>}
          </label>

          <label>
            Endereço
            <input
              type="text"
              name="endereco"
              value={formData.endereco}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Número
            <input
              type="text"
              name="numero"
              value={formData.numero}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          <label>
            Complemento
            <input
              type="text"
              name="complemento"
              value={formData.complemento}
              onChange={handleChange}
              disabled={!isEditing}
            />
          </label>

          {mensagem && (
            <div
              style={{
                padding: '10px',
                borderRadius: '6px',
                backgroundColor: mensagem.type === 'success' ? '#d4edda' : '#f8d7da',
                color: mensagem.type === 'success' ? '#155724' : '#721c24',
                width: '100%',
                textAlign: 'center',
                marginTop: '10px',
              }}
            >
              {mensagem.text}
            </div>
          )}

          {isEditing ? (
            <button
              type="button"
              onClick={handleSave}
              className="dados-prestador-btn-salvar"
              disabled={loading}
            >
              {loading ? 'Salvando...' : 'Salvar'}
            </button>
          ) : (
            <button
              type="button"
              onClick={() => setIsEditing(true)}
              className="dados-prestador-edit-profile-btn"
            >
              Editar Perfil
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default DadosTotal;
