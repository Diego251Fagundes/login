package br.com.loginseguro.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mongodb.spring.session.MongoIndexedSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

/**
 * Verificacao opcional no banco configurado: consulta usuarios sem alterar contas
 * e cria somente uma sessao temporaria, removida ao concluir o teste.
 */
@EnabledIfEnvironmentVariable(named = "RUN_ATLAS_TESTS", matches = "true")
@SpringBootTest(properties = {
		"app.admin-inicial.enabled=false",
		"spring.data.mongodb.auto-index-creation=false",
		"logging.level.org.mongodb.driver=WARN"
})
class MongoAtlasIntegrationTests {

	@Value("${spring.mongodb.uri}")
	private String uri;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private MongoIndexedSessionRepository sessionRepository;

	@Test
	void deveConsultarUsuariosEPersistirRecuperarEExcluirSessao() {
		// A URI real permanece no ambiente; o teste nao imprime credenciais nem dados pessoais.
		assertThat(uri.contains(".mongodb.net")).as("A integracao deve usar um cluster Atlas").isTrue();
		// O servidor pode representar "ok" como inteiro ou decimal no BSON.
		assertThat(mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1))
				.get("ok", Number.class).doubleValue())
				.isEqualTo(1.0);
		assertThat(mongoTemplate.getCollection("usuarios").countDocuments()).isGreaterThanOrEqualTo(0);

		var session = sessionRepository.createSession();
		String id = session.getId();
		try {
			session.setAttribute("verificacao", "login-seguro");
			var contexto = SecurityContextHolder.createEmptyContext();
			var principal = User.withUsername("teste-sessao").password("hash-de-teste").roles("OPERADOR").build();
			contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
					principal, null, principal.getAuthorities()));
			session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, contexto);
			sessionRepository.save(session);
			var recuperada = sessionRepository.findById(id);
			assertThat(recuperada).isNotNull();
			assertThat((String) recuperada.getAttribute("verificacao")).isEqualTo("login-seguro");
			org.springframework.security.core.context.SecurityContext contextoRecuperado =
					recuperada.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
			assertThat(contextoRecuperado.getAuthentication().getName()).isEqualTo("teste-sessao");
			assertThat(contextoRecuperado.getAuthentication().isAuthenticated()).isTrue();
			assertThat(recuperada.getMaxInactiveInterval()).isEqualTo(java.time.Duration.ofMinutes(30));
		} finally {
			sessionRepository.deleteById(id);
		}
		assertThat(sessionRepository.findById(id)).isNull();
	}
}
