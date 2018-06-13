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

package org.ballerinalang.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.channels.base.DataChannel;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;

/**
 * Native function to create data channel.
 *
 * @since 0.973.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "DataChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "channel", type = TypeKind.OBJECT, structType = "ByteChannel",
                structPackage = "ballerina/io"),
                @Argument(name = "order", type = TypeKind.STRING)
        },
        isPublic = true
)
public class CreateDataChannel extends BlockingNativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(CreateDataChannel.class);
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
            case "BI":
                return ByteOrder.BIG_ENDIAN;
            case "LI":
                return ByteOrder.LITTLE_ENDIAN;
            default:
                return ByteOrder.nativeOrder();
        }
    }

    @Override
    public void execute(Context context) {
        try {
            BStruct dataChannelStruct = (BStruct) context.getRefArgument(DATA_CHANNEL_INDEX);
            BStruct byteChannelStruct = (BStruct) context.getRefArgument(BYTE_CHANNEL_INDEX);
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
