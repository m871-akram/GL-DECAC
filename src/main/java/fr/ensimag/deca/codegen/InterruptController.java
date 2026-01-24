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

    private final Set<InterruptVector> armedInterrupts = new HashSet<>();

    /**
     * Déclenche une interruption (génère le branchement vers l'ISR)
     */
    public void triggerInterrupt(DecacCompiler compiler, InterruptVector vector) {
        // Marque l'interruption comme nécessaire
        armedInterrupts.add(vector);
        // Génère le saut vers la routine
        compiler.addInstruction(new BOV(new Label(vector.getLabelName())));
    }

    /**
     * Arme une interruption sans générer de branchement.
     * Utile quand on veut générer un BRA au lieu de BOV.
     */
    public void armInterrupt(InterruptVector vector) {
        armedInterrupts.add(vector);
    }

    /**
     * Génère le code des routines de service (ISR) activées.
     * À appeler à la fin du Main.
     */
    public void flashServiceRoutines(DecacCompiler compiler) {
        if (armedInterrupts.isEmpty()) return;

        compiler.addComment("--- Interrupt Service Routines (ISR) ---");

        for (InterruptVector vec : armedInterrupts) {
            compiler.addLabel(new Label(vec.getLabelName()));
            compiler.addInstruction(new WSTR(vec.getMessage()));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }
    }
}