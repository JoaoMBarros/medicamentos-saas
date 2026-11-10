package br.com.medicacao.saas.inventory;

public class InventoryConflictException extends RuntimeException {
    public InventoryConflictException(String message) {
        super(message);
    }
}
