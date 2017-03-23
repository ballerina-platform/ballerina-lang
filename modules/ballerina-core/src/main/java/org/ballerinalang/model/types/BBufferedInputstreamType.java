package org.ballerinalang.model.types;

import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.values.BBufferedInputstream;
import org.ballerinalang.model.values.BValue;

/**
 * The Ballerina type which holds a {@link BBufferedInputstream}.
 */
public class BBufferedInputstreamType extends BType {
    protected BBufferedInputstreamType(SymbolScope symbolScope) {
        super(symbolScope);
    }

    protected BBufferedInputstreamType(String typeName, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope, BBufferedInputstream.class);
    }

    @Override
    public <V extends BValue> V getDefaultValue() {
        return null;
    }
}
