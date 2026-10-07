package br.com.loginseguro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Exibe a página inicial após a autenticação do usuário.
 */
@Controller
public class HomeController {

	@GetMapping("/")
	public String home() {
		return "home";
	}
}
