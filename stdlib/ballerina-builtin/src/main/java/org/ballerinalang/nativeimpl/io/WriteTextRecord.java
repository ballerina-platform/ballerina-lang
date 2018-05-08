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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.io#writeTextRecord.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "write",
        receiver = @Receiver(type = TypeKind.STRUCT,
                structType = "DelimitedTextRecordChannel",
                structPackage = "ballerina.io"),
        args = {@Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class WriteTextRecord implements NativeCallableUnit {

    /**
     * Index of the record channel in ballerina.io#writeTextRecord.
     */
    private static final int RECORD_CHANNEL_INDEX = 0;

    /**
     * Index of the content in ballerina.io#writeTextRecord.
     */
    private static final int CONTENT_INDEX = 1;

    /**
     * Callback response received after the bytes are written.
     *
     * @param result the response received.
     * @return the result context.
     */
    private static EventResult writeResponse(EventResult<Integer, EventContext> result) {
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        CallableUnitCallback callback = eventContext.getCallback();
        Throwable error = eventContext.getError();
        if (null != error) {
            BStruct errorStruct = IOUtils.createError(context, error.getMessage());
            context.setReturnValues(errorStruct);
        }
        callback.notifySuccess();
        return result;
    }

    /**
     * Writes records to a given file.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BStruct channel = (BStruct) context.getRefArgument(RECORD_CHANNEL_INDEX);
        BStringArray content = (BStringArray) context.getRefArgument(CONTENT_INDEX);
        DelimitedRecordChannel delimitedRecordChannel = (DelimitedRecordChannel) channel.getNativeData(IOConstants
                .TXT_RECORD_CHANNEL_NAME);
        EventContext eventContext = new EventContext(context, callback);
        IOUtils.write(delimitedRecordChannel, content, eventContext, WriteTextRecord::writeResponse);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
