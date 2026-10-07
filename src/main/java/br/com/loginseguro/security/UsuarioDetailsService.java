package br.com.loginseguro.security;

import java.time.Instant;
import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.loginseguro.model.Usuario;
import br.com.loginseguro.repository.UsuarioRepository;

/**
 * Carrega do MongoDB os dados usados pelo Spring Security na autenticacao.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String nomeUsuario) throws UsernameNotFoundException {
		String nomeUsuarioNormalizado = nomeUsuario.trim().toLowerCase(Locale.ROOT);

		Usuario usuario = usuarioRepository.findByNomeUsuario(nomeUsuarioNormalizado)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado."));
		boolean bloqueado = usuario.getBloqueadoAte() != null
				&& usuario.getBloqueadoAte().isAfter(Instant.now());

		return User.withUsername(usuario.getNomeUsuario())
				.password(usuario.getSenhaHash())
				.roles(usuario.getRole().name())
				.disabled(!usuario.isAtivo())
				.accountLocked(bloqueado)
				.build();
	}
}
