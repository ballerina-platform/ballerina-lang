/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ObjectTypeInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.program.BLangFunctions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

/**
 * Util Class for handling structs in Ballerina VM.
 *
 * @since 0.89
 */
public class BLangVMStructs {

    /**
     * Create BStruct for given StructInfo and BValues.
     *
     * @param structInfo {@link StructureTypeInfo} of the BStruct
     * @param values     field values of the BStruct.
     * @return Struct value.
     */
    public static BMap<String, BValue> createBStruct(StructureTypeInfo structInfo, Object... values) {
        BStructureType structType = structInfo.getType();
        BMap<String, BValue> bStruct = new BMap<>(structType);

        Map<String, BField> structFields = structType.getFields();
        int valCount = 0;
        for (BField field : structFields.values()) {
            BValue value;
            if (values.length >= valCount + 1) {
                value = getBValue(field.fieldType, values[valCount]);
            } else {
                value = field.fieldType.getEmptyValue();
            }
            bStruct.put(field.fieldName, value);
            valCount++;
        }
        return bStruct;
    }

    /**
     * This is a helper method to create a object in native code.
     *
     * WARNING - please be cautious when using this method, if you have non blocking calls inside the
     * object constructor, then using this method may cause thread blocking scenarios.
     *
     * @param objectInfo {@link ObjectTypeInfo} of the BStruct
     * @param values     field values of the BStruct.
     * @return Object instance.
     */
    public static BMap<String, BValue> createObject(ObjectTypeInfo objectInfo, BValue... values) {
        BStructureType structType = objectInfo.getType();
        BMap<String, BValue> bStruct = new BMap<>(structType);
        BValue[] vals = new BValue[values.length + 1];
        vals[0] = bStruct;
        System.arraycopy(values, 0, vals, 1, values.length);
        BLangFunctions.invokeCallable(objectInfo.initializer, vals);
        return bStruct;
    }

    private static BValue getBValue(BType type, Object value) {
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                if (value instanceof Integer) {
                    return new BInteger(((Integer) value).longValue());
                } else if (value instanceof Long) {
                    return new BInteger((Long) value);
                } else if (value instanceof BInteger) {
                    return (BInteger) value;
                }
                break;
            case TypeTags.BYTE_TAG:
                if (value instanceof Byte) {
                    return new BByte(((Byte) value));
                } else if (value instanceof Integer) {
                    return new BByte(((Integer) value).byteValue());
                } else if (value instanceof BByte) {
                    return (BByte) value;
                }
                break;
            case TypeTags.FLOAT_TAG:
                if (value != null) {
                    if (value instanceof Float) {
                        return new BFloat((Float) value);
                    } else if (value instanceof Double) {
                        return new BFloat((Double) value);
                    } else if (value instanceof BFloat) {
                        return (BFloat) value;
                    }
                }
                break;
            case TypeTags.DECIMAL_TAG:
                if (value != null) {
                    if (value instanceof String) {
                        return new BDecimal(new BigDecimal((String) value, MathContext.DECIMAL128));
                    } else if (value instanceof BigDecimal) {
                        return new BDecimal((BigDecimal) value);
                    } else if (value instanceof BDecimal) {
                        return (BDecimal) value;
                    }
                }
                break;
            case TypeTags.STRING_TAG:
                if (value != null) {
                    if (value instanceof String) {
                        return new BString((String) value);
                    } else if (value instanceof BString) {
                        return (BString) value;
                    }
                }
                break;
            case TypeTags.BOOLEAN_TAG:
                if (value != null) {
                    if (value instanceof Boolean) {
                        return new BBoolean((Boolean) value);
                    } else if (value instanceof BBoolean) {
                        return (BBoolean) value;
                    }
                }
                break;
            default:
                if (value != null && (value instanceof BRefType)) {
                    return (BRefType) value;
                }
                break;
        }
        
        return null;
    }
}
