package org.ballerinalang.runtime.message;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@code BlobValue} represents a blob value in Ballerina.
 *
 * @since 0.90
 */
public class BinaryMessageDataSource extends BallerinaMessageDataSource {
    private byte[] value;
    private OutputStream outputStream;

    /**
     * Create a String datasource with a string.
     *
     * @param value String value
     */
    public BinaryMessageDataSource(byte[] value) {
        this.value = value;
        this.outputStream = null;
    }

    /**
     * Create a String datasource with a string and a target output stream.
     *
     * @param value         String value
     * @param outputStream  Target outputstream
     */
    public BinaryMessageDataSource(byte[] value, OutputStream outputStream) {
        this.value = value;
        this.outputStream = outputStream;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public void serializeData() {
        try {
            this.outputStream.write(this.value);
        } catch (IOException e) {
            throw new BallerinaException("Error occurred during writing the string message to the output stream", e);
        }
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public String getMessageAsString() {
        return new String(this.value);
    }

    @Override
    public BallerinaMessageDataSource clone() {
        byte[] clonedContent = this.value;
        return new BinaryMessageDataSource(clonedContent);
    }
}
