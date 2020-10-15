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

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.StructureType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import org.ballerinalang.stdlib.io.channels.base.DelimitedRecordChannel;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Extern function ballerina/io#loadToTable.
 *
 * @since 0.970.0
 */
public class GetTable {

    private static final Logger log = LoggerFactory.getLogger(GetTable.class);
    private static final String CSV_CHANNEL_DELIMITED_STRUCT_FIELD = "dc";

    private GetTable() {
    }

    public static Object getTable(BObject csvChannel, BTypedesc bTypedesc, BArray key) {
        try {
            final BObject delimitedObj =
                    (BObject) csvChannel.get(StringUtils.fromString(CSV_CHANNEL_DELIMITED_STRUCT_FIELD));
            DelimitedRecordChannel delimitedChannel = (DelimitedRecordChannel) delimitedObj
                    .getNativeData(IOConstants.TXT_RECORD_CHANNEL_NAME);
            if (delimitedChannel.hasReachedEnd()) {
                return IOUtils.createEoFError();
            }
            List<String[]> records = new ArrayList<>();
            while (delimitedChannel.hasNext()) {
                records.add(delimitedChannel.read());
            }
            return getTable(bTypedesc, key, records);
        } catch (BallerinaIOException | BallerinaException e) {
            String msg = "failed to process the delimited file: " + e.getMessage();
            return IOUtils.createError(msg);
        }
    }

    private static BTable getTable(BTypedesc bTypedesc, BArray key, List<String[]> records) {
        Type describingType = bTypedesc.getDescribingType();
        TableType newTableType;
        if (key.size() == 0) {
            newTableType = TypeCreator.createTableType(describingType, false);
        } else {
            newTableType = TypeCreator.createTableType(describingType, key.getStringArray(), false);
        }
        BTable table = ValueCreator.createTableValue(newTableType);
        StructureType structType = (StructureType) describingType;
        for (String[] fields : records) {
            final Map<String, Object> struct = getStruct(fields, structType);
            if (struct != null) {
                table.add(ValueCreator.createRecordValue(describingType.getPackage(), describingType.getName(),
                                                         struct));
            }
        }
        return table;
    }

    private static Map<String, Object> getStruct(String[] fields, final StructureType structType) {
        Map<String, Field> internalStructFields = structType.getFields();
        int fieldLength = internalStructFields.size();
        Map<String, Object> struct = null;
        if (fields.length > 0) {
            Iterator<Map.Entry<String, Field>> itr = internalStructFields.entrySet().iterator();
            struct = new HashMap();
            for (int i = 0; i < fieldLength; i++) {
                final Field internalStructField = itr.next().getValue();
                final int type = internalStructField.getFieldType().getTag();
                String fieldName = internalStructField.getFieldName();
                if (fields.length > i) {
                    String value = fields[i];
                    switch (type) {
                        case TypeTags.INT_TAG:
                        case TypeTags.FLOAT_TAG:
                        case TypeTags.STRING_TAG:
                        case TypeTags.BOOLEAN_TAG:
                            populateRecord(type, struct, fieldName, value);
                            break;
                        case TypeTags.UNION_TAG:
                            List<Type> members = ((UnionType) internalStructField.getFieldType()).getMemberTypes();
                            if (members.get(0).getTag() == TypeTags.NULL_TAG) {
                                populateRecord(members.get(1).getTag(), struct, fieldName, value);
                            } else if (members.get(1).getTag() == TypeTags.NULL_TAG) {
                                populateRecord(members.get(0).getTag(), struct, fieldName, value);
                            } else {
                                throw IOUtils.createError("unsupported nillable field for value: " + value);
                            }
                            break;
                        default:
                            throw IOUtils.createError(
                                    "type casting support only for int, float, boolean and string. "
                                            + "Invalid value for the struct field: " + value);
                    }
                } else {
                    struct.put(fieldName, null);
                }
            }
        }
        return struct;
    }

    private static void populateRecord(int type, Map<String, Object> struct, String fieldName, String value) {
        switch (type) {
            case TypeTags.INT_TAG:
                struct.put(fieldName, (value == null || value.isEmpty()) ? null : Long.parseLong(value));
                return;
            case TypeTags.FLOAT_TAG:
                struct.put(fieldName, (value == null || value.isEmpty()) ? null : Double.parseDouble(value));
                break;
            case TypeTags.STRING_TAG:
                struct.put(fieldName, value);
                break;
            case TypeTags.BOOLEAN_TAG:
                struct.put(fieldName, (value == null || value.isEmpty()) ? null : (Boolean.parseBoolean(value)));
                break;
            default:
                throw IOUtils.createError("type casting support only for int, float, boolean and string. "
                        + "Invalid value for the struct field: " + value);
        }
    }
}
