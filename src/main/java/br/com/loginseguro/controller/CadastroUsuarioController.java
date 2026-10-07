package br.com.loginseguro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.loginseguro.dto.CadastroUsuarioForm;
import br.com.loginseguro.exception.UsuarioJaCadastradoException;
import br.com.loginseguro.model.Role;
import br.com.loginseguro.service.UsuarioService;
import jakarta.validation.Valid;

/**
 * Disponibiliza o cadastro administrativo e encaminha os dados validados ao serviço de usuários.
 */
@Controller
@RequestMapping("/admin/usuarios")
public class CadastroUsuarioController {

	private final UsuarioService usuarioService;

	public CadastroUsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@ModelAttribute("perfis")
	public Role[] perfis() {
		return Role.values();
	}

	@GetMapping("/cadastro")
	public String exibirFormulario(Model model) {
		if (!model.containsAttribute("cadastroUsuarioForm")) {
			model.addAttribute("cadastroUsuarioForm", new CadastroUsuarioForm());
		}
		return "admin/usuarios/cadastro";
	}

	@PostMapping("/cadastro")
	public String cadastrar(
			@Valid @ModelAttribute("cadastroUsuarioForm") CadastroUsuarioForm form,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes
	) {
		if (bindingResult.hasErrors()) {
			return "admin/usuarios/cadastro";
		}

		try {
			usuarioService.cadastrar(form);
		} catch (UsuarioJaCadastradoException | IllegalArgumentException exception) {
			bindingResult.reject("cadastro.invalido", exception.getMessage());
			return "admin/usuarios/cadastro";
		}

		redirectAttributes.addFlashAttribute("mensagemSucesso", "Usuário cadastrado com sucesso");
		return "redirect:/admin/usuarios/cadastro";
	}
}
