package br.com.medicacao.saas.identity;

public class TenantAuthenticationException extends RuntimeException {

    public TenantAuthenticationException(String message) {
        super(message);
    }
}
