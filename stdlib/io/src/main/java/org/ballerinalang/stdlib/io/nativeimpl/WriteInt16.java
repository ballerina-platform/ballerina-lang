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

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.io.channels.base.DataChannel;
import org.ballerinalang.stdlib.io.channels.base.Representation;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.data.WriteIntegerEvent;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;

/**
 * Extern function ballerina/io#writeInt16.
 *
 * @since 0.973.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "writeInt16",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "WritableDataChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "value", type = TypeKind.INT)},
        isPublic = true
)
public class WriteInt16 {

    public static Object writeInt16(Strand strand, ObjectValue dataChannelObj, long value) {
        DataChannel channel = (DataChannel) dataChannelObj.getNativeData(IOConstants.DATA_CHANNEL_NAME);
        EventContext eventContext = new EventContext(new NonBlockingCallback(strand));
        WriteIntegerEvent writeFloatEvent = new WriteIntegerEvent(channel, value, Representation.BIT_16, eventContext);
        Register register = EventRegister.getFactory().register(writeFloatEvent, WriteInt16::writeResponse);
        eventContext.setRegister(register);
        register.submit();
        return null;
    }

    /**
     * Triggers upon receiving the response.
     *
     * @param result the response received after writing int.
     */
    private static EventResult writeResponse(EventResult<Long, EventContext> result) {
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
