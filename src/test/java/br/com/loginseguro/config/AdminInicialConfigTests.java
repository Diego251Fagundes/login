package br.com.loginseguro.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.loginseguro.model.Role;
import br.com.loginseguro.model.Usuario;
import br.com.loginseguro.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdminInicialConfigTests {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Test
	void deveCriarAdministradorInicialComSenhaProtegida() throws Exception {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
		when(usuarioRepository.findByNomeUsuario("admin.login")).thenReturn(Optional.empty());
		when(usuarioRepository.existsByEmail("admin@example.com")).thenReturn(false);
		when(usuarioRepository.save(any(Usuario.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));
		AdminInicialConfig config = new AdminInicialConfig();
		ApplicationRunner runner = config.criarAdminInicial(
				usuarioRepository,
				passwordEncoder,
				"Administrador",
				"Sistema",
				"ADMIN@EXAMPLE.COM",
				"ADMIN.LOGIN",
				"senha-segura"
		);

		runner.run(null);

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(captor.capture());
		Usuario administrador = captor.getValue();
		assertThat(administrador.getNomeUsuario()).isEqualTo("admin.login");
		assertThat(administrador.getEmail()).isEqualTo("admin@example.com");
		assertThat(administrador.getRole()).isEqualTo(Role.ADMINISTRADOR);
		assertThat(administrador.isAtivo()).isTrue();
		assertThat(administrador.getSenhaHash()).isNotEqualTo("senha-segura");
		assertThat(passwordEncoder.matches("senha-segura", administrador.getSenhaHash())).isTrue();
	}
}
