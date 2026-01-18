package fr.ensimag.deca;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

/**
 * Main class for the command-line Deca compiler.
 *
 * @author gl51
 * @date 01/01/2026
 */
public class DecacMain {
    private static Logger LOG = Logger.getLogger(DecacMain.class);
    
    public static void main(String[] args) {
        // example log4j message.
        LOG.info("Decac compiler started");
        boolean error = false;
        final CompilerOptions options = new CompilerOptions();
        try {
            options.parseArgs(args);
        } catch (CLIException e) {
            System.err.println("Error during option parsing:\n"
                    + e.getMessage());
            options.displayUsage();
            System.exit(1);
        }
        if (options.getPrintBanner()) {
            System.out.println("equipe 51: featuring Thibault marchand");
//            throw new UnsupportedOperationException("decac -b not yet implemented");
            System.exit(0);
        }
        if (options.getSourceFiles().isEmpty()) {
            options.displayUsage();
            System.exit(0);
        }
        if (options.getParallel()) {
            int nbThreads = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(nbThreads);

            List<Future<Boolean>> results = new ArrayList<>();

            for (File source : options.getSourceFiles()) {
                Callable<Boolean> task = () -> {
                    DecacCompiler compiler = new DecacCompiler(options, source);
                    return compiler.compile(); // true = erreur
                };
                results.add(executor.submit(task));
            }

            executor.shutdown();

            for (Future<Boolean> result : results) {
                try {
                    if (result.get()) {
                        error = true;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    error = true;
                    e.printStackTrace();
                }
    }
        } else {
            for (File source : options.getSourceFiles()) {
                DecacCompiler compiler = new DecacCompiler(options, source);
                if (compiler.compile()) {
                    error = true;
                }
            }
        }
        System.exit(error ? 1 : 0);
    }
}


