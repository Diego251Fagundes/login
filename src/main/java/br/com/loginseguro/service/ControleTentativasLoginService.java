package br.com.loginseguro.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.springframework.stereotype.Service;

import br.com.loginseguro.model.Usuario;
import br.com.loginseguro.repository.UsuarioRepository;

/**
 * Controla o bloqueio temporario causado por tentativas de login invalidas.
 */
@Service
public class ControleTentativasLoginService {

	static final int LIMITE_TENTATIVAS = 5;
	static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);

	private final UsuarioRepository usuarioRepository;

	public ControleTentativasLoginService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	public void registrarFalha(String nomeUsuario) {
		usuarioRepository.findByNomeUsuario(normalizar(nomeUsuario))
				.ifPresent(this::incrementarTentativas);
	}

	public void registrarSucesso(String nomeUsuario) {
		usuarioRepository.findByNomeUsuario(normalizar(nomeUsuario))
				.ifPresent(usuario -> {
					usuario.setTentativasLoginInvalidas(0);
					usuario.setBloqueadoAte(null);
					usuarioRepository.save(usuario);
				});
	}

	private void incrementarTentativas(Usuario usuario) {
		Instant agora = Instant.now();

		if (usuario.getBloqueadoAte() != null && !usuario.getBloqueadoAte().isAfter(agora)) {
			usuario.setTentativasLoginInvalidas(0);
			usuario.setBloqueadoAte(null);
		}

		int tentativas = usuario.getTentativasLoginInvalidas() + 1;
		usuario.setTentativasLoginInvalidas(tentativas);

		if (tentativas >= LIMITE_TENTATIVAS) {
			usuario.setBloqueadoAte(agora.plus(DURACAO_BLOQUEIO));
		}

		usuarioRepository.save(usuario);
	}

	private String normalizar(String nomeUsuario) {
		return nomeUsuario.trim().toLowerCase(Locale.ROOT);
	}
}
