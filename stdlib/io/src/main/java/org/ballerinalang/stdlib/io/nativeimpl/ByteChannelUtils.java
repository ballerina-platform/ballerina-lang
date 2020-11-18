/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import org.ballerinalang.stdlib.io.channels.AbstractNativeChannel;
import org.ballerinalang.stdlib.io.channels.BlobChannel;
import org.ballerinalang.stdlib.io.channels.BlobIOChannel;
import org.ballerinalang.stdlib.io.channels.FileIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.stdlib.io.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.ballerinalang.stdlib.io.utils.IOConstants.BYTE_CHANNEL_NAME;

/**
 * This class hold Java inter-ops bridging functions for io# *ByteChannels.
 *
 * @since 1.1.0
 */
public class ByteChannelUtils extends AbstractNativeChannel {

    private static final Logger log = LoggerFactory.getLogger(ByteChannelUtils.class);
    private static final String READ_ACCESS_MODE = "r";
    private static final String WRITE_ACCESS_MODE = "w";
    private static final String APPEND_ACCESS_MODE = "a";

    private ByteChannelUtils() {
    }

    public static Object read(BObject channel, long nBytes) {
        int arraySize = nBytes <= 0 ? IOConstants.CHANNEL_BUFFER_SIZE : (int) nBytes;
        Channel byteChannel = (Channel) channel.getNativeData(BYTE_CHANNEL_NAME);
        ByteBuffer content = ByteBuffer.wrap(new byte[arraySize]);
        if (byteChannel.hasReachedEnd()) {
            return IOUtils.createEoFError();
        } else {
            try {
                byteChannel.read(content);
                return ValueCreator.createArrayValue(getContentData(content));
            } catch (Exception e) {
                String msg = "error occurred while reading bytes from the channel. " + e.getMessage();
                log.error(msg, e);
                return IOUtils.createError(msg);
            }
        }
    }

    private static byte[] getContentData(final ByteBuffer contentBuffer) {
        int bufferSize = contentBuffer.limit();
        int readPosition = contentBuffer.position();
        byte[] content = contentBuffer.array();
        final int startPosition = 0;
        if (readPosition == bufferSize) {
            return content;
        }
        return Arrays.copyOfRange(content, startPosition, readPosition);
    }

    public static Object base64Encode(BObject channel) {
        return Utils.encodeByteChannel(channel, false);
    }

    public static Object base64Decode(BObject channel) {
        return Utils.decodeByteChannel(channel, false);
    }

    public static Object closeByteChannel(BObject channel) {
        Channel byteChannel = (Channel) channel.getNativeData(BYTE_CHANNEL_NAME);
        try {
            byteChannel.close();
        } catch (ClosedChannelException e) {
            return IOUtils.createError("channel already closed.");
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object write(BObject channel, BArray content, long offset) {
        Channel byteChannel = (Channel) channel.getNativeData(BYTE_CHANNEL_NAME);
        ByteBuffer writeBuffer = ByteBuffer.wrap(content.getBytes());
        writeBuffer.position((int) offset);
        try {
            return byteChannel.write(writeBuffer);
        } catch (IOException e) {
            log.error("Error occurred while writing to the channel.", e);
            return IOUtils.createError(e);
        }
    }

    public static Object openReadableFile(BString pathUrl) {
        Object channel;
        try {
            channel = createChannel(inFlow(pathUrl.getValue()));
        } catch (BallerinaIOException e) {
            channel = IOUtils.createError(e);
        } catch (BError e) {
            return e;
        }
        return channel;
    }

    public static Object openWritableFile(BString pathUrl, boolean accessMode) {
        try {
            return createChannel(inFlow(pathUrl.getValue(), accessMode));
        } catch (BallerinaIOException | BallerinaException e) {
            return IOUtils.createError(e);
        } catch (BError e) {
            return e;
        }
    }

    public static Object createReadableChannel(BArray content) {
        try {
            Channel channel = inFlow(content);
            return createChannel(channel);
        } catch (Exception e) {
            return IOUtils.createError(e);
        }
    }

    private static Channel inFlow(String pathUrl) throws BallerinaIOException {
        Path path = Paths.get(pathUrl);
        FileChannel fileChannel = IOUtils.openFileChannelExtended(path, READ_ACCESS_MODE);
        Channel channel = new FileIOChannel(fileChannel);
        channel.setReadable(true);
        return channel;
    }

    private static Channel inFlow(String pathUrl, boolean accessMode) throws BallerinaIOException {
        Path path = Paths.get(pathUrl);
        FileChannel fileChannel;
        if (accessMode) {
            fileChannel = IOUtils.openFileChannelExtended(path, APPEND_ACCESS_MODE);
        } else {
            fileChannel = IOUtils.openFileChannelExtended(path, WRITE_ACCESS_MODE);
        }
        return new FileIOChannel(fileChannel);
    }

    private static Channel inFlow(BArray contentArr) {
        byte[] content = shrink(contentArr);
        ByteArrayInputStream contentStream = new ByteArrayInputStream(content);
        ReadableByteChannel readableByteChannel = Channels.newChannel(contentStream);
        return new BlobIOChannel(new BlobChannel(readableByteChannel));
    }

    private static byte[] shrink(BArray array) {
        int contentLength = array.size();
        byte[] content = new byte[contentLength];
        System.arraycopy(array.getBytes(), 0, content, 0, contentLength);
        return content;
    }
}
