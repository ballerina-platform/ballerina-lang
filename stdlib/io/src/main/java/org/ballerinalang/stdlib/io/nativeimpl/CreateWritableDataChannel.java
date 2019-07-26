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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.DataChannel;
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
public class CreateWritableDataChannel {

    private static final Logger log = LoggerFactory.getLogger(CreateWritableDataChannel.class);

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

    public static void init(Strand strand, ObjectValue dataChannelObj, ObjectValue byteChannelObj, Object order) {
        try {
            ByteOrder byteOrder = getByteOrder((String) order);
            Channel channel = (Channel) byteChannelObj.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            DataChannel dataChannel = new DataChannel(channel, byteOrder);
            dataChannelObj.addNativeData(IOConstants.DATA_CHANNEL_NAME, dataChannel);
        } catch (Exception e) {
            String message = "Error while creating data channel:" + e.getMessage();
            log.error(message, e);
            throw new BallerinaException(message, e);
        }
    }
}
