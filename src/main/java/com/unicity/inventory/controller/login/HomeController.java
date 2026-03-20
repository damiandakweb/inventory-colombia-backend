package com.unicity.inventory.controller.login;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class HomeController {

    @GetMapping("/home")
    public String home(){
        return "Private home";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String admin(){
        return "Admin";
    }
}
