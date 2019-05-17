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

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.records.HasNextDelimitedRecordEvent;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;

/**
 * Extern function ballerina/io#hasNextTextRecord.
 *
 * @since 0.961.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "hasNext",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "ReadableTextRecordChannel",
                structPackage = "ballerina/io"),
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class HasNextTextRecord implements NativeCallableUnit {
    /**
     * Specifies the index which contains the byte channel in ballerina/io#hasNextTextRecord.
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
        IOUtils.validateChannelState(eventContext);
        callback.notifySuccess();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> channel = (BMap<String, BValue>) context.getRefArgument(TXT_RECORD_CHANNEL_INDEX);
        if (channel.getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME) != null) {
            DelimitedRecordChannel textRecordChannel =
                    (DelimitedRecordChannel) channel.getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context, callback);
            HasNextDelimitedRecordEvent hasNextEvent = new HasNextDelimitedRecordEvent(textRecordChannel,
                    eventContext);
            Register register = EventRegister.getFactory().register(hasNextEvent, HasNextTextRecord::response);
            eventContext.setRegister(register);
            register.submit();
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public static Object hasNext(Strand strand, ObjectValue channel) {
        if (channel.getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME) != null) {
            //TODO : NonBlockingCallback is temporary fix to handle non blocking call
            NonBlockingCallback callback = new NonBlockingCallback(strand);

            DelimitedRecordChannel textRecordChannel =
                    (DelimitedRecordChannel) channel.getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(callback);
            HasNextDelimitedRecordEvent hasNextEvent = new HasNextDelimitedRecordEvent(textRecordChannel,
                                                                                       eventContext);
            Register register = EventRegister.getFactory().register(hasNextEvent, HasNextTextRecord::getResponse);
            eventContext.setRegister(register);
            register.submit();
            //TODO : Remove callback once strand non-blocking support is given
            callback.sync();
            return callback.getReturnValue();
        }
        return false;
    }

    /**
     * Responds whether a next record exists.
     *
     * @param result the result processed.
     * @return result context.
     */
    private static EventResult getResponse(EventResult<Boolean, EventContext> result) {
        EventContext eventContext = result.getContext();
        //TODO : Remove callback once strand non-blocking support is given
        NonBlockingCallback callback = eventContext.getNonBlockingCallback();
        Boolean response = result.getResponse();
        callback.setReturnValues(response);
        IOUtils.validateChannelState(eventContext);
        callback.notifySuccess();
        return result;
    }
}
