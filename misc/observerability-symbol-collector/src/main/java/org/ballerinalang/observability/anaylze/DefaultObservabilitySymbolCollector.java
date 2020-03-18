package org.ballerinalang.observability.anaylze;

import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;

/**
 * Default implementation of ObserverbilitySymbolCollector.
 */
public class DefaultObservabilitySymbolCollector implements ObservabilitySymbolCollector {
    private final PrintStream console = System.out;;
    
    @Override
    public void init(CompilerContext context) {
        console.println("DefaultObservabilitySymbolCollector initialized");
    }

    @Override
    public void process(BLangPackage pkgNode) {
        console.println("DefaultObservabilitySymbolCollector process called");
    }
}
