package fr.ensimag.deca.codegen;


import fr.ensimag.ima.pseudocode.Label;

import java.util.Stack;

/**
 * Gère la synchronisation des signaux de contrôle (Labels).
 * Maintient le contexte hiérarchique des boucles et conditions.
 */
public class SignalSequencer {

    private final Stack<LoopContext> loopStack = new Stack<>();
    private long clockCycle = 0; // Compteur unique

    /**
     * Génère un signal unique (ex: pour un IF)
     */
    public Label genSignal(String portName) {
        return new Label(portName + "_" + (clockCycle++));
    }

    /**
     * Démarre une séquence de boucle.
     * Les nœuds enfants pourront appeler getCurrentLoopExit() sans savoir où ils sont.
     */
    public void enterLoopSequence() {
        Label start = genSignal("loop_start");
        Label exit = genSignal("loop_exit");
        loopStack.push(new LoopContext(start, exit));
    }

    public Label getCurrentLoopStart() {
        return loopStack.peek().start;
    }

    public Label getCurrentLoopExit() {
        return loopStack.peek().exit;
    }

    public void exitLoopSequence() {
        loopStack.pop();
    }

    // Structure interne pour se souvenir où on est
    private static class LoopContext {
        final Label start;
        final Label exit;

        LoopContext(Label s, Label e) {
            start = s;
            exit = e;
        }
    }
}