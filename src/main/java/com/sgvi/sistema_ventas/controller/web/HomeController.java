package com.sgvi.sistema_ventas.controller.web;

import org.springframework.web.bind.annotation.GetMapping;

public class HomeController {
    @GetMapping("/")
    public String home() {
        return "Hola, sistema activo!";
    }
}
