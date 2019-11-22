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

import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.stdlib.io.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;

import static org.ballerinalang.stdlib.io.utils.IOConstants.BYTE_CHANNEL_NAME;

/**
 * This class hold Java inter-ops bridging functions for io# *ByteChannels.
 *
 * @since 1.1.0
 */
public class ByteChannelUtils {

    private static final Logger log = LoggerFactory.getLogger(ByteChannelUtils.class);

    private ByteChannelUtils() {
    }

    public static Object read(ObjectValue channel, long nBytes) {
        int arraySize = nBytes <= 0 ? IOConstants.CHANNEL_BUFFER_SIZE : (int) nBytes;
        Channel byteChannel = (Channel) channel.getNativeData(BYTE_CHANNEL_NAME);
        ByteBuffer content = ByteBuffer.wrap(new byte[arraySize]);
        if (byteChannel.hasReachedEnd()) {
            return IOUtils.createEoFError();
        } else {
            try {
                byteChannel.read(content);
                return BValueCreator.createArrayValue(getContentData(content));
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

    public static Object base64Encode(ObjectValue channel) {
        return Utils.encodeByteChannel(channel, false);
    }

    public static Object base64Decode(ObjectValue channel) {
        return Utils.decodeByteChannel(channel, false);
    }

    public static Object closeByteChannel(ObjectValue channel) {
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

    public static Object write(ObjectValue channel, ArrayValue content, long offset) {
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
}
