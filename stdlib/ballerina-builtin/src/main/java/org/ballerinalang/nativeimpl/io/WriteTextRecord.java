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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.records.DelimitedRecordWriteEvent;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Native function ballerina.io#writeTextRecord.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "writeTextRecord",
        receiver = @Receiver(type = TypeKind.STRUCT,
                structType = "DelimitedRecordChannel",
                structPackage = "ballerina.io"),
        args = {@Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class WriteTextRecord extends AbstractNativeFunction {

    /**
     * Index of the record channel in ballerina.io#writeTextRecord.
     */
    private static final int RECORD_CHANNEL_INDEX = 0;

    /**
     * Index of the content in ballerina.io#writeTextRecord.
     */
    private static final int CONTENT_INDEX = 1;

    /**
     * Will be the I/O event handler.
     */
    private EventManager eventManager = EventManager.getInstance();

    private static final Logger log = LoggerFactory.getLogger(WriteTextRecord.class);

/*
    private static EventResult writeRecordResponse(EventResult<Integer, EventContext> result) {
        BStruct errorStruct;
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        if (null != error) {
            errorStruct = IOUtils.createError(context, error.getMessage());
        }
        return result;
    }
*/

    /**
     * Asynchronously writes records to the channel.
     *
     * @param recordChannel channel the records should be written.
     * @param records       the record content.
     * @param context       event context.
     * @throws ExecutionException   during interrupt error.
     * @throws InterruptedException errors which occurs while execution.
     */
    private void writeTextRecord(DelimitedRecordChannel recordChannel, BStringArray records, EventContext context)
            throws ExecutionException, InterruptedException {
        DelimitedRecordWriteEvent recordWriteEvent = new DelimitedRecordWriteEvent(recordChannel, records, context);
        CompletableFuture<EventResult> future = eventManager.publish(recordWriteEvent);
        //future.thenApply(WriteTextRecord::writeRecordResponse);
        EventResult eventResult = future.get();
        Throwable error = ((EventContext) eventResult.getContext()).getError();
        if (error != null) {
            throw new ExecutionException(error);
        }
    }

    /**
     * Writes records for a given file.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public BValue[] execute(Context context) {
        BStruct channel;
        BStringArray content;
        BStruct errorStruct = null;
        try {
            channel = (BStruct) getRefArgument(context, RECORD_CHANNEL_INDEX);
            content = (BStringArray) getRefArgument(context, CONTENT_INDEX);
            DelimitedRecordChannel delimitedRecordChannel = (DelimitedRecordChannel) channel.getNativeData(IOConstants
                    .TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context);
            writeTextRecord(delimitedRecordChannel, content, eventContext);
        } catch (Throwable e) {
            String message = "Error occurred while writing text record:" + e.getMessage();
            log.error(message);
            errorStruct = IOUtils.createError(context, message);
        }
        return getBValues(errorStruct);
    }
}
