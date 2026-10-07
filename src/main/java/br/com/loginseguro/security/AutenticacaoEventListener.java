package br.com.loginseguro.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import br.com.loginseguro.service.ControleTentativasLoginService;

/**
 * Atualiza o controle de tentativas a partir dos eventos do Spring Security.
 */
@Component
public class AutenticacaoEventListener {

	private final ControleTentativasLoginService controleTentativasLoginService;

	public AutenticacaoEventListener(ControleTentativasLoginService controleTentativasLoginService) {
		this.controleTentativasLoginService = controleTentativasLoginService;
	}

	@EventListener
	public void aoFalhar(AuthenticationFailureBadCredentialsEvent evento) {
		controleTentativasLoginService.registrarFalha(evento.getAuthentication().getName());
	}

	@EventListener
	public void aoAutenticar(AuthenticationSuccessEvent evento) {
		controleTentativasLoginService.registrarSucesso(evento.getAuthentication().getName());
	}
}
