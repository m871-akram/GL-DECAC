package fr.ensimag.deca.codegen;

/**
 * Liste des erreurs d'exécution possibles (Exceptions Runtime).
 * Contient le label assembleur et le message d'erreur associé.
 */
public enum InterruptVector {
    IRQ_STACK_OVERFLOW("stack_overflow_isr", "Erreur : Pile pleine"),
    IRQ_HEAP_FULL("heap_full_isr", "Erreur : Tas plein"),
    IRQ_DIV_BY_ZERO("div_zero_isr", "Erreur : Division par zero"),
    IRQ_FLOAT_OVERFLOW("overflow_float", "Erreur : float overflow"),
    IRQ_NULL_PTR("null_ptr_isr", "Erreur : Dereferencement null"),
    IRQ_IO_ERROR("io_error_isr", "Erreur : IO"),
    IRQ_CAST_ERROR("cast_error_isr", "Erreur : Cast invalide");

    private final String labelName;
    private final String message;

    InterruptVector(String labelName, String message) {
        this.labelName = labelName;
        this.message = message;
    }

    public String getLabelName() {
        return labelName;
    }

    public String getMessage() {
        return message;
    }
}