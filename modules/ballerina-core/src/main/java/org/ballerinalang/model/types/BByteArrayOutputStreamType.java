package org.ballerinalang.model.types;

import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.values.BByteArrayOutputStream;
import org.ballerinalang.model.values.BValue;

/**
 * Ballerina type for holding a {@link org.ballerinalang.model.values.BByteArrayOutputStream} value.
 */
public class BByteArrayOutputStreamType extends BType {
    protected BByteArrayOutputStreamType(SymbolScope symbolScope) {
        super(symbolScope);
    }

    protected BByteArrayOutputStreamType(String typeName, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope, BByteArrayOutputStream.class);
    }

    @Override
    public <V extends BValue> V getDefaultValue() {
        return null;
    }
}
