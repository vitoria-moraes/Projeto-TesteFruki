package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clima real via OpenWeatherMap, com três proteções para o pitch:
 *
 * <ol>
 *   <li><b>timeout curto</b> (1,5 s por padrão) — não trava a tela;</li>
 *   <li><b>cache</b> de 10 minutos — evita chamadas repetidas;</li>
 *   <li><b>fallback</b> — se a internet ou a chave falharem, devolve uma
 *       temperatura padrão e a resposta continua funcionando.</li>
 * </ol>
 *
 * <p>A chave está em application.properties apenas para o protótipo. Em produção,
 * use a variável de ambiente {@code OPENWEATHER_API_KEY} e rotacione a chave.</p>
 */
@Service
public class ClimaService {

    private static final Logger log = LoggerFactory.getLogger(ClimaService.class);

    private final String apiKey;
    private final String baseUrl;
    private final long timeoutMs;
    private final long cacheMinutes;
    private final double fallbackTemperatura;
    private final double demoLatitude;
    private final double demoLongitude;
    private final String demoCidade;
    private final RestClient restClient;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public ClimaService(
            @Value("${openweather.api-key:}") String apiKey,
            @Value("${openweather.base-url:https://api.openweathermap.org/data/2.5/weather}") String baseUrl,
            @Value("${openweather.timeout-ms:1500}") long timeoutMs,
            @Value("${openweather.cache-minutes:10}") long cacheMinutes,
            @Value("${openweather.fallback-temperatura:24.0}") double fallbackTemperatura,
            @Value("${demo.latitude:-27.618954}") double demoLatitude,
            @Value("${demo.longitude:-52.268404}") double demoLongitude,
            @Value("${demo.cidade:Erechim}") String demoCidade) {

        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.cacheMinutes = cacheMinutes;
        this.fallbackTemperatura = fallbackTemperatura;
        this.demoLatitude = demoLatitude;
        this.demoLongitude = demoLongitude;
        this.demoCidade = demoCidade;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) timeoutMs);
        factory.setReadTimeout((int) timeoutMs);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /** Clima de uma coordenada, pronto para ir ao JSON. */
    public record Clima(double temperatura, String descricao, String cidade, String fonte) {
    }

    /** Clima do cliente de demonstração (Erechim/RS). */
    public Clima paraDemo() {
        return buscar(demoLatitude, demoLongitude, demoCidade);
    }

    /** Clima de uma coordenada qualquer (usado pelos perfis de CNPJ). */
    public Clima paraCoordenada(double latitude, double longitude, String cidade) {
        return buscar(latitude, longitude, cidade);
    }

    /** Clima do cliente real, usando as coordenadas de dim_cliente quando existirem. */
    public Clima paraCliente(Cliente cliente) {
        if (cliente == null || cliente.getLatitude() == null || cliente.getLongitude() == null) {
            return fallback();
        }
        return buscar(cliente.getLatitude(), cliente.getLongitude(), cliente.getCidade());
    }

    private Clima buscar(double latitude, double longitude, String cidadePadrao) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback();
        }

        String chaveCache = Math.round(latitude * 10_000) + ":" + Math.round(longitude * 10_000);
        CacheEntry emCache = cache.get(chaveCache);
        if (emCache != null && emCache.expiraEm().isAfter(Instant.now())) {
            return emCache.clima();
        }

        try {
            OpenWeatherResponse resposta = restClient.get()
                    .uri(baseUrl + "?lat={lat}&lon={lon}&units=metric&lang=pt_br&appid={key}",
                            latitude, longitude, apiKey)
                    .retrieve()
                    .body(OpenWeatherResponse.class);

            if (resposta == null || resposta.main() == null) {
                return fallback();
            }

            String descricao = (resposta.weather() != null && !resposta.weather().isEmpty())
                    ? resposta.weather().get(0).description()
                    : "";
            String cidade = (resposta.name() != null && !resposta.name().isBlank())
                    ? resposta.name()
                    : cidadePadrao;

            Clima clima = new Clima(resposta.main().temp(), descricao, cidade, "OpenWeatherMap");
            cache.put(chaveCache, new CacheEntry(clima, Instant.now().plus(Duration.ofMinutes(cacheMinutes))));
            return clima;
        } catch (Exception ex) {
            log.warn("Não foi possível consultar o OpenWeatherMap em {}:{} — usando fallback. Causa: {}",
                    latitude, longitude, ex.getMessage());
            return fallback();
        }
    }

    private Clima fallback() {
        return new Clima(fallbackTemperatura, "indisponível", "—", "fallback");
    }

    private record CacheEntry(Clima clima, Instant expiraEm) {
    }
}
