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

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.records.DelimitedRecordWriteEvent;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;

/**
 * Extern function ballerina/io#writeTextRecord.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "write",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "WritableTextRecordChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class WriteTextRecord {

    public static Object write(Strand strand, ObjectValue channel, ArrayValue content) {
        DelimitedRecordChannel delimitedRecordChannel = (DelimitedRecordChannel) channel.getNativeData(
                IOConstants.TXT_RECORD_CHANNEL_NAME);
        EventContext eventContext = new EventContext(new NonBlockingCallback(strand));
        DelimitedRecordWriteEvent recordWriteEvent = new DelimitedRecordWriteEvent(delimitedRecordChannel, content,
                                                                                   eventContext, true);
        Register register = EventRegister.getFactory().register(recordWriteEvent, WriteTextRecord::writeTextResponse);
        eventContext.setRegister(register);
        register.submit();
        return null;
    }

    /**
     * Callback response received after the bytes are written.
     *
     * @param result the response received.
     * @return the result context.
     */
    private static EventResult writeTextResponse(EventResult<Integer, EventContext> result) {
        EventContext eventContext = result.getContext();
        NonBlockingCallback callback = eventContext.getNonBlockingCallback();
        Throwable error = eventContext.getError();
        if (null != error) {
            callback.setReturnValues(IOUtils.createError(error.getMessage()));
        }
        callback.notifySuccess();
        return result;
    }
}
