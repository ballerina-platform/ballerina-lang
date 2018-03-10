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
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Native function ballerina.io#nextTextRecords.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.io",
        functionName = "nextTextRecord",
        receiver = @Receiver(type = TypeKind.STRUCT,
                structType = "DelimitedRecordChannel",
                structPackage = "ballerina.io"),
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING),
                @ReturnType(type = TypeKind.STRUCT, structType = "IOError", structPackage = "ballerina.io")},
        isPublic = true
)
public class NextTextRecord extends BlockingNativeCallableUnit {
    /**
     * Specifies the index which contains the byte channel in ballerina.io#nextTextRecord.
     */
    private static final int TXT_RECORD_CHANNEL_INDEX = 0;

    /**
     * Will be the I/O event handler.
     */
    private EventManager eventManager = EventManager.getInstance();

    private static final Logger log = LoggerFactory.getLogger(NextTextRecord.class);

    /*
     * Response obtained after reading record.
     *
     * @param result the result obtained after processing the record.
     * @return the response obtained after reading record.
     */
/*
    private static EventResult readRecordResponse(EventResult<String[], EventContext> result) {
        BStruct errorStruct;
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        if (null != error) {
            errorStruct = IOUtils.createError(context, error.getMessage());
        }
        String[] fields = result.getResponse();
        return result;
    }
*/

    /**
     * Reads bytes asynchronously.
     *
     * @param recordChannel channel the bytes should be read from.
     * @param context       event context.
     * @return the fields which were read.
     * @throws ExecutionException   errors which occur during execution.
     * @throws InterruptedException during interrupt error.
     */
    private String[] readRecord(DelimitedRecordChannel recordChannel, EventContext context) throws ExecutionException,
            InterruptedException {
        DelimitedRecordReadEvent event = new DelimitedRecordReadEvent(recordChannel, context
        );
        CompletableFuture<EventResult> future = eventManager.publish(event);
        //TODO when async functions are available this should be modified
        //future.thenApply(NextTextRecord::readRecordResponse);
        EventResult eventResult = future.get();
        Throwable error = ((EventContext) eventResult.getContext()).getError();
        if (null != error) {
            throw new ExecutionException(error);
        }
        return (String[]) eventResult.getResponse();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Context context) {
        BStringArray record = null;
        BStruct errorStruct = null;
        try {
            BStruct channel = (BStruct) context.getRefArgument(TXT_RECORD_CHANNEL_INDEX);

            DelimitedRecordChannel delimitedRecordChannel = (DelimitedRecordChannel) channel.getNativeData(IOConstants
                    .TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context);
            String[] recordValue = readRecord(delimitedRecordChannel, eventContext);
            record = new BStringArray(recordValue);
        } catch (Throwable e) {
            String message = "Error occurred while reading text records:" + e.getMessage();
            log.error(message, e);
            errorStruct = IOUtils.createError(context, message);
        }
        context.setReturnValues(record, errorStruct);
    }
}
