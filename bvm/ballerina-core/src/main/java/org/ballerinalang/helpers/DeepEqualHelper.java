/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.helpers;

import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A helper class for the deep equals implementation.
 */
public class DeepEqualHelper {
    /**
     * Deep equal or strict equal implementation for any value type. I.E ===.
     * @param lhsValue The value on the left side.
     * @param rhsValue The value on the right side.
     * @return True if values are equal, else false.
     */
    public static boolean isDeepEqual(BValue lhsValue, BValue rhsValue) {
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
    
        if (null != lhsValue.getType().getPackagePath() && null != rhsValue.getType().getPackagePath() &&
            !lhsValue.getType().getPackagePath().equals(rhsValue.getType().getPackagePath())) {
            return false;
        }
        
        if (null != lhsValue.getType().getName() && null != rhsValue.getType().getName() &&
            !lhsValue.getType().getName().equals(rhsValue.getType().getName())) {
            return false;
        }
        
        // Handling JSON.
        if (lhsValue instanceof BJSON && rhsValue instanceof BJSON) {
            JsonNode lhsJSON = ((BJSON) lhsValue).value();
            JsonNode rhsJSON = ((BJSON) rhsValue).value();
            return isDeepEqual(lhsJSON, rhsJSON);
        }
        
        switch (lhsValue.getType().getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.TYPE_TAG:
                BRefType lhsRef = (BRefType) lhsValue;
                BRefType rhsRef = (BRefType) rhsValue;
                return lhsRef.value().equals(rhsRef.value());
            case TypeTags.STRUCT_TAG:
                BStructType lhsStructType = (BStructType) lhsValue.getType();
                BStructType rhsStructType = (BStructType) rhsValue.getType();
                if (!Arrays.equals(lhsStructType.getFieldTypeCount(), rhsStructType.getFieldTypeCount())) {
                    return false;
                }
                
                BStruct lhsStruct = (BStruct) lhsValue;
                BStruct rhStruct = (BStruct) rhsValue;
    
                // Checking equality for integer fields.
                for (int i = 0; i < lhsStructType.getFieldTypeCount()[0]; i++) {
                    if (lhsStruct.getIntField(i) != rhStruct.getIntField(i)) {
                        return false;
                    }
                }
    
                // Checking equality for float fields.
                for (int i = 0; i < lhsStructType.getFieldTypeCount()[1]; i++) {
                    if (lhsStruct.getFloatField(i) != rhStruct.getFloatField(i)) {
                        return false;
                    }
                }
    
                // Checking equality for string fields.
                for (int i = 0; i < lhsStructType.getFieldTypeCount()[2]; i++) {
                    if (!lhsStruct.getStringField(i).equals(rhStruct.getStringField(i))) {
                        return false;
                    }
                }
    
                // Checking equality for boolean fields.
                for (int i = 0; i < lhsStructType.getFieldTypeCount()[3]; i++) {
                    if (lhsStruct.getBooleanField(i) != rhStruct.getBooleanField(i)) {
                        return false;
                    }
                }
    
                // Checking equality for byte fields.
                for (int i = 0; i < lhsStructType.getFieldTypeCount()[4]; i++) {
                    if (!Arrays.equals(lhsStruct.getBlobField(i), rhStruct.getBlobField(i))) {
                        return false;
                    }
                }
    
                // Checking equality for refs fields.
                for (int i = 0; i < lhsStructType.getFieldTypeCount()[5]; i++) {
                    if (!isDeepEqual(lhsStruct.getRefField(i), rhStruct.getRefField(i))) {
                        return false;
                    }
                }
                break;
            case TypeTags.MAP_TAG:
                BMap lhsMap = (BMap) lhsValue;
                BMap rhsMap = (BMap) rhsValue;
                // Check if size is same.
                if (lhsMap.size() != rhsMap.size()) {
                    return false;
                }
    
                // Check if key set is equal.
                if (!lhsMap.keySet().containsAll(rhsMap.keySet())) {
                    return false;
                }
    
                List<String> keys = Arrays.stream(lhsMap.keySet().toArray())
                        .map(String.class::cast)
                        .collect(Collectors.toList());
                for (int i = 0; i < lhsMap.size(); i++) {
                    if (!isDeepEqual(lhsMap.get(keys.get(i)), rhsMap.get(keys.get(i)))) {
                        return false;
                    }
                }
                break;
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
                        if (!isDeepEqual(lhsArray.getBValue(i), rhsArray.getBValue(i))) {
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
     * Deep equals function for JSON type values. I.E ===
     * @param lhsJson The left side value.
     * @param rhsJson The right side value.
     * @return True if values are equal, else false.
     */
    private static boolean isDeepEqual(JsonNode lhsJson, JsonNode rhsJson) {
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
                for (String key : lhJsonFields.keySet()) {
                    if (!rhJsonFields.keySet().contains(key)) {
                        return false;
                    }
                    if (!isDeepEqual(lhJsonFields.get(key), rhJsonFields.get(key))) {
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
                if (!isDeepEqual(lhsJson.get(i), rhsJson.get(i))) {
                    return false;
                }
            }
        }
        
        // If it is a value type, then equalize their text values.
        return lhsJson.asText().equals(rhsJson.asText());
    }
}
