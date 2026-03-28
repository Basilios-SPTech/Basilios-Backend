package com.basilios.basilios.infra.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Componente de auditoria de segurança (OWASP A09).
 * Registra eventos de autenticação para detecção de atividades suspeitas.
 */
@Component
@Slf4j
public class SecurityAuditLogger {

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        log.info("[SECURITY_AUDIT] action=LOGIN_SUCCESS user={}", username);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String reason = event.getException().getMessage();
        log.warn("[SECURITY_AUDIT] action=LOGIN_FAILED user={} reason={}", username, reason);
    }
}
