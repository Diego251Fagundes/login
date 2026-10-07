package br.com.loginseguro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.loginseguro.dto.CadastroUsuarioForm;
import br.com.loginseguro.exception.UsuarioJaCadastradoException;
import br.com.loginseguro.model.Role;
import br.com.loginseguro.model.Usuario;
import br.com.loginseguro.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTests {

	@Mock
	private UsuarioRepository usuarioRepository;

	private BCryptPasswordEncoder passwordEncoder;
	private UsuarioService usuarioService;

	@BeforeEach
	void setUp() {
		passwordEncoder = new BCryptPasswordEncoder(4);
		usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
	}

	@Test
	void deveCadastrarComPerfilSelecionadoIdentificadoresNormalizadosESenhaProtegida() {
		CadastroUsuarioForm form = criarFormValido();
		when(usuarioRepository.existsByNomeUsuario("maria.silva")).thenReturn(false);
		when(usuarioRepository.existsByEmail("maria@example.com")).thenReturn(false);
		when(usuarioRepository.save(any(Usuario.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		Usuario usuario = usuarioService.cadastrar(form);

		assertThat(usuario.getNome()).isEqualTo("Maria");
		assertThat(usuario.getSobrenome()).isEqualTo("Silva");
		assertThat(usuario.getNomeUsuario()).isEqualTo("maria.silva");
		assertThat(usuario.getEmail()).isEqualTo("maria@example.com");
		assertThat(usuario.getRole()).isEqualTo(Role.OPERADOR);
		assertThat(usuario.isAtivo()).isTrue();
		assertThat(usuario.getSenhaHash()).isNotEqualTo(form.getSenha());
		assertThat(passwordEncoder.matches(form.getSenha(), usuario.getSenhaHash())).isTrue();
	}

	@Test
	void naoDeveCadastrarQuandoAsSenhasForemDiferentes() {
		CadastroUsuarioForm form = criarFormValido();
		form.setConfirmacaoSenha("outra-senha");

		assertThatThrownBy(() -> usuarioService.cadastrar(form))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("A senha e a confirmacao devem ser iguais.");

		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void naoDeveCadastrarSemPerfil() {
		CadastroUsuarioForm form = criarFormValido();
		form.setRole(null);

		assertThatThrownBy(() -> usuarioService.cadastrar(form))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Selecione um perfil valido.");

		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void naoDeveCadastrarNomeDeUsuarioDuplicado() {
		CadastroUsuarioForm form = criarFormValido();
		when(usuarioRepository.existsByNomeUsuario("maria.silva")).thenReturn(true);

		assertThatThrownBy(() -> usuarioService.cadastrar(form))
				.isInstanceOf(UsuarioJaCadastradoException.class)
				.hasMessage("O nome de usuario ou e-mail informado ja esta cadastrado.");

		verify(usuarioRepository, never()).existsByEmail(any());
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void naoDeveCadastrarEmailDuplicado() {
		CadastroUsuarioForm form = criarFormValido();
		when(usuarioRepository.existsByNomeUsuario("maria.silva")).thenReturn(false);
		when(usuarioRepository.existsByEmail("maria@example.com")).thenReturn(true);

		assertThatThrownBy(() -> usuarioService.cadastrar(form))
				.isInstanceOf(UsuarioJaCadastradoException.class)
				.hasMessage("O nome de usuario ou e-mail informado ja esta cadastrado.");

		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void deveTratarConflitoDeIndiceUnico() {
		CadastroUsuarioForm form = criarFormValido();
		when(usuarioRepository.existsByNomeUsuario("maria.silva")).thenReturn(false);
		when(usuarioRepository.existsByEmail("maria@example.com")).thenReturn(false);
		when(usuarioRepository.save(any(Usuario.class)))
				.thenThrow(new DuplicateKeyException("E-mail duplicado"));

		assertThatThrownBy(() -> usuarioService.cadastrar(form))
				.isInstanceOf(UsuarioJaCadastradoException.class)
				.hasCauseInstanceOf(DuplicateKeyException.class);
	}

	private CadastroUsuarioForm criarFormValido() {
		CadastroUsuarioForm form = new CadastroUsuarioForm();
		form.setNome("  Maria  ");
		form.setSobrenome("  Silva  ");
		form.setNomeUsuario("  MARIA.SILVA  ");
		form.setEmail("  MARIA@EXAMPLE.COM  ");
		form.setRole(Role.OPERADOR);
		form.setSenha("senha-segura");
		form.setConfirmacaoSenha("senha-segura");
		return form;
	}
}
