package br.com.loginseguro.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.loginseguro.model.Role;
import br.com.loginseguro.model.Usuario;
import br.com.loginseguro.repository.UsuarioRepository;

@SpringBootTest(properties = {
		"app.session.mongodb.enabled=false",
		"spring.data.mongodb.auto-index-creation=false",
		"spring.mongodb.uri=mongodb://localhost:27017/login_seguro_test"
})
@AutoConfigureMockMvc
class SecurityConfigTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@Test
	void deveExibirTelaPersonalizadaDeLoginSemCadastroPublico() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(content().string(containsString("Nome de usuário")))
				.andExpect(content().string(containsString("name=\"username\"")))
				.andExpect(content().string(containsString("name=\"password\"")))
				.andExpect(content().string(not(containsString("Criar conta"))));
	}

	@Test
	void deveExibirMensagemNeutraQuandoLoginFalhar() throws Exception {
		mockMvc.perform(get("/login").param("error", ""))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Nome de usuário ou senha inválidos.")));
	}

	@Test
	void deveRedirecionarUsuarioAnonimoParaLogin() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@ParameterizedTest
	@EnumSource(Role.class)
	void deveAutenticarOsTresPerfis(Role role) throws Exception {
		String nomeUsuario = "usuario-" + role.name().toLowerCase(Locale.ROOT);
		Usuario usuario = criarUsuario(nomeUsuario, "senha-segura", role, true);
		when(usuarioRepository.findByNomeUsuario(nomeUsuario)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		mockMvc.perform(post("/login")
						.with(csrf())
						.param("username", nomeUsuario)
						.param("password", "senha-segura"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(authenticated().withUsername(nomeUsuario));
	}

	@Test
	void deveRejeitarCredenciaisInvalidas() throws Exception {
		Usuario usuario = criarUsuario("maria.silva", "senha-segura", Role.PROFESSOR, true);
		when(usuarioRepository.findByNomeUsuario("maria.silva")).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		mockMvc.perform(post("/login")
						.with(csrf())
						.param("username", "maria.silva")
						.param("password", "senha-incorreta"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}

	@Test
	void deveRejeitarContaInativa() throws Exception {
		Usuario usuario = criarUsuario("maria.silva", "senha-segura", Role.PROFESSOR, false);
		when(usuarioRepository.findByNomeUsuario("maria.silva")).thenReturn(Optional.of(usuario));

		mockMvc.perform(post("/login")
						.with(csrf())
						.param("username", "maria.silva")
						.param("password", "senha-segura"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}

	@Test
	void deveBloquearAposCincoTentativasInvalidas() throws Exception {
		Usuario usuario = criarUsuario("maria.silva", "senha-segura", Role.PROFESSOR, true);
		when(usuarioRepository.findByNomeUsuario("maria.silva")).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		for (int tentativa = 0; tentativa < 5; tentativa++) {
			mockMvc.perform(post("/login")
							.with(csrf())
							.param("username", "maria.silva")
							.param("password", "senha-incorreta"))
					.andExpect(unauthenticated());
		}

		assertThat(usuario.getBloqueadoAte()).isNotNull();
		mockMvc.perform(post("/login")
						.with(csrf())
						.param("username", "maria.silva")
						.param("password", "senha-segura"))
				.andExpect(redirectedUrl("/login?error"))
				.andExpect(unauthenticated());
	}

	@Test
	void devePermitirAcessoAoCadastroSomenteParaAdministrador() throws Exception {
		mockMvc.perform(get("/admin/usuarios/cadastro")
						.with(user("admin").roles("ADMINISTRADOR")))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/usuarios/cadastro"));

		mockMvc.perform(get("/admin/usuarios/cadastro")
						.with(user("operador").roles("OPERADOR")))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/admin/usuarios/cadastro")
						.with(user("professor").roles("PROFESSOR")))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/admin/usuarios/cadastro"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void administradorDeveCadastrarUsuarioComPerfilSelecionado() throws Exception {
		when(usuarioRepository.existsByNomeUsuario("novo.operador")).thenReturn(false);
		when(usuarioRepository.existsByEmail("operador@example.com")).thenReturn(false);
		when(usuarioRepository.save(any(Usuario.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		mockMvc.perform(post("/admin/usuarios/cadastro")
						.with(user("admin").roles("ADMINISTRADOR"))
						.with(csrf())
						.param("nome", "Novo")
						.param("sobrenome", "Operador")
						.param("email", "OPERADOR@EXAMPLE.COM")
						.param("nomeUsuario", "NOVO.OPERADOR")
						.param("role", "OPERADOR")
						.param("senha", "senha-segura")
						.param("confirmacaoSenha", "senha-segura"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/usuarios/cadastro"))
				.andExpect(flash().attribute("mensagemSucesso", "Usuário cadastrado com sucesso"));

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(captor.capture());
		Usuario usuarioSalvo = captor.getValue();
		assertThat(usuarioSalvo.getRole()).isEqualTo(Role.OPERADOR);
		assertThat(usuarioSalvo.getNomeUsuario()).isEqualTo("novo.operador");
		assertThat(usuarioSalvo.getEmail()).isEqualTo("operador@example.com");
		assertThat(usuarioSalvo.isAtivo()).isTrue();
		assertThat(usuarioSalvo.getSenhaHash()).isNotEqualTo("senha-segura");
		assertThat(passwordEncoder.matches("senha-segura", usuarioSalvo.getSenhaHash())).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = Role.class, names = {"OPERADOR", "PROFESSOR"})
	void perfisNaoAdministrativosNaoDevemCadastrar(Role role) throws Exception {
		mockMvc.perform(post("/admin/usuarios/cadastro")
						.with(user("usuario").roles(role.name()))
						.with(csrf())
						.param("nome", "Novo")
						.param("sobrenome", "Usuario")
						.param("email", "novo@example.com")
						.param("nomeUsuario", "novo.usuario")
						.param("role", "PROFESSOR")
						.param("senha", "senha-segura")
						.param("confirmacaoSenha", "senha-segura"))
				.andExpect(status().isForbidden());

		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void deveRejeitarPerfilDesconhecido() throws Exception {
		mockMvc.perform(post("/admin/usuarios/cadastro")
						.with(user("admin").roles("ADMINISTRADOR"))
						.with(csrf())
						.param("nome", "Novo")
						.param("sobrenome", "Usuario")
						.param("email", "novo@example.com")
						.param("nomeUsuario", "novo.usuario")
						.param("role", "DESCONHECIDO")
						.param("senha", "senha-segura")
						.param("confirmacaoSenha", "senha-segura"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/usuarios/cadastro"));

		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void deveExigirCsrfNoCadastro() throws Exception {
		mockMvc.perform(post("/admin/usuarios/cadastro")
						.with(user("admin").roles("ADMINISTRADOR")))
				.andExpect(status().isForbidden());
	}

	@ParameterizedTest
	@EnumSource(Role.class)
	void deveExibirSomenteAsOpcoesDoPerfilNaPaginaInicial(Role role) throws Exception {
		var resposta = mockMvc.perform(get("/").with(user("usuario").roles(role.name())))
				.andExpect(status().isOk())
				.andExpect(view().name("home"));

		for (Role perfil : Role.values()) {
			String rota = switch (perfil) {
				case ADMINISTRADOR -> "/admin/usuarios/cadastro";
				case OPERADOR -> "/operador/inicio";
				case PROFESSOR -> "/professor/inicio";
			};
			if (perfil == role) {
				resposta.andExpect(content().string(containsString("href=\"" + rota + "\"")));
			} else {
				resposta.andExpect(content().string(not(containsString("href=\"" + rota + "\""))));
			}
		}
	}

	@ParameterizedTest
	@EnumSource(Role.class)
	void deveAplicarPermissoesNasPaginasDeOperadorEProfessor(Role role) throws Exception {
		for (Role perfil : new Role[] {Role.OPERADOR, Role.PROFESSOR}) {
			String rota = perfil == Role.OPERADOR ? "/operador/inicio" : "/professor/inicio";
			var resposta = mockMvc.perform(get(rota).with(user("usuario").roles(role.name())));
			if (perfil == role) {
				resposta.andExpect(status().isOk()).andExpect(view().name("perfil"));
			} else {
				resposta.andExpect(status().isForbidden());
			}
		}
	}

	@Test
	void deveExigirCsrfNoLoginELogout() throws Exception {
		mockMvc.perform(post("/login").param("username", "usuario").param("password", "senha-segura"))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/logout").with(user("usuario").roles("OPERADOR")))
				.andExpect(status().isForbidden());
	}

	@Test
	void deveEncerrarASessaoNoLogout() throws Exception {
		mockMvc.perform(post("/logout")
						.with(user("maria.silva").roles("PROFESSOR"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?logout"))
				.andExpect(unauthenticated());
	}

	private Usuario criarUsuario(String nomeUsuario, String senha, Role role, boolean ativo) {
		return new Usuario(
				"Usuario",
				"de Teste",
				nomeUsuario,
				nomeUsuario + "@example.com",
				passwordEncoder.encode(senha),
				role,
				ativo
		);
	}
}
