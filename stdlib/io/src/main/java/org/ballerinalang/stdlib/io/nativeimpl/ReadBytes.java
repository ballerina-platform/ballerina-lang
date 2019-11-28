/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Extern function ballerina.lo#readBytes.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "read",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ReadableByteChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "nBytes", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @ReturnType(type = TypeKind.INT),
                @ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class ReadBytes {

    private static final Logger log = LoggerFactory.getLogger(ReadBytes.class);

    public static Object read(Strand strand, ObjectValue channel, long nBytes) {
        int arraySize = nBytes <= 0 ? IOConstants.CHANNEL_BUFFER_SIZE : (int) nBytes;
        Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        ByteBuffer content = ByteBuffer.wrap(new byte[arraySize]);
        if (byteChannel.hasReachedEnd()) {
            return IOUtils.createEoFError();
        } else {
            try {
                byteChannel.read(content);
                return new ArrayValueImpl(getContentData(content));
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

}
