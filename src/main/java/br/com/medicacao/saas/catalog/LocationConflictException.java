package br.com.medicacao.saas.catalog;

public class LocationConflictException extends RuntimeException {
    public LocationConflictException(String message) {
        super(message);
    }
}
