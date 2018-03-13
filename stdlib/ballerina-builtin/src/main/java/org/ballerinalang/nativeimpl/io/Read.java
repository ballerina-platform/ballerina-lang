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

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.bytes.ReadBytesEvent;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.lo#readBytes.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "read",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "numberOfBytes", type = TypeKind.INT),
                @Argument(name = "size", type = TypeKind.INT),
                @Argument(name = "offset", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.BLOB),
                @ReturnType(type = TypeKind.INT),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class Read implements NativeCallableUnit {

    /**
     * Specifies the index which holds the number of bytes in ballerina.lo#readBytes.
     */
    private static final int NUMBER_OF_BYTES_INDEX = 0;

    /**
     * Specifies the offset of the array to read bytes.
     */
    private static final int OFFSET_INDEX = 2;

    /**
     * Specifies the number of bytes which should be read.
     */
    private static final int SIZE_INDEX = 1;

    /**
     * Specifies the index which contains the byte channel in ballerina.lo#readBytes.
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    /*
     * Function which will be notified on the response obtained after the async operation.
     *
     * @param result context of the callback.
     * @return Once the callback is processed we further return back the result.
     */
    private static EventResult readResponse(EventResult<Integer, EventContext> result) {
        BStruct errorStruct = null;
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        Integer numberOfBytes = result.getResponse();
        CallableUnitCallback callback = eventContext.getCallback();
        byte[] content = (byte[]) eventContext.getProperties().get(ReadBytesEvent.CONTENT_PROPERTY);
        if (null != error) {
            errorStruct = IOUtils.createError(context, error.getMessage());
        }
        context.setReturnValues(new BBlob(content), new BInteger(numberOfBytes), errorStruct);
        callback.notifySuccess();
        return result;
    }

    /**
     * <p>
     * Reads bytes from a given channel.
     * </p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BStruct channel = (BStruct) context.getRefArgument(BYTE_CHANNEL_INDEX);
        int numberOfBytes = (int) context.getIntArgument(NUMBER_OF_BYTES_INDEX);
        int offset = (int) context.getIntArgument(OFFSET_INDEX);
        int size = (int) context.getIntArgument(SIZE_INDEX);
        Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        byte[] content = new byte[numberOfBytes];
        EventContext eventContext = new EventContext(context, callback);
        IOUtils.read(byteChannel, content, offset, size, eventContext, Read::readResponse);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
