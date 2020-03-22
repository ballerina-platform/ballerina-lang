package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;

/**
 * Null Object for ObserverbilitySymbolCollector interface.
 */
public class NullObservabiltySymbolCollector implements ObservabilitySymbolCollector {
    @Override
    public void init(CompilerContext context) {
        // Do nothing
    }

    @Override
    public void process(BLangPackage pkgNode) {
        // Do nothing
    }

    @Override
    public void writeCollectedSymbols(BLangPackage module, Path destination) {
        // Do nothing
    }
}
