package org.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/chat")
    public String abrirChat() {
        return "chat"; // Procura por chat.html em resources/templates/
    }
}