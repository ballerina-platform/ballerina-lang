/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;

/**
 * Extern function to create data channel.
 *
 * @since 0.982.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "WritableDataChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "channel", type = TypeKind.OBJECT, structType = "WritableByteChannel",
                structPackage = "ballerina/io"),
                @Argument(name = "order", type = TypeKind.STRING)
        },
        isPublic = true
)
public class CreateWritableDataChannel extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(CreateWritableDataChannel.class);
    /**
     * Represents the index of the data channel.
     */
    private static final int DATA_CHANNEL_INDEX = 0;
    /**
     * Represents the index of the byte channel.
     */
    private static final int BYTE_CHANNEL_INDEX = 1;
    /**
     * Represents the byte order.
     */
    private static final int BYTE_ORDER_INDEX = 2;

    /**
     * Returns the relevant byte order.
     *
     * @param byteOrder byte order defined through ballerina api.
     * @return byte order mapped with java equivalent.
     */
    private ByteOrder getByteOrder(String byteOrder) {
        switch (byteOrder) {
            case "BE":
                return ByteOrder.BIG_ENDIAN;
            case "LE":
                return ByteOrder.LITTLE_ENDIAN;
            default:
                return ByteOrder.nativeOrder();
        }
    }

    @Override
    public void execute(Context context) {
        try {
            BMap<String, BValue> dataChannelStruct = (BMap<String, BValue>) context.getRefArgument(DATA_CHANNEL_INDEX);
            BMap<String, BValue> byteChannelStruct = (BMap<String, BValue>) context.getRefArgument(BYTE_CHANNEL_INDEX);
            ByteOrder byteOrder = getByteOrder(context.getRefArgument(BYTE_ORDER_INDEX).stringValue());
            Channel channel = (Channel) byteChannelStruct.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            DataChannel dataChannel = new DataChannel(channel, byteOrder);
            dataChannelStruct.addNativeData(IOConstants.DATA_CHANNEL_NAME, dataChannel);
        } catch (Exception e) {
            String message = "Error while creating data channel:" + e.getMessage();
            log.error(message, e);
            throw new BallerinaIOException(message, e);
        }
    }
}
