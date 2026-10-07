package br.com.loginseguro.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import br.com.loginseguro.model.Role;

class CadastroUsuarioFormTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void deveRejeitarDadosInvalidos() {
		CadastroUsuarioForm form = new CadastroUsuarioForm();
		form.setNome(" ");
		form.setSobrenome(" ");
		form.setNomeUsuario("usuario invalido");
		form.setEmail("email-invalido");
		form.setSenha("123");
		form.setConfirmacaoSenha("456");

		Set<String> camposInvalidos = validator.validate(form).stream()
				.map(ConstraintViolation::getPropertyPath)
				.map(Object::toString)
				.collect(Collectors.toSet());

		assertThat(camposInvalidos).contains(
				"nome",
				"sobrenome",
				"nomeUsuario",
				"email",
				"role",
				"senha",
				"confirmacaoSenha",
				"senhaConfirmada"
		);
	}

	@Test
	void deveAceitarDadosValidos() {
		CadastroUsuarioForm form = new CadastroUsuarioForm();
		form.setNome("Maria");
		form.setSobrenome("Silva");
		form.setNomeUsuario("maria.silva");
		form.setEmail("maria@example.com");
		form.setRole(Role.OPERADOR);
		form.setSenha("senha-segura");
		form.setConfirmacaoSenha("senha-segura");

		assertThat(validator.validate(form)).isEmpty();
	}
}
