package org.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(ExcelReaderService excelReaderService) {
        return args -> {
            System.out.println("--- 🚀 A CARREGAR TODAS AS PLANILHAS DO DESAFIO GC 2026 ---");
            excelReaderService.lerClientesDoCsv();
            excelReaderService.lerProdutosDoCsv();
            excelReaderService.lerHierarquiaDoCsv();
            excelReaderService.lerTabelaPrecoDoCsv();
            System.out.println("--- ✨ INGESTÃO CONCLUÍDA COM SUCESSO! ---");
        };
    }
}