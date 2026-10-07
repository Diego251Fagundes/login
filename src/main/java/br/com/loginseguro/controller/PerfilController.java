package br.com.loginseguro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Paginas simples para demonstrar as rotas restritas dos perfis nao administrativos.
 * As permissoes sao aplicadas no servidor pelo SecurityConfig.
 */
@Controller
public class PerfilController {

	@GetMapping("/operador/inicio")
	public String operador(Model model) {
		model.addAttribute("perfil", "Operador");
		return "perfil";
	}

	@GetMapping("/professor/inicio")
	public String professor(Model model) {
		model.addAttribute("perfil", "Professor");
		return "perfil";
	}
}
