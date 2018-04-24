/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.nativeimpl.io;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.nativeimpl.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.nativeimpl.io.events.EventContext;
import org.ballerinalang.nativeimpl.io.events.EventManager;
import org.ballerinalang.nativeimpl.io.events.EventResult;
import org.ballerinalang.nativeimpl.io.events.records.DelimitedRecordReadAllEvent;
import org.ballerinalang.nativeimpl.io.utils.IOUtils;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Native function ballerina.io#loadToTable.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "getTable",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "CSVChannel", structPackage = "ballerina.io"),
        args = {@Argument(name = "structType", type = TypeKind.TYPEDESC)},
        returnType = {
                @ReturnType(type = TypeKind.TABLE),
                @ReturnType(type = TypeKind.STRUCT, structType = "error", structPackage = "ballerina.builtin")},
        isPublic = true
)
public class GetTable implements NativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(GetTable.class);

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BStruct csvChannel = (BStruct) context.getRefArgument(0);
        try {
            final BStruct delimitedStruct = (BStruct) csvChannel.getRefField(0);
            DelimitedRecordChannel delimitedChannel = (DelimitedRecordChannel) delimitedStruct
                    .getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context, callback);
            DelimitedRecordReadAllEvent event = new DelimitedRecordReadAllEvent(delimitedChannel, eventContext);
            CompletableFuture<EventResult> future = EventManager.getInstance().publish(event);
            future.thenApply(GetTable::response);
        } catch (Exception e) {
            String msg = "Failed to process the delimited file: " + e.getMessage();
            log.error(msg, e);
            context.setReturnValues(IOUtils.createError(context, msg));
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    private static EventResult response(EventResult<List, EventContext> result) {
        BStruct errorStruct;
        BTable table;
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        if (null != error) {
            errorStruct = IOUtils.createError(context, error.getMessage());
            context.setReturnValues(errorStruct);
        } else {
            try {
                List records = result.getResponse();
                table = getbTable(context, records);
                context.setReturnValues(table);
            } catch (Throwable e) {
                errorStruct = IOUtils.createError(context, e.getMessage());
                context.setReturnValues(errorStruct);
            }
        }
        CallableUnitCallback callback = eventContext.getCallback();
        callback.notifySuccess();
        return result;
    }

    private static BTable getbTable(Context context, List records) throws BallerinaIOException {
        BTypeDescValue type = (BTypeDescValue) context.getRefArgument(1);
        BTable table = new BTable(new BTableType(type.value()), null);
        BStructType structType = (BStructType) type.value();
        for (Object obj : records) {
            String[] fields = (String[]) obj;
            final BStruct struct = getStruct(fields, structType);
            if (struct != null) {
                table.addData(struct);
            }
        }
        return table;
    }

    private static BStruct getStruct(String[] fields, final BStructType structType) {
        BStructType.StructField[] internalStructFields = structType.getStructFields();
        int fieldLength = internalStructFields.length;
        BStruct struct = null;
        if (fields.length > 0) {
            if (internalStructFields.length != fields.length) {
                String msg = "Record row fields count and the give struct's fields count are mismatch";
                throw new BallerinaIOException(msg);
            }
            struct = new BStruct(structType);
            int longRegIndex = -1;
            int doubleRegIndex = -1;
            int stringRegIndex = -1;
            int booleanRegIndex = -1;
            for (int i = 0; i < fieldLength; i++) {
                String value = fields[i];
                final BStructType.StructField internalStructField = internalStructFields[i];
                final int type = internalStructField.getFieldType().getTag();
                switch (type) {
                    case TypeTags.INT_TAG:
                        struct.setIntField(++longRegIndex, Long.parseLong(value));
                        break;
                    case TypeTags.FLOAT_TAG:
                        struct.setFloatField(++doubleRegIndex, Double.parseDouble(value));
                        break;
                    case TypeTags.STRING_TAG:
                        struct.setStringField(++stringRegIndex, value);
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        struct.setBooleanField(++booleanRegIndex, (Boolean.parseBoolean(value)) ? 1 : 0);
                        break;
                    default:
                        throw new BallerinaIOException("Type casting support only for int, float, boolean and string. "
                                + "Invalid value for the struct field: " + value);
                }
            }
        }
        return struct;
    }

}
