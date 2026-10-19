package org.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String raiz() {
        // Página de escolha entre os dois fluxos da demonstração.
        return "fluxos";
    }

    @GetMapping("/whatsapp")
    public String abrirWhatsappCompleto() {
        return "chat-whats"; // fluxo 1: recomendação + catálogo + carrinho dentro do chat
    }

    @GetMapping("/chat")
    public String abrirChat() {
        return "chat"; // fluxo 2: WhatsApp como recomendador + link para a tela de catálogo
    }

    @GetMapping("/catalogo")
    public String abrirCatalogo() {
        return "catalogo"; // tela de menu, carrinho e fechamento do pedido
    }

    @GetMapping("/promocoes")
    public String abrirPromocoes() {
        return "promocoes"; // os três templates de disparo contextual do WhatsApp
    }

    @GetMapping("/agenda")
    public String abrirAgenda() {
        return "agenda"; // um evento contextual para cada dia dos próximos sete dias
    }

    @GetMapping("/chat-remoto")
    public String abrirChatRemoto() {
        return "chat-remoto"; // UI alternativa mantida do merge
    }
}
