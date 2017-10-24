/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.base.AbstractChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.io#close.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "close",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
        isPublic = true
)
public class Close extends AbstractNativeFunction {

    /**
     * The index of the ByteChannel in ballerina.io#close().
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    /**
     * Reads bytes from a given channel.
     *
     * <p>
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct channel;
        try {
            channel = (BStruct) getRefArgument(context, BYTE_CHANNEL_INDEX);
            AbstractChannel byteChannel = (AbstractChannel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            byteChannel.close();
        } catch (Throwable e) {
            String message = "Failed to close the channel. " + e.getMessage();
            throw new BallerinaException(message, context);
        }
        return VOID_RETURN;
    }
}
