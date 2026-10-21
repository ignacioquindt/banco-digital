package com.ignacio.banco.comun;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class NavegacionController {
    @GetMapping("/") public String inicio() { return "redirect:/index.html"; }
    @GetMapping("/login") public String login() { return "redirect:/login.html"; }
}
