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

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.events.EventRegister;
import org.ballerinalang.stdlib.io.events.EventResult;
import org.ballerinalang.stdlib.io.events.Register;
import org.ballerinalang.stdlib.io.events.records.DelimitedRecordReadAllEvent;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Extern function ballerina/io#loadToTable.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "getTable",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ReadableCSVChannel", structPackage = "ballerina/io"),
        args = {@Argument(name = "structType", type = TypeKind.TYPEDESC)},
        returnType = {
                @ReturnType(type = TypeKind.TABLE),
                @ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class GetTable implements NativeCallableUnit {

    private static final Logger log = LoggerFactory.getLogger(GetTable.class);
    private static final String CSV_CHANNEL_DELIMITED_STRUCT_FIELD = "dc";
    private static final String TYPE_DESC_VALUE = "TypedescValue";

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> csvChannel = (BMap<String, BValue>) context.getRefArgument(0);
        try {
            final BMap<String, BValue> delimitedStruct =
                    (BMap<String, BValue>) csvChannel.get(CSV_CHANNEL_DELIMITED_STRUCT_FIELD);
            DelimitedRecordChannel delimitedChannel = (DelimitedRecordChannel) delimitedStruct
                    .getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(context, callback);
            DelimitedRecordReadAllEvent event = new DelimitedRecordReadAllEvent(delimitedChannel, eventContext);
            Register register = EventRegister.getFactory().register(event, GetTable::response);
            eventContext.setRegister(register);
            register.submit();
        } catch (Exception e) {
            String msg = "Failed to process the delimited file: " + e.getMessage();
            log.error(msg, e);
            BError errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, msg);
            context.setReturnValues(errorStruct);
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    private static EventResult response(EventResult<List, EventContext> result) {
        BError errorStruct;
        BTable table;
        EventContext eventContext = result.getContext();
        Context context = eventContext.getContext();
        Throwable error = eventContext.getError();
        if (null != error) {
            errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, error.getMessage());
            context.setReturnValues(errorStruct);
        } else {
            try {
                List records = result.getResponse();
                table = getbTable(context, records);
                context.setReturnValues(table);
            } catch (Throwable e) {
                errorStruct = IOUtils.createError(context, IOConstants.IO_ERROR_CODE, e.getMessage());
                context.setReturnValues(errorStruct);
            }
        }
        IOUtils.validateChannelState(eventContext);
        CallableUnitCallback callback = eventContext.getCallback();
        callback.notifySuccess();
        return result;
    }

    private static BTable getbTable(Context context, List records) throws BallerinaIOException {
        BTypeDescValue type = (BTypeDescValue) context.getRefArgument(1);
        BTable table = new BTable(new BTableType(type.value()), null, null, null);
        BStructureType structType = (BStructureType) type.value();
        for (Object obj : records) {
            String[] fields = (String[]) obj;
            final BMap<String, BValue> struct = getStruct(fields, structType);
            if (struct != null) {
                table.addData(struct);
            }
        }
        return table;
    }

    private static BMap<String, BValue> getStruct(String[] fields, final BStructureType structType) {
        Map<String, BField> internalStructFields = structType.getFields();
        int fieldLength = internalStructFields.size();
        BMap<String, BValue> struct = null;
        if (fields.length > 0) {
            if (internalStructFields.size() != fields.length) {
                String msg = "Record row fields count and the give struct's fields count are mismatch";
                throw new BallerinaIOException(msg);
            }
            Iterator<Map.Entry<String, BField>> itr = internalStructFields.entrySet().iterator();
            struct = new BMap<>(structType);
            for (int i = 0; i < fieldLength; i++) {
                String value = fields[i];
                final BField internalStructField = itr.next().getValue(); // TODO: 11/15/18 double check this logic
                final int type = internalStructField.getFieldType().getTag();
                String fieldName = internalStructField.fieldName;
                switch (type) {
                    case TypeTags.INT_TAG:
                        struct.put(fieldName, new BInteger(Long.parseLong(value)));
                        break;
                    case TypeTags.FLOAT_TAG:
                        struct.put(fieldName, new BFloat(Double.parseDouble(value)));
                        break;
                    case TypeTags.STRING_TAG:
                        struct.put(fieldName, new BString(value));
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        struct.put(fieldName, new BBoolean((Boolean.parseBoolean(value))));
                        break;
                    default:
                        throw new BallerinaIOException("Type casting support only for int, float, boolean and string. "
                                + "Invalid value for the struct field: " + value);
                }
            }
        }
        return struct;
    }

    public static Object getTable(Strand strand, ObjectValue csvChannel, TypedescValue typedescValue) {
        //TODO : NonBlockingCallback is temporary fix to handle non blocking call
        NonBlockingCallback callback = new NonBlockingCallback(strand);

        try {
            //TODO : Recheck the casting
            final ObjectValue delimitedObj = (ObjectValue) csvChannel.get(CSV_CHANNEL_DELIMITED_STRUCT_FIELD);
            DelimitedRecordChannel delimitedChannel = (DelimitedRecordChannel) delimitedObj
                    .getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(callback);
            eventContext.getProperties().put(TYPE_DESC_VALUE, typedescValue);
            DelimitedRecordReadAllEvent event = new DelimitedRecordReadAllEvent(delimitedChannel, eventContext);
            Register register = EventRegister.getFactory().register(event, GetTable::getResponse);
            eventContext.setRegister(register);
            register.submit();
            //TODO : Remove callback once strand non-blocking support is given
            callback.sync();
        } catch (Exception e) {
            String msg = "Failed to process the delimited file: " + e.getMessage();
            log.error(msg, e);
            callback.setReturnValues(IOUtils.createError(msg));
            callback.notifySuccess();
        }
        return callback.getReturnValue();
    }

    private static EventResult getResponse(EventResult<List, EventContext> result) {
        TableValue table;
        EventContext eventContext = result.getContext();
        //TODO : Remove callback once strand non-blocking support is given
        NonBlockingCallback callback = eventContext.getNonBlockingCallback();
        Throwable error = eventContext.getError();
        if (null != error) {
            callback.setReturnValues(IOUtils.createError(error.getMessage()));
        } else {
            try {
                List records = result.getResponse();
                table = getbTable(eventContext, records);
                callback.setReturnValues(table);
            } catch (Throwable e) {
                callback.setReturnValues(IOUtils.createError(e.getMessage()));
            }
        }
        IOUtils.validateChannelState(eventContext);
        callback.notifySuccess();
        return result;
    }

    private static TableValue getbTable(EventContext eventContext, List records)
            throws org.ballerinalang.jvm.util.exceptions.BallerinaException {
        TypedescValue type = (TypedescValue) eventContext.getProperties().get(TYPE_DESC_VALUE);
        TableValue table = new TableValue(new org.ballerinalang.jvm.types.BTableType(type.getType()), null, null, null);
        org.ballerinalang.jvm.types.BStructureType structType =
                (org.ballerinalang.jvm.types.BStructureType) type.getType();
        for (Object obj : records) {
            String[] fields = (String[]) obj;
            final MapValueImpl<String, Object> struct = getStruct(fields, structType);
            if (struct != null) {
                table.addData(struct);
            }
        }
        return table;
    }

    private static MapValueImpl<String, Object> getStruct(String[] fields,
                                                          final org.ballerinalang.jvm.types.BStructureType structType) {
        //TODO : Recheck following migration
        Map<String, org.ballerinalang.jvm.types.BField> internalStructFields = structType.getFields();
        int fieldLength = internalStructFields.size();
        MapValueImpl<String, Object> struct = null;
        if (fields.length > 0) {
            if (internalStructFields.size() != fields.length) {
                String msg = "Record row fields count and the give struct's fields count are mismatch";
                throw new org.ballerinalang.jvm.util.exceptions.BallerinaException(msg);
            }
            Iterator<Map.Entry<String, org.ballerinalang.jvm.types.BField>> itr =
                    internalStructFields.entrySet().iterator();
            struct = new MapValueImpl<>(structType);
            for (int i = 0; i < fieldLength; i++) {
                String value = fields[i];
                final org.ballerinalang.jvm.types.BField internalStructField =
                        itr.next().getValue(); // TODO: 11/15/18 double check this logic
                final int type = internalStructField.getFieldType().getTag();
                String fieldName = internalStructField.getFieldName();
                switch (type) {
                    case TypeTags.INT_TAG:
                        struct.put(fieldName, Long.parseLong(value));
                        break;
                    case TypeTags.FLOAT_TAG:
                        struct.put(fieldName, Double.parseDouble(value));
                        break;
                    case TypeTags.STRING_TAG:
                        struct.put(fieldName, value);
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        struct.put(fieldName, (Boolean.parseBoolean(value)));
                        break;
                    default:
                        throw new org.ballerinalang.jvm.util.exceptions.BallerinaException(
                                "Type casting support only for int, float, boolean and string. "
                                        + "Invalid value for the struct field: " + value);
                }
            }
        }
        return struct;
    }
}
