package br.com.loginseguro.config;

import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.loginseguro.model.Role;
import br.com.loginseguro.model.Usuario;
import br.com.loginseguro.repository.UsuarioRepository;

/**
 * Permite criar o primeiro administrador sem liberar cadastro publico.
 */
@Configuration
@ConditionalOnProperty(name = "app.admin-inicial.enabled", havingValue = "true")
public class AdminInicialConfig {

	@Bean
	public ApplicationRunner criarAdminInicial(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.admin-inicial.nome}") String nome,
			@Value("${app.admin-inicial.sobrenome}") String sobrenome,
			@Value("${app.admin-inicial.email}") String email,
			@Value("${app.admin-inicial.nome-usuario}") String nomeUsuario,
			@Value("${app.admin-inicial.senha}") String senha
	) {
		return argumentos -> {
			validarConfiguracao(nome, sobrenome, email, nomeUsuario, senha);

			String nomeUsuarioNormalizado = nomeUsuario.trim().toLowerCase(Locale.ROOT);
			String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
			Optional<Usuario> usuarioExistente = usuarioRepository.findByNomeUsuario(nomeUsuarioNormalizado);

			if (usuarioExistente.isPresent()) {
				if (usuarioExistente.get().getRole() != Role.ADMINISTRADOR) {
					throw new IllegalStateException("O nome de usuario do administrador inicial ja esta em uso.");
				}
				return;
			}

			if (usuarioRepository.existsByEmail(emailNormalizado)) {
				throw new IllegalStateException("O e-mail do administrador inicial ja esta em uso.");
			}

			Usuario administrador = new Usuario(
					nome.trim(),
					sobrenome.trim(),
					nomeUsuarioNormalizado,
					emailNormalizado,
					passwordEncoder.encode(senha),
					Role.ADMINISTRADOR,
					true
			);
			usuarioRepository.save(administrador);
		};
	}

	private void validarConfiguracao(
			String nome,
			String sobrenome,
			String email,
			String nomeUsuario,
			String senha
	) {
		if (nome.isBlank() || sobrenome.isBlank() || email.isBlank() || nomeUsuario.isBlank()) {
			throw new IllegalStateException("Preencha todos os dados do administrador inicial.");
		}
		if (senha.length() < 8 || senha.length() > 72) {
			throw new IllegalStateException("A senha do administrador inicial deve ter entre 8 e 72 caracteres.");
		}
	}
}
