/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.records.HasNextDelimitedRecordEvent;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.concurrent.CompletableFuture;

/**
 * Native function ballerina.io#hasNextTextRecord.
 *
 * @since 0.961.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "hasNext",
        receiver = @Receiver(type = TypeKind.STRUCT,
                structType = "DelimitedTextRecordChannel",
                structPackage = "ballerina.io"),
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class HasNextTextRecord implements NativeCallableUnit {
    /**
     * Specifies the index which contains the byte channel in ballerina.io#hasNextTextRecord.
     */
    private static final int TXT_RECORD_CHANNEL_INDEX = 0;

    /**
     * Responds whether a next record exists.
     *
     * @param result the result processed.
     * @return result context.
     */
    private static EventResult response(EventResult<Boolean, EventContext> result) {
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        CallableUnitCallback callback = eventContext.getCallback();
        Boolean response = result.getResponse();
        context.setReturnValues(new BBoolean(response));
        callback.notifySuccess();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BStruct channel = (BStruct) context.getRefArgument(TXT_RECORD_CHANNEL_INDEX);
        if (channel.getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME) != null) {
            DelimitedRecordChannel textRecordChannel =
                    (DelimitedRecordChannel) channel.getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context, callback);
            HasNextDelimitedRecordEvent hasNextEvent = new HasNextDelimitedRecordEvent(textRecordChannel,
                    eventContext);
            CompletableFuture<EventResult> event = EventManager.getInstance().publish(hasNextEvent);
            event.thenApply(HasNextTextRecord::response);
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
