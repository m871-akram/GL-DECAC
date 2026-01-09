package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl51
 * @date 01/01/2026
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO  = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }
    
    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private List<File> sourceFiles = new ArrayList<File>();

    private int arretEnAvance = -1;
    private boolean noCheck = false;
    private int maxRegisters = -1;

    public int getArretEnAvance() {
        return arretEnAvance;
    }
    public boolean getNoCheck() {
        return noCheck;
    }

    public int getMaxRegisters() {
        return maxRegisters;
    }
    
    public void parseArgs(String[] args) throws CLIException {
        // A FAIRE : parcourir args pour positionner les options correctement.

        if (args.length == 0) {
            displayUsage();
            throw new CLIException("Aucun argument fourni");
        }
    
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
    
            switch (arg) {
    
            case "-b":
                if (args.length != 1) {
                    throw new CLIException("-b ne peut pas être combiné avec d'autres options");
                }
                printBanner = true;
                return;
    
            case "-p":
                arretEnAvance = 0;
                break;
            
            case "-v":
                arretEnAvance = 1;
                break;
            
            case "-n":
                noCheck = true;
                break;
            
            case "-r":
                if (i + 1 >= args.length) {
                    throw new CLIException("Option -r requiert un argument");
                }
                int r = Integer.parseInt(args[++i]);
                if (r < 4 || r > 16) {
                    throw new CLIException("X doit être entre 4 et 16");
                }
                maxRegisters = r;
                break;
    
            default:
                if (!arg.endsWith(".deca")) {
                    throw new CLIException("Fichier source invalide : " + arg);
                }
                File f = new File(arg);
                if (!sourceFiles.contains(f)) {
                    sourceFiles.add(f);
                }
            }
        }
        if (!printBanner && sourceFiles.isEmpty()) {
            throw new CLIException("Aucun fichier source fourni");
        }

        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        switch (getDebug()) {
        case QUIET: break; // keep default
        case INFO:
            logger.setLevel(Level.INFO); break;
        case DEBUG:
            logger.setLevel(Level.DEBUG); break;
        case TRACE:
            logger.setLevel(Level.TRACE); break;
        default:
            logger.setLevel(Level.ALL); break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }

    }

    protected void displayUsage() {
        System.out.println("Usage: decac [[-p | -v] [-n] [-r X] [-d]* [-P] <fichier.deca>...] | [-b]");
        System.out.println("Options:");
        System.out.println("  -b        affiche la bannière dois etre seul");
        System.out.println("  -p        arrêt après l'analyse syntaxique");
        System.out.println("  -v        arrêt après les vérifications");
        System.out.println("  -n        supprime les tests d'exécution");
        System.out.println("  -r X      limite les registres (4 <= X <= 16)");
        System.out.println("  -d        active le mode debug (cumulable)");
        System.out.println("  -P        compilation parallèle");
    }
}
