package org.ballerinalang.stdlib.io.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * represent the byte channel to be used as protoChannel.
 */
public class ProtoByteChannel implements ByteChannel {

    private InputStream inputStream;
    private ReadableByteChannel byteChannel;

    public ProtoByteChannel(InputStream inputStream) {
        this.inputStream = inputStream;
        this.byteChannel = Channels.newChannel(inputStream);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return byteChannel.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return 0;
    }

    @Override
    public boolean isOpen() {
        return byteChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        byteChannel.close();
        inputStream.close();
    }
}
