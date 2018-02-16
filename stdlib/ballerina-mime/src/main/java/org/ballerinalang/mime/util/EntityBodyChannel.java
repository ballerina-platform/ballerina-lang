package org.ballerinalang.mime.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class EntityBodyChannel implements ByteChannel {
    private InputStream in;
    private ReadableByteChannel inChannel;

    public EntityBodyChannel(InputStream in) {
        this.in = in;
        this.inChannel = Channels.newChannel(in);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return inChannel.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return 0;
    }

    @Override
    public boolean isOpen() {
        return inChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        inChannel.close();
        in.close();
    }
}
