package org.ballerinalang.model.values;

import java.io.ByteArrayOutputStream;

/**
 * This model represents a byte array of an output stream.
 */
public class BByteArrayOutputStream extends ByteArrayOutputStream  implements BRefType {
    @Override
    public String stringValue() {
        return null;
    }

    @Override
    public Object value() {
        return null;
    }
}
