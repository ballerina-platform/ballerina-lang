/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.reflect.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The ballerina.reflect:equals function which checks equality among 2 values. Currently supports string, int, float,
 * boolean, type, structs, maps, arrays, any, JSON.
 */
@BallerinaFunction(orgName = "ballerina", packageName = "reflect",
                   functionName = "equals",
                   args = {@Argument(name = "value1", type = TypeKind.ANY),
                           @Argument(name = "value2", type = TypeKind.ANY)},
                   returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
                   isPublic = true)
public class Equals extends BlockingNativeCallableUnit {
    
    @Override
    public void execute(Context context) {
        BValue value1 = context.getNullableRefArgument(0);
        BValue value2 = context.getNullableRefArgument(1);
        context.setReturnValues(new BBoolean(isEqual(value1, value2)));
    }
    
    /**
     * Deep equal or strict equal implementation for any value type.
     *
     * @param lhsValue The value on the left side.
     * @param rhsValue The value on the right side.
     * @return True if values are equal, else false.
     */
    @SuppressWarnings("rawtypes")
    private boolean isEqual(BValue lhsValue, BValue rhsValue) {
        if (null == lhsValue && null == rhsValue) {
            return true;
        }
        
        if (null == lhsValue || null == rhsValue) {
            return false;
        }

        if (TypeTags.INT_TAG == lhsValue.getType().getTag() && TypeTags.BYTE_TAG == rhsValue.getType().getTag()) {
            BInteger bInteger = (BInteger) lhsValue;
            BByte bByte = (BByte) rhsValue;
            return bInteger.intValue() == bByte.intValue();
        }

        if (TypeTags.BYTE_TAG == lhsValue.getType().getTag() && TypeTags.INT_TAG == rhsValue.getType().getTag()) {
            BByte bByte = (BByte) lhsValue;
            BInteger bInteger = (BInteger) rhsValue;
            return bInteger.intValue() == bByte.intValue();
        }
        
        // Required for any == any.
        if (lhsValue.getType().getTag() != rhsValue.getType().getTag()) {
            return false;
        }
        
        if (null != lhsValue.getType().getPackagePath() && null != rhsValue.getType().getPackagePath() && !lhsValue
                .getType().getPackagePath().equals(rhsValue.getType().getPackagePath())) {
            return false;
        }
        
        if (null != lhsValue.getType().getName() && null != rhsValue.getType().getName() && !lhsValue.getType()
                .getName().equals(rhsValue.getType().getName())) {
            return false;
        }
        
        switch (lhsValue.getType().getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.TYPEDESC_TAG:
                BRefType lhsRef = (BRefType) lhsValue;
                BRefType rhsRef = (BRefType) rhsValue;
                if (null == lhsRef.value() && null == rhsRef.value()) {
                    return true;
                }
    
                if (null == lhsRef.value() || null == rhsRef.value()) {
                    return false;
                }
                
                return lhsRef.value().equals(rhsRef.value());
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                BStructureType lhsStructType = (BStructureType) lhsValue.getType();
                BStructureType rhsStructType = (BStructureType) rhsValue.getType();
                if (!Arrays.equals(lhsStructType.getFieldTypeCount(), rhsStructType.getFieldTypeCount())) {
                    return false;
                }
    
                return isEqual((BMap<String, BValue>) lhsValue, (BMap<String, BValue>) rhsValue, lhsStructType);
            case TypeTags.MAP_TAG:
            case TypeTags.JSON_TAG:
                return isEqual((BMap) lhsValue, (BMap) rhsValue);
            case TypeTags.ARRAY_TAG:
            case TypeTags.ANY_TAG:
            case TypeTags.TUPLE_TAG:
                // TODO: This block should ideally be in the ARRAY_TAG, TUPLE_TAG cases only. Not ANY_TAG. #4505.
                if (lhsValue instanceof BNewArray) {
                    BNewArray lhsArray = (BNewArray) lhsValue;
                    BNewArray rhsArray = (BNewArray) rhsValue;
                    if (lhsArray.size() != rhsArray.size()) {
                        return false;
                    }
                    
                    for (int i = 0; i < lhsArray.size(); i++) {
                        if (!isEqual(lhsArray.getBValue(i), rhsArray.getBValue(i))) {
                            return false;
                        }
                    }
                }
                break;
            case TypeTags.XML_TAG:
                // TODO: https://www.pixelstech.net/article/1453272563-Canonicalize-XML-in-Java
                break;
            default:
                return false;
        }
        
        return true;
    }
    
    /**
     * Check if Structs are deeply equals.
     * @param lhsStruct Left hand side struct value.
     * @param rhsStruct Rigth hand side struct value.
     * @param structType Struct type.
     * @return True if deeply equals, else false.
     */
    private boolean isEqual(BMap<String, BValue> lhsStruct, BMap<String, BValue> rhsStruct, BStructureType structType) {
        for (String fieldName : structType.getFields().keySet()) {
            if (!isEqual(lhsStruct.get(fieldName), rhsStruct.get(fieldName))) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Check if a map is deeply equals.
     * @param lhsMap Left hand side map.
     * @param rhsMap Right hand side map.
     * @return Return if maps are deeply equal.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean isEqual(BMap lhsMap, BMap rhsMap) {
        // Check if size is same.
        if (lhsMap.size() != rhsMap.size()) {
            return false;
        }
        
        // Check if key set is equal.
        if (!lhsMap.getMap().keySet().containsAll(rhsMap.getMap().keySet())) {
            return false;
        }

        List<String> keys = Arrays.stream(lhsMap.keys()).map(String.class::cast).collect(Collectors.toList());
        for (int i = 0; i < lhsMap.size(); i++) {
            if (!isEqual(lhsMap.get(keys.get(i)), rhsMap.get(keys.get(i)))) {
                return false;
            }
        }
        return true;
    }
}
