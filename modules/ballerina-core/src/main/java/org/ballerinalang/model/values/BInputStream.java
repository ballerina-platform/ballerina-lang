package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * The {@code BInputStream} represents a buffer of an input stream.
 * {@link BInputStream} will be useful for reading input streams.
 *
 * @since 0.9.0
 */
public class BInputStream implements BRefType {

    private BufferedInputStream value;

    public BInputStream(InputStream in) {
        value = new BufferedInputStream(in);
    }

    public BInputStream(InputStream in, int size) {
        value = new BufferedInputStream(in, size);
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return BTypes.typeInputStream;
    }

    @Override public BValue copy() {
        return new BInputStream(value);
    }

    @Override
    public BufferedInputStream value() {
        return value;
    }
}
