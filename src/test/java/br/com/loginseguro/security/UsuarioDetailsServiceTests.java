package br.com.loginseguro.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.loginseguro.model.Role;
import br.com.loginseguro.model.Usuario;
import br.com.loginseguro.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTests {

	@Mock
	private UsuarioRepository usuarioRepository;

	private UsuarioDetailsService usuarioDetailsService;

	@BeforeEach
	void setUp() {
		usuarioDetailsService = new UsuarioDetailsService(usuarioRepository);
	}

	@Test
	void deveCarregarUsuarioNormalizadoComPerfil() {
		Usuario usuario = new Usuario(
				"Maria",
				"Silva",
				"maria.silva",
				"maria@example.com",
				"hash-bcrypt",
				Role.PROFESSOR,
				true
		);
		when(usuarioRepository.findByNomeUsuario("maria.silva")).thenReturn(Optional.of(usuario));

		UserDetails detalhes = usuarioDetailsService.loadUserByUsername("  MARIA.SILVA  ");

		assertThat(detalhes.getUsername()).isEqualTo("maria.silva");
		assertThat(detalhes.getPassword()).isEqualTo("hash-bcrypt");
		assertThat(detalhes.getAuthorities())
				.extracting("authority")
				.containsExactly("ROLE_PROFESSOR");
		verify(usuarioRepository).findByNomeUsuario("maria.silva");
	}

	@Test
	void deveMarcarContaInativaComoDesabilitada() {
		Usuario usuario = criarUsuarioAtivo();
		usuario.setAtivo(false);
		when(usuarioRepository.findByNomeUsuario("maria.silva")).thenReturn(Optional.of(usuario));

		UserDetails detalhes = usuarioDetailsService.loadUserByUsername("maria.silva");

		assertThat(detalhes.isEnabled()).isFalse();
	}

	@Test
	void deveMarcarContaTemporariamenteBloqueada() {
		Usuario usuario = criarUsuarioAtivo();
		usuario.setBloqueadoAte(Instant.now().plusSeconds(60));
		when(usuarioRepository.findByNomeUsuario("maria.silva")).thenReturn(Optional.of(usuario));

		UserDetails detalhes = usuarioDetailsService.loadUserByUsername("maria.silva");

		assertThat(detalhes.isAccountNonLocked()).isFalse();
	}

	@Test
	void deveRejeitarNomeDeUsuarioInexistente() {
		when(usuarioRepository.findByNomeUsuario("inexistente")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("inexistente"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("Usuario nao encontrado.");
	}

	private Usuario criarUsuarioAtivo() {
		return new Usuario(
				"Maria",
				"Silva",
				"maria.silva",
				"maria@example.com",
				"hash-bcrypt",
				Role.PROFESSOR,
				true
		);
	}
}
