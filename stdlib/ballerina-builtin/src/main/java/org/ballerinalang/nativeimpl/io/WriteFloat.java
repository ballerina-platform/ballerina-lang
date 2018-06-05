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

package org.ballerinalang.nativeimpl.io;


import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.DataChannel;
import org.ballerinalang.nativeimpl.io.channels.base.Representation;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.data.WriteFloatEvent;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.util.concurrent.CompletableFuture;

/**
 * Native function ballerina.io#writeFloat
 *
 * @since 0.973.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "writeFloat",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "DataChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "value", type = TypeKind.FLOAT),
                @Argument(name = "len", type = TypeKind.STRING)},
        isPublic = true
)
public class WriteFloat implements NativeCallableUnit {
    /**
     * Represents data channel.
     */
    private static final int DATA_CHANNEL_INDEX = 0;
    /**
     * Specifies the index for representation.
     */
    private static final int REPRESENTATION_INDEX = 0;
    /**
     * Index which holds the value of the data to be written.
     */
    private static final int VALUE_INDEX = 0;

    /**
     * Triggers upon receiving the response.
     *
     * @param result the response received after writing double
     */
    private static EventResult writeFloatResponse(EventResult<Integer, EventContext> result) {
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        CallableUnitCallback callback = eventContext.getCallback();
        if (null != error) {
            BStruct errorStruct = IOUtils.createError(context, error.getMessage());
            context.setReturnValues(errorStruct);
        }
        callback.notifySuccess();
        return result;
    }


    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BStruct dataChannelStruct = (BStruct) context.getRefArgument(DATA_CHANNEL_INDEX);
        DataChannel channel = (DataChannel) dataChannelStruct.getNativeData(IOConstants.DATA_CHANNEL_NAME);
        double value = context.getFloatArgument(VALUE_INDEX);
        Representation representation = Representation.find(context.getRefArgument(REPRESENTATION_INDEX).stringValue());
        EventContext eventContext = new EventContext(context, callback);
        WriteFloatEvent writeIntegerEvent = new WriteFloatEvent(channel, value, representation, eventContext);
        CompletableFuture<EventResult> publish = EventManager.getInstance().publish(writeIntegerEvent);
        publish.thenApply(WriteFloat::writeFloatResponse);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}

