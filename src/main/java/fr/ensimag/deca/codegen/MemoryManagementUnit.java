package fr.ensimag.deca.codegen;


import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import java.util.Stack;

/**
 * Memory Management Unit (MMU)
 * Centralise toute la gestion de la mémoire : Statique (GB), Pile (LB) et Tas.
 * Remplace les gestionnaires classiques (StackManager, AddressManager).
 */
public class MemoryManagementUnit {

    // --- SECTION GLOBALE (Ex-MemoryBus) ---
    private int globalOffset = 1; // 1(GB) est la première dispo (0=null)

    /**
     * Alloue un espace dans la mémoire globale (GB)
     */
    public RegisterOffset allocGlobal(int sizeWords) {
        RegisterOffset addr = new RegisterOffset(globalOffset, Register.GB);
        globalOffset += sizeWords;
        return addr;
    }

    public int getGlobalUsage() {
        return globalOffset;
    }


    // --- SECTION PILE / BLOC (Ex-BlockStructure) ---
    private int localOffset = 1;      // 1(LB) pour la première var locale
    private int currentStackUsage = 0;// Empilement courant (PUSH)
    private int maxStackUsage = 0;    // TSTO (High Water Mark)

    // Snapshot pour sauvegarder l'état des registres temporaires si besoin
    private final Stack<Integer> contextStack = new Stack<>();

    /**
     * Entre dans un nouveau bloc de méthode (Reset LB)
     */
    public void enterNewMethodFrame() {
        this.localOffset = 1;
        this.currentStackUsage = 0;
        this.maxStackUsage = 0;
        // On ne reset PAS globalOffset (les globales restent)
    }

    /**
     * Alloue une variable locale dans la Frame courante (LB)
     */
    public RegisterOffset allocLocal() {
        RegisterOffset addr = new RegisterOffset(localOffset, Register.LB);
        localOffset++;
        return addr;
    }

    /**
     * Simule un empilement physique (PUSH) et met à jour le TSTO
     */
    public void notifyPush(int count) {
        currentStackUsage += count;
        updateMaxFuse();
    }

    /**
     * Simule un dépilement physique (POP)
     */
    public void notifyPop(int count) {
        currentStackUsage -= count;
        if (currentStackUsage < 0) {
            // Sécurité pour le débogage du compilateur
            // throw new DecacInternalError("Stack Underflow simulation");
            currentStackUsage = 0;
        }
    }

    /**
     * Appelé pour réserver de la place pour les variables locales (ADDSP)
     * et l'inclure dans le calcul TSTO global
     */
    public void notifyLocalBlockAllocation(int numberOfLocals) {
        // Le TSTO doit couvrir : Locals + Max(Temporaires/Push)
        // Ici on ajoute juste les locales à la base de calcul
        currentStackUsage += numberOfLocals;
        updateMaxFuse();
        // Attention : ADDSP augmente SP, mais ne change pas LB,
        // donc on n'augmente pas localOffset ici si allocLocal le fait déjà.
    }

    private void updateMaxFuse() {
        if (currentStackUsage > maxStackUsage) {
            maxStackUsage = currentStackUsage;
        }
    }

    /**
     * Retourne la valeur à utiliser pour l'instruction TSTO
     */
    public int getStackRequirements() {
        return maxStackUsage; // + éventuellement registres sauvegardés
    }
}