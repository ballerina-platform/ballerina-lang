package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

/**
 * The {@code BInputStream} represents a buffer of an input stream.
 * {@link BOutputStream} will be useful for reading input streams.
 *
 * @since 0.9.0
 */
public class BOutputStream implements BRefType {

    private BufferedOutputStream value;

    public BOutputStream(OutputStream out) {
        value = new BufferedOutputStream(out);
    }

    public BOutputStream(OutputStream out, int size) {
        value = new BufferedOutputStream(out, size);
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return BTypes.typeOutputStream;
    }

    @Override public BValue copy() {
        return new BOutputStream(value);
    }

    @Override
    public BufferedOutputStream value() {
        return value;
    }
}
