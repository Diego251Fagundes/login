package br.com.loginseguro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Exibe a tela de autenticacao personalizada da aplicacao.
 */
@Controller
public class LoginController {

	@GetMapping("/login")
	public String exibirLogin() {
		return "login";
	}
}
