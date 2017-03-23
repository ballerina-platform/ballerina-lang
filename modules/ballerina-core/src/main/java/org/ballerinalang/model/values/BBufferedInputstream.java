package org.ballerinalang.model.values;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * Model for a buffer of an input stream.
 * {@link BBufferedInputstream} will be useful for reading input streams.
 */
public class BBufferedInputstream extends BufferedInputStream implements BRefType {

    public BBufferedInputstream(InputStream in) {
        super(in);
    }

    public BBufferedInputstream(InputStream in, int size) {
        super(in, size);
    }

    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public Object value() {
        return null;
    }
}
