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

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.channels.base.Representation;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;

import static org.ballerinalang.stdlib.io.utils.IOConstants.DATA_CHANNEL_NAME;

/**
 * This class hold Java inter-ops bridging functions for io# *DataChannels.
 *
 * @since 1.1.0
 */
public class DataChannelUtils {

    private static final Logger log = LoggerFactory.getLogger(DataChannelUtils.class);

    private DataChannelUtils() {
    }

    /**
     * Returns the relevant byte order.
     *
     * @param byteOrder byte order defined through ballerina api.
     * @return byte order mapped with java equivalent.
     */
    private static ByteOrder getByteOrder(String byteOrder) {
        switch (byteOrder) {
            case "BE":
                return ByteOrder.BIG_ENDIAN;
            case "LE":
                return ByteOrder.LITTLE_ENDIAN;
            default:
                return ByteOrder.nativeOrder();
        }
    }

    public static void initReadableDataChannel(BObject dataChannelObj, BObject byteChannelObj, BString order) {
        try {
            ByteOrder byteOrder = getByteOrder(order.getValue());
            Channel channel = (Channel) byteChannelObj.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            DataChannel dataChannel = new DataChannel(channel, byteOrder);
            dataChannelObj.addNativeData(DATA_CHANNEL_NAME, dataChannel);
        } catch (Exception e) {
            String message = "error while creating data channel: " + e.getMessage();
            log.error(message, e);
            throw IOUtils.createError(message);
        }
    }

    public static Object readInt16(BObject dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.BIT_16).getValue();
        } catch (IOException e) {
            log.error("error occurred while reading Int16", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readInt32(BObject dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.BIT_32).getValue();
        } catch (IOException e) {
            log.error("error occurred while reading Int32", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readInt64(BObject dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.BIT_64).getValue();
        } catch (IOException e) {
            log.error("error occurred while reading Int64", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readFloat32(BObject dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readDouble(Representation.BIT_32);
        } catch (IOException e) {
            log.error("error occurred while reading Float32", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readFloat64(BObject dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readDouble(Representation.BIT_64);
        } catch (IOException e) {
            log.error("error occurred while reading Float64", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readBool(BObject dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readBoolean();
        } catch (IOException e) {
            log.error("error while reading boolean", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readString(BObject dataChannelObj, long nBytes, BString encoding) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        if (channel.hasReachedEnd()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Channel %d reached it's end", channel.hashCode()));
            }
            return IOUtils.createEoFError();
        } else {
            try {
                return StringUtils.fromString(channel.readString((int) nBytes, encoding.getValue()));
            } catch (IOException e) {
                String msg = "Error occurred while reading string: " + e.getMessage();
                log.error(msg, e);
                return IOUtils.createError(msg);
            }
        }
    }

    public static Object readVarInt(BObject dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.VARIABLE).getValue();
        } catch (IOException e) {
            log.error("Error occurred while reading VarInt", e);
            return IOUtils.createError(e);
        }
    }

    public static Object closeDataChannel(BObject dataChannel) {
        DataChannel channel = (DataChannel) dataChannel.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.close();
        } catch (ClosedChannelException e) {
            return IOUtils.createError("channel already closed.");
        } catch (IOException e) {
            return IOUtils.createError(e);
        }
        return null;
    }

    public static void initWritableDataChannel(BObject dataChannelObj, BObject byteChannelObj, BString order) {
        try {
            ByteOrder byteOrder = getByteOrder(order.getValue());
            Channel channel = (Channel) byteChannelObj.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            DataChannel dataChannel = new DataChannel(channel, byteOrder);
            dataChannelObj.addNativeData(DATA_CHANNEL_NAME, dataChannel);
        } catch (Exception e) {
            String message = "error while creating data channel: " + e.getMessage();
            log.error(message, e);
            throw IOUtils.createError(message);
        }
    }

    public static Object writeInt16(BObject dataChannelObj, long value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.BIT_16);
        } catch (IOException e) {
            log.error("Error occurred while writing int16.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeInt32(BObject dataChannelObj, long value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.BIT_32);
        } catch (IOException e) {
            log.error("Error occurred while writing int32.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeInt64(BObject dataChannelObj, long value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.BIT_64);
        } catch (IOException e) {
            log.error("Error occurred while writing int64.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeFloat32(BObject dataChannelObj, double value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeDouble(value, Representation.BIT_32);
        } catch (IOException e) {
            log.error("Error occurred while writing float32.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeFloat64(BObject dataChannelObj, double value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeDouble(value, Representation.BIT_64);
        } catch (IOException e) {
            log.error("Error occurred while writing float64.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeBool(BObject dataChannelObj, boolean value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(IOConstants.DATA_CHANNEL_NAME);
        try {
            channel.writeBoolean(value);
        } catch (IOException e) {
            log.error("Error occurred while writing boolean.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeString(BObject dataChannelObj, BString value, BString encoding) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeString(value.getValue(), encoding.getValue());
        } catch (IOException e) {
            log.error("Error occurred while writing string.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeVarInt(BObject dataChannelObj, long value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.VARIABLE);
        } catch (IOException e) {
            log.error("Error occurred while writing VarInt.", e);
            return IOUtils.createError(e);
        }
        return null;
    }
}
