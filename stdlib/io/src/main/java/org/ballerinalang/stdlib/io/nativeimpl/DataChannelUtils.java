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

import org.ballerinalang.jvm.values.ObjectValue;
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

    public static void initReadableDataChannel(ObjectValue dataChannelObj, ObjectValue byteChannelObj, Object order) {
        try {
            ByteOrder byteOrder = getByteOrder((String) order);
            Channel channel = (Channel) byteChannelObj.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            DataChannel dataChannel = new DataChannel(channel, byteOrder);
            dataChannelObj.addNativeData(DATA_CHANNEL_NAME, dataChannel);
        } catch (Exception e) {
            String message = "error while creating data channel: " + e.getMessage();
            log.error(message, e);
            throw IOUtils.createError(message);
        }
    }

    public static Object readInt16(ObjectValue dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.BIT_16).getValue();
        } catch (IOException e) {
            log.error("error occurred while reading Int16", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readInt32(ObjectValue dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.BIT_32).getValue();
        } catch (IOException e) {
            log.error("error occurred while reading Int32", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readInt64(ObjectValue dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.BIT_64).getValue();
        } catch (IOException e) {
            log.error("error occurred while reading Int64", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readFloat32(ObjectValue dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readDouble(Representation.BIT_32);
        } catch (IOException e) {
            log.error("error occurred while reading Float32", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readFloat64(ObjectValue dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readDouble(Representation.BIT_64);
        } catch (IOException e) {
            log.error("error occurred while reading Float64", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readBool(ObjectValue dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readBoolean();
        } catch (IOException e) {
            log.error("error while reading boolean", e);
            return IOUtils.createError(e);
        }
    }

    public static Object readString(ObjectValue dataChannelObj, long nBytes, String encoding) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        if (channel.hasReachedEnd()) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Channel %d reached it's end", channel.hashCode()));
            }
            return IOUtils.createEoFError();
        } else {
            try {
                return channel.readString((int) nBytes, encoding);
            } catch (IOException e) {
                String msg = "Error occurred while reading string: " + e.getMessage();
                log.error(msg, e);
                return IOUtils.createError(msg);
            }
        }
    }

    public static Object readVarInt(ObjectValue dataChannelObj) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            return channel.readLong(Representation.VARIABLE).getValue();
        } catch (IOException e) {
            log.error("Error occurred while reading VarInt", e);
            return IOUtils.createError(e);
        }
    }

    public static Object closeDataChannel(ObjectValue dataChannel) {
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

    public static void initWritableDataChannel(ObjectValue dataChannelObj, ObjectValue byteChannelObj, Object order) {
        try {
            ByteOrder byteOrder = getByteOrder((String) order);
            Channel channel = (Channel) byteChannelObj.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            DataChannel dataChannel = new DataChannel(channel, byteOrder);
            dataChannelObj.addNativeData(DATA_CHANNEL_NAME, dataChannel);
        } catch (Exception e) {
            String message = "error while creating data channel: " + e.getMessage();
            log.error(message, e);
            throw IOUtils.createError(message);
        }
    }

    public static Object writeInt16(ObjectValue dataChannelObj, long value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.BIT_16);
        } catch (IOException e) {
            log.error("Error occurred while writing int16.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeInt32(ObjectValue dataChannelObj, long value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.BIT_32);
        } catch (IOException e) {
            log.error("Error occurred while writing int32.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeInt64(ObjectValue dataChannelObj, long value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeLong(value, Representation.BIT_64);
        } catch (IOException e) {
            log.error("Error occurred while writing int64.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeFloat32(ObjectValue dataChannelObj, double value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeDouble(value, Representation.BIT_32);
        } catch (IOException e) {
            log.error("Error occurred while writing float32.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeFloat64(ObjectValue dataChannelObj, double value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeDouble(value, Representation.BIT_64);
        } catch (IOException e) {
            log.error("Error occurred while writing float64.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeBool(ObjectValue dataChannelObj, boolean value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(IOConstants.DATA_CHANNEL_NAME);
        try {
            channel.writeBoolean(value);
        } catch (IOException e) {
            log.error("Error occurred while writing boolean.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeString(ObjectValue dataChannelObj, String value, String encoding) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(DATA_CHANNEL_NAME);
        try {
            channel.writeString(value, encoding);
        } catch (IOException e) {
            log.error("Error occurred while writing string.", e);
            return IOUtils.createError(e);
        }
        return null;
    }

    public static Object writeVarInt(ObjectValue dataChannelObj, long value) {
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
