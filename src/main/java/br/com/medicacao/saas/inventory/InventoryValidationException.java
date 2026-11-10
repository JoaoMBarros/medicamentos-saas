package br.com.medicacao.saas.inventory;

public class InventoryValidationException extends RuntimeException {
    public InventoryValidationException(String message) {
        super(message);
    }
}
