package br.com.medicacao.saas.catalog;

public class MedicationConflictException extends RuntimeException {
    public MedicationConflictException(String message) {
        super(message);
    }
}
