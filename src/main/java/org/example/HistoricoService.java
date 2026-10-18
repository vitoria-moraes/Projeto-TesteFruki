package org.example;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Histórico de compras mock. O cliente 999 é fictício e também é criado no RecomendacaoService.
 * Os códigos de item (00001, 00209, 00417) existem em dim_item.csv.
 */
@Service
public class HistoricoService {

    private static final Map<Long, List<HistoricoCompra>> MOCK = Map.of(
            999L, List.of(
                    new HistoricoCompra(999L, "00001", LocalDate.now().minusDays(10), 24),
                    new HistoricoCompra(999L, "00209", LocalDate.now().minusDays(3), 12),
                    new HistoricoCompra(999L, "00417", LocalDate.now().minusDays(21), 48)
            )
    );

    public List<HistoricoCompra> buscar(Long codCliente) {
        return MOCK.getOrDefault(codCliente, List.of());
    }
}
