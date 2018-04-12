/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.reflect;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        
        // Handling JSON.
        if (lhsValue instanceof BJSON && rhsValue instanceof BJSON) {
            JsonNode lhsJSON = ((BJSON) lhsValue).value();
            JsonNode rhsJSON = ((BJSON) rhsValue).value();
            return isEqual(lhsJSON, rhsJSON);
        }
        
        switch (lhsValue.getType().getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.INT_TAG:
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
            case TypeTags.STRUCT_TAG:
                BStructType lhsStructType = (BStructType) lhsValue.getType();
                BStructType rhsStructType = (BStructType) rhsValue.getType();
                if (!Arrays.equals(lhsStructType.getFieldTypeCount(), rhsStructType.getFieldTypeCount())) {
                    return false;
                }
    
                return isEqual((BStruct) lhsValue, (BStruct) rhsValue, lhsStructType);
            case TypeTags.MAP_TAG:
                return isEqual((BMap) lhsValue, (BMap) rhsValue);
            case TypeTags.ARRAY_TAG:
            case TypeTags.ANY_TAG:
                // TODO: This block should ideally be in the ARRAY_TAG case only. Not ANY_TAG. #4505.
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
    private boolean isEqual(BStruct lhsStruct, BStruct rhsStruct, BStructType structType) {
        // Checking equality for integer fields.
        for (int i = 0; i < structType.getFieldTypeCount()[0]; i++) {
            if (lhsStruct.getIntField(i) != rhsStruct.getIntField(i)) {
                return false;
            }
        }
        
        // Checking equality for float fields.
        for (int i = 0; i < structType.getFieldTypeCount()[1]; i++) {
            if (Double.compare(lhsStruct.getFloatField(i), rhsStruct.getFloatField(i)) != 0) {
                return false;
            }
        }
        
        // Checking equality for string fields.
        for (int i = 0; i < structType.getFieldTypeCount()[2]; i++) {
            if (!lhsStruct.getStringField(i).equals(rhsStruct.getStringField(i))) {
                return false;
            }
        }
        
        // Checking equality for boolean fields.
        for (int i = 0; i < structType.getFieldTypeCount()[3]; i++) {
            if (lhsStruct.getBooleanField(i) != rhsStruct.getBooleanField(i)) {
                return false;
            }
        }
        
        // Checking equality for byte fields.
        for (int i = 0; i < structType.getFieldTypeCount()[4]; i++) {
            if (!Arrays.equals(lhsStruct.getBlobField(i), rhsStruct.getBlobField(i))) {
                return false;
            }
        }
        
        // Checking equality for refs fields.
        for (int i = 0; i < structType.getFieldTypeCount()[5]; i++) {
            if (!isEqual(lhsStruct.getRefField(i), rhsStruct.getRefField(i))) {
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
        if (!lhsMap.keySet().containsAll(rhsMap.keySet())) {
            return false;
        }
        
        List<String> keys = Arrays.stream(lhsMap.keySet().toArray()).map(String.class::cast).collect
                (Collectors.toList());
        for (int i = 0; i < lhsMap.size(); i++) {
            if (!isEqual(lhsMap.get(keys.get(i)), rhsMap.get(keys.get(i)))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Deep equals function for JSON type values.
     *
     * @param lhsJson The left side value.
     * @param rhsJson The right side value.
     * @return True if values are equal, else false.
     */
    private boolean isEqual(JsonNode lhsJson, JsonNode rhsJson) {
        // JsonNode can become null
        if (lhsJson == null && rhsJson == null) {
            return true;
        }
        if (lhsJson == null || rhsJson == null) {
            return false;
        }
        if (lhsJson.getType() == JsonNode.Type.OBJECT) {
            // Converting iterators to maps as iterators are ordered.
            Iterator<Map.Entry<String, JsonNode>> lhJsonFieldsIterator = lhsJson.fields();
            HashMap<String, JsonNode> lhJsonFields = new HashMap<>();
            while (lhJsonFieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> field = lhJsonFieldsIterator.next();
                lhJsonFields.put(field.getKey(), field.getValue());
            }
            
            Iterator<Map.Entry<String, JsonNode>> rhJsonFieldsIterator = rhsJson.fields();
            HashMap<String, JsonNode> rhJsonFields = new HashMap<>();
            while (rhJsonFieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> field = rhJsonFieldsIterator.next();
                rhJsonFields.put(field.getKey(), field.getValue());
            }
            
            // Size of the fields should be same.
            if (lhJsonFields.size() != rhJsonFields.size()) {
                return false;
            }
    
            if (lhJsonFields.size() > 0) {
                for (Map.Entry<String, JsonNode> entry : lhJsonFields.entrySet()) {
                    if (!rhJsonFields.keySet().contains(entry.getKey()) || !isEqual(entry.getValue(),
                            rhJsonFields.get(entry.getKey()))) {
                        return false;
                    }
                }
            }
        }
        
        if (lhsJson.getType() == JsonNode.Type.ARRAY) {
            if (lhsJson.size() != rhsJson.size()) {
                return false;
            }
            
            for (int i = 0; i < lhsJson.size(); i++) {
                if (!isEqual(lhsJson.get(i), rhsJson.get(i))) {
                    return false;
                }
            }
        }
        
        // If it is a value type, then equalize their text values.
        return lhsJson.asText().equals(rhsJson.asText());
    }
}


