package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Subconjunto da resposta da API OpenWeatherMap que realmente usamos.
 * Campos desconhecidos são ignorados para a API poder evoluir sem quebrar o app.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(Main main, List<Weather> weather, String name) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(double temp) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weather(String description, String icon) {
    }
}
