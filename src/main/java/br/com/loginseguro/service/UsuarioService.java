package br.com.loginseguro.service;

import java.util.Locale;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.loginseguro.dto.CadastroUsuarioForm;
import br.com.loginseguro.exception.UsuarioJaCadastradoException;
import br.com.loginseguro.model.Usuario;
import br.com.loginseguro.repository.UsuarioRepository;

/**
 * Aplica as regras do cadastro antes de persistir um usuario.
 */
@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Usuario cadastrar(CadastroUsuarioForm form) {
		if (!form.isSenhaConfirmada()) {
			throw new IllegalArgumentException("A senha e a confirmacao devem ser iguais.");
		}
		if (form.getRole() == null) {
			throw new IllegalArgumentException("Selecione um perfil valido.");
		}

		String nomeUsuarioNormalizado = normalizarNomeUsuario(form.getNomeUsuario());
		String emailNormalizado = normalizarEmail(form.getEmail());

		if (usuarioRepository.existsByNomeUsuario(nomeUsuarioNormalizado)
				|| usuarioRepository.existsByEmail(emailNormalizado)) {
			throw new UsuarioJaCadastradoException();
		}

		Usuario usuario = new Usuario(
				form.getNome().trim(),
				form.getSobrenome().trim(),
				nomeUsuarioNormalizado,
				emailNormalizado,
				passwordEncoder.encode(form.getSenha()),
				form.getRole(),
				true
		);

		try {
			return usuarioRepository.save(usuario);
		} catch (DuplicateKeyException exception) {
			throw new UsuarioJaCadastradoException(exception);
		}
	}

	private String normalizarNomeUsuario(String nomeUsuario) {
		return nomeUsuario.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
