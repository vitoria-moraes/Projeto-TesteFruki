package org.example;

import java.time.LocalDate;

public record HistoricoCompra(Long codCliente, String codItem, LocalDate data, int quantidade) {
}
