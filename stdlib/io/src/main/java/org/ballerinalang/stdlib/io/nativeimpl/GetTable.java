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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    //TODO Table remove - Fix

//    private static MapValueImpl<String, Object> getStruct(String[] fields, final BStructureType structType) {
//        Map<String, BField> internalStructFields = structType.getFields();
//        int fieldLength = internalStructFields.size();
//        MapValueImpl<String, Object> struct = null;
//        if (fields.length > 0) {
//            Iterator<Map.Entry<String, BField>> itr = internalStructFields.entrySet().iterator();
//            struct = new MapValueImpl<>(structType);
//            for (int i = 0; i < fieldLength; i++) {
//                final BField internalStructField = itr.next().getValue();
//                final int type = internalStructField.getFieldType().getTag();
//                String fieldName = internalStructField.getFieldName();
//                if (fields.length > i) {
//                    String value = fields[i];
//                    switch (type) {
//                        case TypeTags.INT_TAG:
//                        case TypeTags.FLOAT_TAG:
//                        case TypeTags.STRING_TAG:
//                        case TypeTags.BOOLEAN_TAG:
//                            populateRecord(type, struct, fieldName, value);
//                        break;
//                        case TypeTags.UNION_TAG:
//                            List<BType> members = ((BUnionType) internalStructField.getFieldType()).getMemberTypes();
//                            if (members.get(0).getTag() == TypeTags.NULL_TAG) {
//                                populateRecord(members.get(1).getTag(), struct, fieldName, value);
//                            } else if (members.get(1).getTag() == TypeTags.NULL_TAG) {
//                                populateRecord(members.get(0).getTag(), struct, fieldName, value);
//                            } else {
//                                throw IOUtils.createError("unsupported nillable field for value: " + value);
//                            }
//                            break;
//                        default:
//                            throw IOUtils.createError(
//                                    "type casting support only for int, float, boolean and string. "
//                                            + "Invalid value for the struct field: " + value);
//                    }
//                } else {
//                    struct.put(fieldName, null);
//                }
//            }
//        }
//        return struct;
//    }
//
//    private static void populateRecord(int type, MapValueImpl<String, Object> struct, String fieldName,
//    String value) {
//        switch (type) {
//            case TypeTags.INT_TAG:
//                struct.put(fieldName, value == null ? null : Long.parseLong(value));
//                return;
//            case TypeTags.FLOAT_TAG:
//                struct.put(fieldName, value == null ? null : Double.parseDouble(value));
//                break;
//            case TypeTags.STRING_TAG:
//                struct.put(fieldName, value);
//                break;
//            case TypeTags.BOOLEAN_TAG:
//                struct.put(fieldName, value == null ? null : (Boolean.parseBoolean(value)));
//                break;
//            default:
//                throw IOUtils.createError("type casting support only for int, float, boolean and string. "
//                        + "Invalid value for the struct field: " + value);
//        }
//    }
}
