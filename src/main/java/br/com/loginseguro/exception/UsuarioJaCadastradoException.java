package br.com.loginseguro.exception;

/**
 * Indica que o nome de usuario ou o e-mail ja pertence a outro cadastro.
 */
public class UsuarioJaCadastradoException extends RuntimeException {

	public UsuarioJaCadastradoException() {
		super("O nome de usuario ou e-mail informado ja esta cadastrado.");
	}

	public UsuarioJaCadastradoException(Throwable cause) {
		super("O nome de usuario ou e-mail informado ja esta cadastrado.", cause);
	}
}
