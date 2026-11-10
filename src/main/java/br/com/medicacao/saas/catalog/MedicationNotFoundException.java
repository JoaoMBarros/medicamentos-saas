package br.com.medicacao.saas.catalog;

public class MedicationNotFoundException extends RuntimeException {
    public MedicationNotFoundException(String message) {
        super(message);
    }
}
