package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The {@code BInputStream} represents a buffer of an input stream.
 * {@link BOutputStream} will be useful for reading input streams.
 *
 * @since 0.9.0
 */
public class BOutputStream extends BufferedOutputStream implements BRefType {

    public BOutputStream(OutputStream out) {
        super(out);
    }

    public BOutputStream(OutputStream out, int size) {
        super(out, size);
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public BType getType() {
        return BTypes.typeOutputStream;
    }

    @Override
    public Object value() {
        return null;
    }
}
