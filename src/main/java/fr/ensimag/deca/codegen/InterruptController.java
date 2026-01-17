package fr.ensimag.deca.codegen;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;

import java.util.HashSet;
import java.util.Set;

/**
 * Contrôleur d'interruptions logicielles (Exceptions Runtime).
 * Câblage dynamique des routines de service (ISR).
 */
public class InterruptController {

    // Vecteurs d'interruption possibles
    public enum Vector {
        IRQ_STACK_OVERFLOW("stack_overflow_isr", "Erreur : Pile pleine"),
        IRQ_HEAP_FULL("heap_full_isr", "Erreur : Tas plein"),
        IRQ_DIV_BY_ZERO("div_zero_isr", "Erreur : Division par zero"),
        IRQ_NULL_PTR("null_ptr_isr", "Erreur : Dereferencement null"),
        IRQ_IO_ERROR("io_error_isr", "Erreur : IO"),
        IRQ_CAST_ERROR("cast_error_isr", "Erreur : Cast invalide");

        final String labelName;
        final String message;

        Vector(String l, String m) { this.labelName = l; this.message = m; }
    }

    private final Set<Vector> armedInterrupts = new HashSet<>();

    /**
     * Déclenche une interruption (génère le branchement vers l'ISR)
     */
    public void triggerInterrupt(DecacCompiler compiler, Vector vector) {
        // Marque l'interruption comme nécessaire
        armedInterrupts.add(vector);
        // Génère le saut vers la routine
        compiler.addInstruction(new BOV(new Label(vector.labelName)));
    }

    /**
     * Génère le code des routines de service (ISR) activées.
     * À appeler à la fin du Main.
     */
    public void flashServiceRoutines(DecacCompiler compiler) {
        if (armedInterrupts.isEmpty()) return;

        compiler.addComment("--- Interrupt Service Routines (ISR) ---");

        for (Vector vec : armedInterrupts) {
            compiler.addLabel(new Label(vec.labelName));
            compiler.addInstruction(new WSTR(vec.message));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }
    }
}