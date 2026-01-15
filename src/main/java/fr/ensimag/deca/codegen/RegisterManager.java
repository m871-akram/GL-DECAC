

package fr.ensimag.deca.codegen;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.DecacInternalError;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.GPRegister;


/** gestion des registres et  la pile pour C pour
 *
 * l alocation de registres  R2 --> R15 et
 * le calcul du TSTO

 */


public class RegisterManager {
    private DecacCompiler compiler;
    // Registres
    private int registreCourant = 2;  // R0 et R1 sont scratch
    private int registreMax; // 15 ,  X-1 si option -r X
    private Deque<GPRegister> allocationStack = new ArrayDeque<>();
    private Deque<GPRegister> toRestore = new ArrayDeque<>();
    private Queue<GPRegister> used = new LinkedList<>();
    // TSTO
    private int taillePileCourante = 0; // spills
    private int taillePileMax = 0; // taille maximale atteinte dans le bloc courant
    private int nbGlobales = 0;

    /**
     * @param numRegisters Le nombre de registres disponibles (valeur de l'option -r, défaut 16).
     */

    public RegisterManager(int numRegisters, DecacCompiler compiler) {

        // on a besoin de  au moins R0, R1 et R2
        if (numRegisters < 4 || numRegisters > 16) {
            throw new DecacInternalError(" Nombre de registres invalide (entre 4 et 16)");
        }

        this.registreMax = numRegisters - 1;
        this.compiler = compiler;
    }

    // REGISTRES

    /**
     * @return true s'il reste un registre libre
     */
    public boolean registreLibre() {
        return registreCourant < registreMax;
    }

    /**
     * Alloue un registre temporaire
     */
    public GPRegister prendreRegistre() {
        int registreLibre;
        if (!registreLibre()) {
            GPRegister oldestRegister= findOldRegister();
            compiler.addInstruction(new PUSH(oldestRegister));
            toRestore.add(oldestRegister);
            this.empiler();
            registreLibre = oldestRegister.getNumber();
        }else{
            registreCourant++;
            registreLibre =registreCourant;
        }

        GPRegister reg = Register.getR(registreLibre);
        used.add(reg);
        allocationStack.push(reg);
        return reg;
    }


    private GPRegister findOldRegister() {
        return used.poll();
    }

    /**
     * Libère le dernier registre utilisé
     */
    public void libererRegistre() {
        GPRegister reg = allocationStack.pop();

        if (toRestore.contains(reg)) {
            compiler.addInstruction(new POP(reg));
            compiler.getRegisterManager().depiler();
            toRestore.remove(reg);
        } else {
            registreCourant--;
        }

        used.remove(reg);
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
        allocationStack.clear();
        used.clear();
        toRestore.clear();
    }

}



