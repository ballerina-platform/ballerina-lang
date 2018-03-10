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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.records.CloseDelimitedRecordEvent;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Native function ballerina.io#closeTextRecordChannel.
 *
 * @since 0.95
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "closeDelimitedRecordChannel",
        receiver = @Receiver(type = TypeKind.STRUCT,
                structType = "DelimitedRecordChannel",
                structPackage = "ballerina.io"),
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class CloseDelimitedRecordChannel extends BlockingNativeCallableUnit {

    /**
     * The index of the DelimitedRecordChannel in ballerina.io#closeDelimitedRecordChannel().
     */
    private static final int RECORD_CHANNEL_INDEX = 0;

    private static final Logger log = LoggerFactory.getLogger(CloseDelimitedRecordChannel.class);

    /**
     * <p>
     * Closes a text record channel.
     * </p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BStruct errorStruct = null;
        try {
            BStruct channel = (BStruct) context.getRefArgument(RECORD_CHANNEL_INDEX);
            DelimitedRecordChannel recordChannel = (DelimitedRecordChannel)
                    channel.getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context);
            CloseDelimitedRecordEvent closeEvent = new CloseDelimitedRecordEvent(recordChannel, eventContext);
            CompletableFuture<EventResult> future = EventManager.getInstance().publish(closeEvent);
            EventResult eventResult = future.get();
            Throwable error = ((EventContext) eventResult.getContext()).getError();
            if (null != error) {
                errorStruct = IOUtils.createError(context, error.getMessage());
            }
        } catch (Throwable e) {
            String message = "Failed to close the text record channel:" + e.getMessage();
            log.error(message, e);
            errorStruct = IOUtils.createError(context, message);
        }
        context.setReturnValues(errorStruct);
    }
}
