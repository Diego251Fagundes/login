package br.com.loginseguro.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Representa um usuario persistido no MongoDB.
 * A senha deve ser armazenada exclusivamente como hash.
 */
@Document(collection = "usuarios")
public class Usuario {

	@Id
	private String id;

	private String nome;

	private String sobrenome;

	@Indexed(unique = true)
	private String nomeUsuario;

	@Indexed(unique = true)
	private String email;

	private String senhaHash;

	private Role role;

	private boolean ativo;

	private int tentativasLoginInvalidas;

	private Instant bloqueadoAte;

	public Usuario() {
	}

	public Usuario(
			String nome,
			String sobrenome,
			String nomeUsuario,
			String email,
			String senhaHash,
			Role role,
			boolean ativo
	) {
		this.nome = nome;
		this.sobrenome = sobrenome;
		this.nomeUsuario = nomeUsuario;
		this.email = email;
		this.senhaHash = senhaHash;
		this.role = role;
		this.ativo = ativo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getSenhaHash() {
		return senhaHash;
	}

	public void setSenhaHash(String senhaHash) {
		this.senhaHash = senhaHash;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public int getTentativasLoginInvalidas() {
		return tentativasLoginInvalidas;
	}

	public void setTentativasLoginInvalidas(int tentativasLoginInvalidas) {
		this.tentativasLoginInvalidas = tentativasLoginInvalidas;
	}

	public Instant getBloqueadoAte() {
		return bloqueadoAte;
	}

	public void setBloqueadoAte(Instant bloqueadoAte) {
		this.bloqueadoAte = bloqueadoAte;
	}
}
