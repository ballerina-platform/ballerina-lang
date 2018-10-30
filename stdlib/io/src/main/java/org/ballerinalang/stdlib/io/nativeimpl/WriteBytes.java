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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.bytes.WriteBytesEvent;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;

/**
 * Extern function ballerina.lo#writeBytes.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "write",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "WritableByteChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @Argument(name = "offset", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.INT),
                @ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class WriteBytes implements NativeCallableUnit {

    /**
     * Index which holds the byte channel in ballerina/io#writeBytes.
     */
    private static final int BYTE_CHANNEL_INDEX = 0;

    /**
     * Index which holds the content in ballerina/io#writeBytes.
     */
    private static final int CONTENT_INDEX = 1;

    /*
     * Index which holds the start offset in ballerina/io#writeBytes.
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
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, error.getMessage());
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
        BMap<String, BValue> channel = (BMap<String, BValue>) context.getRefArgument(BYTE_CHANNEL_INDEX);
        byte[] content = ((BByteArray) context.getRefArgument(CONTENT_INDEX)).getBytes();
        int offset = (int) context.getIntArgument(START_OFFSET_INDEX);
        Channel byteChannel = (Channel) channel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        EventContext eventContext = new EventContext(context, callback);
        WriteBytesEvent writeBytesEvent = new WriteBytesEvent(byteChannel, content, offset, eventContext);
        Register register = EventRegister.getFactory().register(writeBytesEvent, WriteBytes::writeResponse);
        eventContext.setRegister(register);
        register.submit();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
