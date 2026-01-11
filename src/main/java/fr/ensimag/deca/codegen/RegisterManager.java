

package fr.ensimag.deca.codegen;
import fr.ensimag.deca.tools.DecacInternalError;

import fr.ensimag.ima.pseudocode.Register;

import fr.ensimag.ima.pseudocode.GPRegister;


/** gestion des registres et  la pile pour C pour
 *
 * l alocation de registres  R2 --> R15 et
 * le calcul du TSTO

 */


public class RegisterManager {

    // Registres
    private int registreCourant = 2;  // R0 et R1 sont scratch
    private  int registreMax; // 15 ,  X-1 si option -r X
    // TSTO
    private int taillePileCourante = 0; // spills
    private int taillePileMax = 0; // taille maximale atteinte dans le bloc courant
    private int nbGlobales = 0;

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
    }


    /**
     * Libère le dernier registre utilisé
     */
    public void libererRegistre() {
        if (registreCourant > 2) {
            registreCourant--;
        }
    }
    /**
     * Pour debug
     */
    public int getRegistreCourant() {
        return registreCourant;
    }
    // PILE ET TSTO


     /**
     * Signale un PUSH ou un spill
     */
    public void empiler() {
        taillePileCourante++;

        if (taillePileCourante > taillePileMax) {
            taillePileMax = taillePileCourante;
        }
    }



    /**
     * Signale un POP
     */
    public void depiler() {
        taillePileCourante--;

        if (taillePileCourante < 0) {
            taillePileCourante = 0;
        }
    }

    /**
     * Ajout de variables locales
     */
    public void ajouterVariablesLocales(int nb) {
        taillePileCourante += nb;

        if (taillePileCourante > taillePileMax) {
            taillePileMax = taillePileCourante;
        }
    }

    public void incrNbGlobales() {
        nbGlobales++;
    }


    public int getNbGlobales() {
        return nbGlobales;
    }


    public int getTaillePileMax() {
        return taillePileMax;
    }

    /**
     * Réinitialise les compteurs pour un.  nouvelle méthode
     */
    public void reset() {

        taillePileCourante = 0;
        taillePileMax = 0;
        registreCourant = 2;
    }

}



