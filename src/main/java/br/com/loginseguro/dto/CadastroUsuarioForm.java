package br.com.loginseguro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import br.com.loginseguro.model.Role;

/**
 * Dados recebidos pelo formulario de cadastro de usuario.
 */
public class CadastroUsuarioForm {

	@NotBlank(message = "Informe o nome.")
	@Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
	private String nome;

	@NotBlank(message = "Informe o sobrenome.")
	@Size(min = 2, max = 100, message = "O sobrenome deve ter entre 2 e 100 caracteres.")
	private String sobrenome;

	@NotBlank(message = "Informe o nome de usuario.")
	@Size(min = 3, max = 30, message = "O nome de usuario deve ter entre 3 e 30 caracteres.")
	@Pattern(
			regexp = "^[A-Za-z0-9._-]+$",
			message = "O nome de usuario deve conter apenas letras, numeros, ponto, hifen ou sublinhado."
	)
	private String nomeUsuario;

	@NotBlank(message = "Informe o e-mail.")
	@Email(message = "Informe um e-mail valido.")
	@Size(max = 254, message = "O e-mail deve ter no maximo 254 caracteres.")
	private String email;

	@NotNull(message = "Selecione o perfil.")
	private Role role;

	@NotBlank(message = "Informe a senha.")
	@Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
	private String senha;

	@NotBlank(message = "Confirme a senha.")
	@Size(min = 8, max = 72, message = "A confirmacao deve ter entre 8 e 72 caracteres.")
	private String confirmacaoSenha;

	@AssertTrue(message = "A senha e a confirmacao devem ser iguais.")
	public boolean isSenhaConfirmada() {
		return senha != null && senha.equals(confirmacaoSenha);
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSobrenome() {
		return sobrenome;
	}

	public void setSobrenome(String sobrenome) {
		this.sobrenome = sobrenome;
	}

	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getConfirmacaoSenha() {
		return confirmacaoSenha;
	}

	public void setConfirmacaoSenha(String confirmacaoSenha) {
		this.confirmacaoSenha = confirmacaoSenha;
	}
}
