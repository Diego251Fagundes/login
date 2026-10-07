package br.com.loginseguro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação de autenticação genérica.
 */
@SpringBootApplication
public class LoginSeguroApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginSeguroApplication.class, args);
	}

}
