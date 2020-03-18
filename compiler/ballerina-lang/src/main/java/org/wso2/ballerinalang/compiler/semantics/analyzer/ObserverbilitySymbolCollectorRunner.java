package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Used to analyze ballerina code and store observabilty symbols in the Ballerina program artifact.
 */
public class ObserverbilitySymbolCollectorRunner {
    private static final CompilerContext.Key<ObservabilitySymbolCollector> OBSERVARBILITY_SYMBOL_COLLECTOR_KEY =
            new CompilerContext.Key<>();

    private static final String NAME = "name";

    private static final PrintStream console = System.out;;

    private ObserverbilitySymbolCollectorRunner(CompilerContext context) {
        console.println("Choreo compiler plugin initialized");
    }

    public static synchronized ObservabilitySymbolCollector getInstance(CompilerContext context) {
        ObservabilitySymbolCollector observabilitySymbolCollector = context.get(OBSERVARBILITY_SYMBOL_COLLECTOR_KEY);
        if (observabilitySymbolCollector == null) {
            ServiceLoader<ObservabilitySymbolCollector> observerbilitySymbolCollectors
                    = ServiceLoader.load(ObservabilitySymbolCollector.class);

            Iterator<ObservabilitySymbolCollector> collectorIterator = observerbilitySymbolCollectors.iterator();
            if (collectorIterator.hasNext()) {
                observabilitySymbolCollector = collectorIterator.next();
            } else {
                observabilitySymbolCollector = new NullObservabiltySymbolCollector();
            }
            context.put(OBSERVARBILITY_SYMBOL_COLLECTOR_KEY, observabilitySymbolCollector);
        }

        return observabilitySymbolCollector;
    }

}
