/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.io.channels.AbstractNativeChannel;
import org.ballerinalang.stdlib.io.channels.BlobChannel;
import org.ballerinalang.stdlib.io.channels.BlobIOChannel;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Extern function ballerina/io#createMemoryChannel.
 *
 * @since 0.970.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "createReadableChannel",
        args = {@Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.BYTE)},
        isPublic = true
)
public class CreateMemoryChannel extends AbstractNativeChannel {

    public static Object createReadableChannel(Strand strand, ArrayValue content) {
        try {
            Channel channel = inFlow(content);
            return createChannel(channel);
        } catch (BallerinaException e) {
            return IOUtils.createError(e.getMessage());
        }
    }

    private static Channel inFlow(ArrayValue contentArr) {
        try {
            byte[] content = shrink(contentArr);
            ByteArrayInputStream contentStream = new ByteArrayInputStream(content);
            ReadableByteChannel readableByteChannel = Channels.newChannel(contentStream);
            return new BlobIOChannel(new BlobChannel(readableByteChannel));
        } catch (Throwable e) {
            String message = "Error occurred while obtaining channel";
            throw new BallerinaException(message, e);
        }
    }

    /**
     * Shrink the byte array to fit with the given content.
     *
     * @param array byte array with elements initialized.
     * @return byte array which is shrunk.
     */
    private static byte[] shrink(ArrayValue array) {
        int contentLength = array.size();
        byte[] content = new byte[contentLength];
        System.arraycopy(array.getBytes(), 0, content, 0, contentLength);
        return content;
    }
}
