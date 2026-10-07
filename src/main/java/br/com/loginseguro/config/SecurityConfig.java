package br.com.loginseguro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import br.com.loginseguro.model.Role;

/**
 * Define as regras de autenticacao e autorizacao da aplicacao.
 */
@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(autorizacao -> autorizacao
						.requestMatchers(
								"/css/**",
								"/js/**",
								"/images/**",
								"/favicon.ico",
								"/error"
						).permitAll()
						.requestMatchers("/admin/**").hasRole(Role.ADMINISTRADOR.name())
						.requestMatchers("/operador/**").hasRole(Role.OPERADOR.name())
						.requestMatchers("/professor/**").hasRole(Role.PROFESSOR.name())
						.anyRequest().authenticated()
				)
				.formLogin(login -> login
						.loginPage("/login")
						.defaultSuccessUrl("/", true)
						.permitAll()
				)
				.logout(logout -> logout
						.logoutSuccessUrl("/login?logout")
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("SESSION")
				);

		return http.build();
	}
}
