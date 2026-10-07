package br.com.loginseguro.config;

import org.mongodb.spring.session.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Mantém as sessões HTTP no MongoDB quando a integração de sessão está habilitada.
 */
@Configuration
@EnableMongoHttpSession
@ConditionalOnProperty(
		name = "app.session.mongodb.enabled",
		havingValue = "true",
		matchIfMissing = true
)
public class SessionConfig {
}
