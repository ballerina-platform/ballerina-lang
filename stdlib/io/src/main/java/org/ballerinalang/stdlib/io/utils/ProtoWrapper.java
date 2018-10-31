package org.ballerinalang.stdlib.io.utils;

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.readers.ChannelReader;
import org.ballerinalang.stdlib.io.channels.base.writers.ChannelWriter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * A channel to be used in gRPC.
 */
public class ProtoWrapper extends Channel {

    public ProtoWrapper(ProtoByteChannel channel) throws BallerinaIOException {
        super(channel, new ChannelReader(), new ChannelWriter());
    }

    @Override
    public Channel getChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transfer(int position, int count, WritableByteChannel dstChannel) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSelectable() {
        return false;
    }
}
