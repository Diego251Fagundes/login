package br.com.loginseguro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Compartilha somente dados visuais com os templates. Trocar nome ou tema nao
 * altera as regras de autenticacao, autorizacao ou cadastro.
 */
@ControllerAdvice
public class InterfaceConfig {

	@Value("${app.interface.nome}")
	private String nome;

	@Value("${app.interface.tema-css}")
	private String temaCss;

	@ModelAttribute("nomeAplicacao")
	public String nomeAplicacao() {
		return nome;
	}

	@ModelAttribute("temaCss")
	public String temaCss() {
		return temaCss;
	}
}
