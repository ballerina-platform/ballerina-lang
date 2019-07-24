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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.jvm.values.TypedescValue;
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
import org.ballerinalang.stdlib.io.events.records.DelimitedRecordReadAllEvent;
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
public class GetTable {

    private static final Logger log = LoggerFactory.getLogger(GetTable.class);
    private static final String CSV_CHANNEL_DELIMITED_STRUCT_FIELD = "dc";
    private static final String TYPE_DESC_VALUE = "TypedescValue";

    public static Object getTable(Strand strand, ObjectValue csvChannel, TypedescValue typedescValue) {
        NonBlockingCallback callback = new NonBlockingCallback(strand);
        try {
            final ObjectValue delimitedObj = (ObjectValue) csvChannel.get(CSV_CHANNEL_DELIMITED_STRUCT_FIELD);
            DelimitedRecordChannel delimitedChannel = (DelimitedRecordChannel) delimitedObj
                    .getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            EventContext eventContext = new EventContext(callback);
            eventContext.getProperties().put(TYPE_DESC_VALUE, typedescValue);
            DelimitedRecordReadAllEvent event = new DelimitedRecordReadAllEvent(delimitedChannel, eventContext);
            Register register = EventRegister.getFactory().register(event, GetTable::getResponse);
            eventContext.setRegister(register);
            register.submit();
        } catch (Exception e) {
            String msg = "Failed to process the delimited file: " + e.getMessage();
            log.error(msg, e);
            callback.setReturnValues(IOUtils.createError(msg));
            callback.notifySuccess();
        }
        return null;
    }

    private static EventResult getResponse(EventResult<List, EventContext> result) {
        TableValue table;
        EventContext eventContext = result.getContext();
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

    private static TableValue getbTable(EventContext eventContext, List records) throws BallerinaException {
        TypedescValue type = (TypedescValue) eventContext.getProperties().get(TYPE_DESC_VALUE);
        TableValue table = new TableValue(new org.ballerinalang.jvm.types.BTableType(type.getType()), null, null, null);
        BStructureType structType = (BStructureType) type.getType();
        for (Object obj : records) {
            String[] fields = (String[]) obj;
            final MapValueImpl<String, Object> struct = getStruct(fields, structType);
            if (struct != null) {
                table.addData(struct);
            }
        }
        return table;
    }

    private static MapValueImpl<String, Object> getStruct(String[] fields, final BStructureType structType) {
        Map<String, BField> internalStructFields = structType.getFields();
        int fieldLength = internalStructFields.size();
        MapValueImpl<String, Object> struct = null;
        if (fields.length > 0) {
            if (internalStructFields.size() != fields.length) {
                String msg = "Record row fields count and the give struct's fields count are mismatch";
                throw new BallerinaException(msg);
            }
            Iterator<Map.Entry<String, BField>> itr = internalStructFields.entrySet().iterator();
            struct = new MapValueImpl<>(structType);
            for (int i = 0; i < fieldLength; i++) {
                String value = fields[i];
                final BField internalStructField = itr.next().getValue();
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
                        throw new BallerinaException(
                                "Type casting support only for int, float, boolean and string. "
                                        + "Invalid value for the struct field: " + value);
                }
            }
        }
        return struct;
    }
}
