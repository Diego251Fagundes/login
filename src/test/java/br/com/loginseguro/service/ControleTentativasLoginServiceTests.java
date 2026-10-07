package br.com.loginseguro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.loginseguro.model.Role;
import br.com.loginseguro.model.Usuario;
import br.com.loginseguro.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ControleTentativasLoginServiceTests {

	@Mock
	private UsuarioRepository usuarioRepository;

	private ControleTentativasLoginService controleTentativasLoginService;

	@BeforeEach
	void setUp() {
		controleTentativasLoginService = new ControleTentativasLoginService(usuarioRepository);
	}

	@Test
	void deveBloquearPorQuinzeMinutosNaQuintaFalha() {
		Usuario usuario = criarUsuario();
		usuario.setTentativasLoginInvalidas(4);
		when(usuarioRepository.findByNomeUsuario("maria.silva")).thenReturn(Optional.of(usuario));
		Instant limiteInferior = Instant.now().plusSeconds(14 * 60);

		controleTentativasLoginService.registrarFalha("  MARIA.SILVA  ");

		assertThat(usuario.getTentativasLoginInvalidas()).isEqualTo(5);
		assertThat(usuario.getBloqueadoAte()).isAfter(limiteInferior);
		assertThat(usuario.getBloqueadoAte()).isBefore(Instant.now().plusSeconds(16 * 60));
		verify(usuarioRepository).save(usuario);
	}

	@Test
	void deveReiniciarContagemAposLoginValido() {
		Usuario usuario = criarUsuario();
		usuario.setTentativasLoginInvalidas(3);
		usuario.setBloqueadoAte(Instant.now().minusSeconds(1));
		when(usuarioRepository.findByNomeUsuario("maria.silva")).thenReturn(Optional.of(usuario));

		controleTentativasLoginService.registrarSucesso("maria.silva");

		assertThat(usuario.getTentativasLoginInvalidas()).isZero();
		assertThat(usuario.getBloqueadoAte()).isNull();
		verify(usuarioRepository).save(usuario);
	}

	private Usuario criarUsuario() {
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
