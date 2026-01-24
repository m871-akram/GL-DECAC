package fr.ensimag.deca.codegen;

import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;


/**
 * gestion des registres et  la pile pour C pour
 * <p>
 * l alocation de registres  R2 --> R15 et
 * le calcul du TSTO
 */

/**
 * Gestionnaire du Banc de Registres Physiques (R2 à R15).
 * * Rôle : Allouer et libérer les registres de travail.
 * Note : La gestion du débordement (Spill) et de la pile est déléguée
 * à la MemoryManagementUnit (MMU).
 */
public class RegisterManager {

    // Registres
    private int registreCourant = 2;  // R0 et R1 sont scratch
    private int registreMax; // 15 ,  X-1 si option -r X


    /**
     * @param numRegisters Le nombre de registres disponibles (valeur de l'option -r, défaut 16).
     */

    public RegisterManager(int numRegisters) {

        // on a besoin de  au moins R0, R1 et R2
        if (numRegisters < 4 || numRegisters > 16) {
            throw new DecacInternalError(" Nombre de registres invalide (entre 4 et 16)");
        }

        this.registreMax = numRegisters - 1;
    }

    // REGISTRES

    /**
     * @return true s'il reste un registre libre
     */
    public boolean registreLibre() {
        return registreCourant <= registreMax;
    }

    /**
     * Alloue un registre temporaire
     */
    public GPRegister prendreRegistre() {

        if (!registreLibre()) {
            throw new DecacInternalError(
                    "Plus de registres disponibles"
            );
        }

        GPRegister reg = Register.getR(registreCourant);
        registreCourant++;
        return reg;

        // return Register.getR(currentRegisterIndex++);
    }


    /**
     * Libère le dernier registre utilisé
     */
    public void libererRegistre() {
        if (registreCourant > 2) {
            registreCourant--;
        } else {
//            throw new DecacInternalError("Bug compilateur : tentative de libération excessive de registres");
        }
    }


    /**
     * Réinitialise les compteurs pour un.  nouvelle méthode
     */
    public void reset() {


        registreCourant = 2;
    }


}


