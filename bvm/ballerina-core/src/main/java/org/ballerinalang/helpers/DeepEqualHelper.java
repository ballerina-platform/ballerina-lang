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
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BEnumerator;
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
     * @param lhVal The value on the left side.
     * @param rhVal The value on the right side.
     * @return True if values are equal, else false.
     */
    public static boolean isDeepEqual(BValue lhVal, BValue rhVal) {
        if (null == lhVal && null == rhVal) {
            return true;
        }
        
        if (null == lhVal || null == rhVal) {
            return false;
        }
        
        // Required for any == any.
        if (lhVal.getType().getTag() != rhVal.getType().getTag()) {
            return false;
        }
    
        if (null != lhVal.getType().getPackagePath() && null != rhVal.getType().getPackagePath() &&
            !lhVal.getType().getPackagePath().equals(rhVal.getType().getPackagePath())) {
            return false;
        }
        
        if (null != lhVal.getType().getName() && null != rhVal.getType().getName() &&
            !lhVal.getType().getName().equals(rhVal.getType().getName())) {
            return false;
        }
        
        switch (lhVal.getType().getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.TYPE_TAG:
                BRefType lhRef = (BRefType) lhVal;
                BRefType rhRef = (BRefType) rhVal;
                if (lhRef instanceof BJSON) {
                    JsonNode lhJSON = ((BJSON) lhVal).value();
                    JsonNode rhJSON = ((BJSON) rhVal).value();
                    return isDeepEqual(lhJSON, rhJSON);
                } else {
                    return lhRef.value().equals(rhRef.value());
                }
            case TypeTags.STRUCT_TAG:
                BStructType lhStructType = (BStructType) lhVal.getType();
                BStructType rhStructType = (BStructType) rhVal.getType();
                if (!Arrays.equals(lhStructType.getFieldTypeCount(), rhStructType.getFieldTypeCount())) {
                    return false;
                }
                
                BStruct lhStruct = (BStruct) lhVal;
                BStruct rhStruct = (BStruct) rhVal;
    
                // Checking equality for integer fields.
                for (int i = 0; i < lhStructType.getFieldTypeCount()[0]; i++) {
                    if (lhStruct.getIntField(i) != rhStruct.getIntField(i)) {
                        return false;
                    }
                }
    
                // Checking equality for float fields.
                for (int i = 0; i < lhStructType.getFieldTypeCount()[1]; i++) {
                    if (lhStruct.getFloatField(i) != rhStruct.getFloatField(i)) {
                        return false;
                    }
                }
    
                // Checking equality for string fields.
                for (int i = 0; i < lhStructType.getFieldTypeCount()[2]; i++) {
                    if (!lhStruct.getStringField(i).equals(rhStruct.getStringField(i))) {
                        return false;
                    }
                }
    
                // Checking equality for boolean fields.
                for (int i = 0; i < lhStructType.getFieldTypeCount()[3]; i++) {
                    if (lhStruct.getBooleanField(i) != rhStruct.getBooleanField(i)) {
                        return false;
                    }
                }
    
                // Checking equality for byte fields.
                for (int i = 0; i < lhStructType.getFieldTypeCount()[4]; i++) {
                    if (!Arrays.equals(lhStruct.getBlobField(i), rhStruct.getBlobField(i))) {
                        return false;
                    }
                }
    
                // Checking equality for refs fields.
                for (int i = 0; i < lhStructType.getFieldTypeCount()[5]; i++) {
                    if (!isDeepEqual(lhStruct.getRefField(i), rhStruct.getRefField(i))) {
                        return false;
                    }
                }
                break;
            case TypeTags.MAP_TAG:
                BMap lhMap = (BMap) lhVal;
                BMap rhMap = (BMap) rhVal;
                // Check if size is same.
                if (lhMap.size() != rhMap.size()) {
                    return false;
                }
    
                // Check if key set is equal.
                if (!lhMap.keySet().containsAll(rhMap.keySet())) {
                    return false;
                }
    
                List<String> keys = Arrays.stream(lhMap.keySet().toArray())
                        .map(String.class::cast)
                        .collect(Collectors.toList());
                for (int i = 0; i < lhMap.size(); i++) {
                    if (!isDeepEqual(lhMap.get(keys.get(i)), rhMap.get(keys.get(i)))) {
                        return false;
                    }
                }
                break;
            case TypeTags.ARRAY_TAG:
            case TypeTags.ANY_TAG:
                // TODO: This block should ideally be in the ARRAY_TAG case only. Not ANY_TAG. #4505.
                if (lhVal instanceof BNewArray) {
                    BNewArray lhArray = (BNewArray) lhVal;
                    BNewArray rhArray = (BNewArray) rhVal;
                    if (lhArray.size() != rhArray.size()) {
                        return false;
                    }
    
                    for (int i = 0; i < lhArray.size(); i++) {
                        if (!isDeepEqual(lhArray.getBValue(i), rhArray.getBValue(i))) {
                            return false;
                        }
                    }
                }
                break;
            case TypeTags.JSON_TAG:
                JsonNode lhJSON = ((BJSON) lhVal).value();
                JsonNode rhJSON = ((BJSON) rhVal).value();
    
                return isDeepEqual(lhJSON, rhJSON);
            case TypeTags.XML_TAG:
                // https://www.pixelstech.net/article/1453272563-Canonicalize-XML-in-Java
                break;
            case TypeTags.CONNECTOR_TAG:
                BConnector lhConnector = (BConnector) lhVal;
                BConnector rhConnector = (BConnector) rhVal;
                
                if (!lhConnector.getConnectorType().getPackagePath().equals(
                        rhConnector.getConnectorType().getPackagePath())) {
                    return false;
                }
    
                if (!lhConnector.getConnectorType().getName().equals(rhConnector.getConnectorType().getName())) {
                    return false;
                }
                break;
            case TypeTags.ENUM_TAG:
                BEnumerator lhEnum = (BEnumerator) lhVal;
                BEnumerator rhEnum = (BEnumerator) rhVal;
                
                if (!lhEnum.stringValue().equals(rhEnum.stringValue())) {
                    return false;
                }
                break;
            default:
                return false;
        }
        
        return true;
    }
    
    /**
     * Deep equals function for JSON type values. I.E ===
     * @param lhJson The left side value.
     * @param rhJson The right side value.
     * @return True if values are equal, else false.
     */
    private static boolean isDeepEqual(JsonNode lhJson, JsonNode rhJson) {
        if (lhJson.getType() == JsonNode.Type.OBJECT) {
            // Converting iterators to maps as iterators are ordered.
            Iterator<Map.Entry<String, JsonNode>> lhJsonFieldsIterator = lhJson.fields();
            HashMap<String, JsonNode> lhJsonFields = new HashMap<>();
            while (lhJsonFieldsIterator.hasNext()) {
                Map.Entry<String, JsonNode> field = lhJsonFieldsIterator.next();
                lhJsonFields.put(field.getKey(), field.getValue());
            }
    
            Iterator<Map.Entry<String, JsonNode>> rhJsonFieldsIterator = rhJson.fields();
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
        
        if (lhJson.getType() == JsonNode.Type.ARRAY) {
            if (lhJson.size() != rhJson.size()) {
                return false;
            }
            
            for (int i = 0; i < lhJson.size(); i++) {
                if (!isDeepEqual(lhJson.get(i), rhJson.get(i))) {
                    return false;
                }
            }
        }
        
        // If it is a value type, then equalize their text values.
        return lhJson.asText().equals(rhJson.asText());
    }
}
