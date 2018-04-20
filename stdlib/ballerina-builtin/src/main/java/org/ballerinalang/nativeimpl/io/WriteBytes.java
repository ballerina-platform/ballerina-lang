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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.lo#writeBytes.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "write",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "content", type = TypeKind.BLOB),
                @Argument(name = "offset", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.INT),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class WriteBytes implements NativeCallableUnit {

    /**
     * Index which holds the byte channel in ballerina.io#writeBytes.
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    /**
     * Index which holds the content in ballerina.io#writeBytes.
     */
    private static final int CONTENT_INDEX = 0;

    /*
     * Index which holds the start offset in ballerina.io#writeBytes.
     */
    private static final int START_OFFSET_INDEX = 0;

    /*
     * Function which will be notified on the response obtained after the async operation.
     *
     * @param result context of the callback.
     * @return Once the callback is processed we further return back the result.
     */
    private static EventResult writeResponse(EventResult<Integer, EventContext> result) {
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        CallableUnitCallback callback = eventContext.getCallback();
        if (null != error) {
            BStruct errorStruct = IOUtils.createError(context, error.getMessage());
            context.setReturnValues(errorStruct);
        } else {
            Integer numberOfBytesWritten = result.getResponse();
            context.setReturnValues(new BInteger(numberOfBytesWritten));
        }
        callback.notifySuccess();
        return result;
    }


    /**
     * Writes bytes to a given channel.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BStruct channel = (BStruct) context.getRefArgument(BYTE_CHANNEL_INDEX);
        byte[] content = context.getBlobArgument(CONTENT_INDEX);
        int offset = (int) context.getIntArgument(START_OFFSET_INDEX);
        Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        EventContext eventContext = new EventContext(context, callback);
        IOUtils.write(byteChannel, content, offset, eventContext, WriteBytes::writeResponse);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
