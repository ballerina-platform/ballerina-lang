/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.model.util;

import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.ballerinalang.model.DataTableJSONDataSource;
import org.ballerinalang.model.types.BAnyType;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructFieldInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.DefaultValueAttributeInfo;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Common utility methods used for JSON manipulation.
 * 
 * @since 0.87
 */
public class JSONUtils {

    private static final String NULL = "null";
    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSI_PREFIX = "xsi";
    private static final String NIL = "nil";

    /**
     * Check whether JSON has particular field.
     *
     * @param json JSON to be considered.
     * @param elementName String name json field to be considered.
     * @return Boolean 'true' if JSON has given field.
     */
    public static boolean hasElement(BJSON json, String elementName) {
        JsonNode jsonNode = json.value();
        return jsonNode.has(elementName);
    }

    /**
     * Convert {@link BMap} to {@link BJSON}.
     *
     * @param map {@link BMap} to be converted to {@link BJSON}
     * @return JSON representation of the provided map
     */
    public static BJSON convertMapToJSON(BMap<String, BValue> map) {
        Set<String> keys = map.keySet();
        BJSON bjson = new BJSON("{}");
        ObjectNode jsonNode = (ObjectNode) bjson.value();
        for (String key : keys) {
            try {
                BValue bvalue = map.get(key);
                if (bvalue == null) {
                    jsonNode.set(key, new BJSON(NULL).value());
                } else if (bvalue.getType() == BTypes.typeString) {
                    jsonNode.put(key, bvalue.stringValue());
                } else if (bvalue.getType() == BTypes.typeInt) {
                    jsonNode.put(key, ((BInteger) bvalue).intValue());
                } else if (bvalue.getType() == BTypes.typeFloat) {
                    jsonNode.put(key, ((BFloat) bvalue).floatValue());
                } else if (bvalue.getType() == BTypes.typeBoolean) {
                    jsonNode.put(key, ((BBoolean) bvalue).booleanValue());
                } else if (bvalue.getType() == BTypes.typeMap) {
                    jsonNode.set(key, convertMapToJSON((BMap<String, BValue>) bvalue).value());
                } else if (bvalue.getType() == BTypes.typeJSON) {
                    jsonNode.set(key, ((BJSON) bvalue).value());
                } else if (bvalue instanceof BNewArray) {
                    jsonNode.set(key, convertArrayToJSON((BNewArray) bvalue).value());
                } else if (bvalue instanceof BStruct) {
                    jsonNode.set(key, convertStructToJSON((BStruct) bvalue).value());
                } else {
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                            BTypes.typeJSON, bvalue.getType());
                }
            } catch (BallerinaException e) {
                handleError(e, key);
            }
        }
        return bjson;
    }

    /**
     * Convert {@link BIntArray} to {@link BJSON}.
     *
     * @param intArray {@link BIntArray} to be converted to {@link BJSON}
     * @return JSON representation of the provided intArray
     */
    public static BJSON convertArrayToJSON(BIntArray intArray) {
        BJSON bjson = new BJSON("[]");
        ArrayNode arrayNode = (ArrayNode) bjson.value();
        for (int i = 0; i < intArray.size(); i++) {
            long value = intArray.get(i);
            arrayNode.add(value);
        }
        return bjson;
    }

    /**
     * Convert {@link BFloatArray} to {@link BJSON}.
     *
     * @param floatArray {@link BFloatArray} to be converted to {@link BJSON}
     * @return JSON representation of the provided floatArray
     */
    public static BJSON convertArrayToJSON(BFloatArray floatArray) {
        BJSON bjson = new BJSON("[]");
        ArrayNode arrayNode = (ArrayNode) bjson.value();
        for (int i = 0; i < floatArray.size(); i++) {
            double value = floatArray.get(i);
            arrayNode.add(value);
        }
        return bjson;
    }

    /**
     * Convert {@link BStringArray} to {@link BJSON}.
     *
     * @param stringArray {@link BStringArray} to be converted to {@link BJSON}
     * @return JSON representation of the provided stringArray
     */
    public static BJSON convertArrayToJSON(BStringArray stringArray) {
        BJSON bjson = new BJSON("[]");
        ArrayNode arrayNode = (ArrayNode) bjson.value();
        for (int i = 0; i < stringArray.size(); i++) {
            String value = stringArray.get(i);
            arrayNode.add(value);
        }
        return bjson;
    }

    /**
     * Convert {@link BBooleanArray} to {@link BJSON}.
     *
     * @param booleanArray {@link BBooleanArray} to be converted to {@link BJSON}
     * @return JSON representation of the provided booleanArray
     */
    public static BJSON convertArrayToJSON(BBooleanArray booleanArray) {
        BJSON bjson = new BJSON("[]");
        ArrayNode arrayNode = (ArrayNode) bjson.value();
        for (int i = 0; i < booleanArray.size(); i++) {
            int value = booleanArray.get(i);
            arrayNode.add(value == 1);
        }
        return bjson;
    }

    /**
     * Convert {@link BNewArray} to {@link BJSON}.
     *
     * @param bArray {@link BNewArray} to be converted to {@link BJSON}
     * @return JSON representation of the provided bArray
     */
    public static BJSON convertArrayToJSON(BNewArray bArray) {
        if (bArray instanceof BIntArray) {
            return convertArrayToJSON((BIntArray) bArray);
        } else if (bArray instanceof BFloatArray) {
            return convertArrayToJSON((BFloatArray) bArray);
        } else if (bArray instanceof BStringArray) {
            return convertArrayToJSON((BStringArray) bArray);
        } else if (bArray instanceof BBooleanArray) {
            return convertArrayToJSON((BBooleanArray) bArray);
        } else if (bArray instanceof BRefValueArray) {
            return convertArrayToJSON((BRefValueArray) bArray);
        }

        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                BTypes.typeJSON, bArray.getType());
    }

    /**
     * Convert {@link BRefValueArray} to {@link BJSON}.
     *
     * @param refValueArray {@link BRefValueArray} to be converted to {@link BJSON}
     * @return JSON representation of the provided refValueArray
     */
    public static BJSON convertArrayToJSON(BRefValueArray refValueArray) {
        BJSON bjson = new BJSON("[]");
        ArrayNode arrayNode = (ArrayNode) bjson.value();
        for (int i = 0; i < refValueArray.size(); i++) {
            BRefType value = refValueArray.get(i);
            if (value == null) {
                arrayNode.add(new BJSON(NULL).value());
            } else if (value.getType() == BTypes.typeMap) {
                arrayNode.add(convertMapToJSON((BMap<String, BValue>) value).value());
            } else if (value.getType() == BTypes.typeJSON) {
                arrayNode.add(((BJSON) value).value());
            } else if (value instanceof BStruct) {
                arrayNode.add(convertStructToJSON((BStruct) value).value());
            } else if (value instanceof BNewArray) {
                arrayNode.add(convertArrayToJSON((BNewArray) value).value());
            } else {
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                        BTypes.typeJSON, value.getType());
            }
        }
        return bjson;
    }

    /**
     * Convert {@link BStruct} to {@link BJSON}.
     *
     * @param struct {@link BStruct} to be converted to {@link BJSON}
     * @return JSON representation of the provided array
     */
    public static BJSON convertStructToJSON(BStruct struct) {
        BJSON bjson = new BJSON("{}");
        ObjectNode jsonNode = (ObjectNode) bjson.value();
        BStructType structType = (BStructType) struct.getType();

        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;
        for (BStructType.StructField structField : structType.getStructFields()) {
            String key = structField.getFieldName();
            BType fieldType = structField.getFieldType();
            try {
                switch (fieldType.getTag()) {
                    case TypeTags.INT_TAG:
                        jsonNode.put(key, struct.getIntField(++longRegIndex));
                        break;
                    case TypeTags.FLOAT_TAG:
                        jsonNode.put(key, struct.getFloatField(++doubleRegIndex));
                        break;
                    case TypeTags.STRING_TAG:
                        jsonNode.put(key, struct.getStringField(++stringRegIndex));
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        jsonNode.put(key, struct.getBooleanField(++booleanRegIndex) == 1);
                        break;
                    default:
                        BValue value = struct.getRefField(++refRegIndex);
                        if (value == null) {
                            jsonNode.set(key, new BJSON(NULL).value());
                        } else if (value.getType() == BTypes.typeMap) {
                            jsonNode.set(key, convertMapToJSON((BMap<String, BValue>) value).value());
                        } else if (value.getType() == BTypes.typeJSON) {
                            jsonNode.set(key, ((BJSON) value).value());
                        } else if (value instanceof BNewArray) {
                            jsonNode.set(key, convertArrayToJSON((BNewArray) value).value());
                        } else if (value instanceof BStruct) {
                            jsonNode.set(key, convertStructToJSON((BStruct) value).value());
                        } else {
                            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                                    BTypes.typeJSON, value.getType());
                        }
                }
            } catch (BallerinaException e) {
                handleError(e, key);
            }
        }

        return bjson;
    }

    /**
     * Convert {@link BDataTable} to {@link BJSON}.
     *
     * @param dataTable {@link BDataTable} to be converted to {@link BJSON}
     * @param isInTransaction   Within a transaction or not
     * @return JSON representation of the provided datatable
     */
    public static BJSON toJSON(BDataTable dataTable, boolean isInTransaction) {
        return new BJSON(new DataTableJSONDataSource(dataTable, isInTransaction));
    }
    
    /**
     * Get an element from a JSON.
     * 
     * @param json JSON object to get the element from
     * @param elementName Name of the element to be retrieved
     * @return Element of JSON having the provided name
     */
    public static BJSON getElement(BJSON json, String elementName) {
        JsonNode jsonNode = json.value();
        
        if (!jsonNode.isObject()) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CANNOT_GET_VALUE_INCOMPATIBLE_TYPES,
                    elementName, getComplexObjectTypeName(JsonNodeType.OBJECT), getTypeName(jsonNode));
        }
        
        try {
            JsonNode element = jsonNode.get(elementName);
            if (element == null || element.isNull()) {
                return null;
            }
            return new BJSON(element);
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_GET_ERROR, t.getMessage());
        }
    }
    
    /**
     * Set an element in a JSON. If an element with the given name already exists,
     * this method will update the existing element. Otherwise, a new element with
     * the given name will be added.
     * 
     * @param json JSON object to set the element
     * @param elementName Name of the element to be set
     * @param element json element
     */
    public static void setElement(BJSON json, String elementName, BJSON element) {
        if (json == null) {
            return;
        }

        JsonNode jsonNode = json.value();
        JsonNode jsonElement = element == null ? null : element.value();

        if (!jsonNode.isObject()) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CANNOT_SET_VALUE_INCOMPATIBLE_TYPES,
                    elementName, getComplexObjectTypeName(JsonNodeType.OBJECT), getTypeName(jsonNode));
        }

        try {
            ((ObjectNode) jsonNode).set(elementName, jsonElement);
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_SET_ERROR, t.getMessage());
        }
    }

    /**
     * Check whether provided JSON object is a JSON Array.
     *
     * @param json JSON to execute array condition.
     * @return returns true if provided JSON is a JSON Array.
     */
    public static boolean isJSONArray(BJSON json) {
        if (json == null) {
            return false;
        }
        JsonNode jsonNode = json.value();
        return jsonNode.isArray();
    }

    /**
     * Returns the size of JSON Array.
     *
     * @param json JSON to calculate array size.
     * @return returns integer that represents size of JSON Array.
     */
    public static int getJSONArrayLength(BJSON json) {
        if (json == null) {
            return -1;
        }
        JsonNode jsonNode = json.value();
        return jsonNode.size();
    }

    /**
     * Get an element from a JSON array.
     * 
     * @param json JSON array to get the element from
     * @param index Index of the element needed
     * @return Element at the given index, if the provided JSON is an array. Error, otherwise. 
     */
    public static BJSON getArrayElement(BJSON json, long index) {
        JsonNode jsonNode = json.value();

        if (!jsonNode.isArray()) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CANNOT_GET_VALUE_INCOMPATIBLE_TYPES,
                    index, getComplexObjectTypeName(JsonNodeType.ARRAY), getTypeName(jsonNode));
        }

        try {
            if (jsonNode.size() <= index) {
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.ARRAY_INDEX_OUT_OF_RANGE,
                    index, jsonNode.size());
            }
            JsonNode element = jsonNode.get((int) index);
            if (element == null || element.isNull()) {
                return null;
            }
            return new BJSON(element);
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_GET_ERROR, t.getMessage());
        }
    }
    
    /**
     * Set an element in the given position of a JSON array. This method will update the existing value.
     * 
     * @param json JSON array to set the element
     * @param index Index of the element to be set
     * @param element Element to be set
     */
    public static void setArrayElement(BJSON json, long index, BJSON element) {
        if (json == null) {
            return;
        }

        JsonNode jsonNode = json.value();

        if (!jsonNode.isArray()) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CANNOT_SET_VALUE_INCOMPATIBLE_TYPES, index,
                    getComplexObjectTypeName(JsonNodeType.ARRAY), getTypeName(jsonNode));
        }

        JsonNode jsonElement = element == null ? null : element.value();
        ArrayNode arrayNode = ((ArrayNode) jsonNode);
        try {
            if (arrayNode.size() <= index) {
                // auto-grow the array
                for (int i = arrayNode.size(); i < index; i++) {
                    arrayNode.add(NullNode.getInstance());
                }
                arrayNode.add(jsonElement);
            } else {
                arrayNode.set((int) index, jsonElement);
            }
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_SET_ERROR, t.getMessage());
        }
    }

    /**
     * Converts given json object to the corresponding xml.
     *
     * @param json JSON object to get the corresponding xml
     * @return BXML XML representation of the given json object
     */
    public static BXML convertToXML(BJSON json, String attributePrefix, String arrayEntryTag) {
        try {
            BXML xml;
            JsonNode jsonNode = json.value();
            ArrayList<BXML> omElementArrayList = traverseTree(jsonNode, attributePrefix, arrayEntryTag);
            if (omElementArrayList.size() == 1) {
                xml = omElementArrayList.get(0);
            } else {
                //There is a multi rooted node and create xml sequence from it
                BRefValueArray elementsSeq = new BRefValueArray();
                int count = omElementArrayList.size();
                for (int i = 0; i < count; i++) {
                    elementsSeq.add(i, omElementArrayList.get(i));
                }
                xml = new BXMLSequence(elementsSeq);
            }
            return xml;
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_SET_ERROR, t.getMessage());
        }
    }

    /**
     * Traverse a JSON root node and produces the corresponding xml items.
     *
     * @param node {@link JsonNode} to be traversed
     * @param attributePrefix String prefix used for attributes
     * @param arrayEntryTag String used as the tag in the arrays
     * @return List of xml items genereated during the traversal.
     */
    private static ArrayList<BXML> traverseTree(JsonNode node, String attributePrefix, String arrayEntryTag)
            throws Exception {
        ArrayList<BXML> xmlArray = new ArrayList<>();
        if (node.isValueNode()) {
            BXML xml = XMLUtils.parse(node.asText());
            xmlArray.add(xml);
        } else {
            traverseJsonNode(node, null, null, xmlArray, attributePrefix, arrayEntryTag);
        }
        return xmlArray;
    }

    /**
     * Traverse a JSON node ad produces the corresponding xml items.
     *
     * @param node {@link JsonNode} to be traversed
     * @param nodeName name of the current traversing node
     * @param parentElement parent element of the current node
     * @param omElementArrayList List of xml iterms generated
     * @param attributePrefix String prefix used for attributes
     * @param arrayEntryTag String used as the tag in the arrays
     * @return List of xml items genereated during the traversal.
     */
    private static OMElement traverseJsonNode(JsonNode node, String nodeName, OMElement parentElement,
            ArrayList<BXML> omElementArrayList, String attributePrefix, String arrayEntryTag) throws Exception {
        OMElement currentRoot = null;
        boolean processNode = true;
        if (nodeName != null) {
            currentRoot = OM_FACTORY.createOMElement(nodeName, null);
            //Extract attributes and set to the immidiate parent.
            if (nodeName.startsWith(attributePrefix)) {
                if (!node.isValueNode()) {
                    throw new BallerinaException("attribute cannot be an object or array");
                }
                if (parentElement != null) {
                    String attributeKey = nodeName.substring(1);
                    parentElement.addAttribute(attributeKey, node.asText(), null);
                    processNode = false;
                }
            }
        }
        if (node.isObject()) {
            Iterator<Entry<String, JsonNode>> nodeIterator = node.fields();
            while (nodeIterator.hasNext()) {
                Entry<String, JsonNode> nodeEntry = nodeIterator.next();
                JsonNode objectNode = nodeEntry.getValue();
                currentRoot = traverseJsonNode(objectNode, nodeEntry.getKey(), currentRoot, omElementArrayList,
                        attributePrefix, arrayEntryTag);
                if (nodeName == null) { //Outermost object
                    omElementArrayList.add(new BXMLItem(currentRoot));
                    currentRoot = null;
                }
            }
        } else if (node.isArray()) {
            Iterator<JsonNode> arrayItemsIterator = node.elements();
            while (arrayItemsIterator.hasNext()) {
                JsonNode arrayNode = arrayItemsIterator.next();
                currentRoot = traverseJsonNode(arrayNode, arrayEntryTag, currentRoot, omElementArrayList,
                        attributePrefix, arrayEntryTag);
                if (nodeName == null) { //Outermost array
                    omElementArrayList.add(new BXMLItem(currentRoot));
                    currentRoot = null;
                }
            }
        } else if (node.isValueNode() && currentRoot != null) {
            if (node.isNull()) {
                OMNamespace xsiNameSpace = OM_FACTORY.createOMNamespace(XSI_NAMESPACE, XSI_PREFIX);
                currentRoot.addAttribute(NIL, "true", xsiNameSpace);
            } else {
                OMText txt1 = OM_FACTORY.createOMText(currentRoot, node.asText());
                currentRoot.addChild(txt1);
            }
        } else {
            throw new BallerinaException("error in converting json to xml");
        }
        //Set the current constructed root the parent element
        if (parentElement != null) {
            if (processNode) {
                parentElement.addChild(currentRoot);
            }
            currentRoot = parentElement;
        }
        return currentRoot;
    }

    /**
     * Convert {@link JsonNode} to int.
     *
     * @param jsonNode {@link JsonNode} to be converted
     * @return BInteger value of the JSON, if its a integer or a long JSON node. Error, otherwise.
     */
    private static long jsonNodeToInt(JsonNode jsonNode) {
        if (jsonNode.isInt() || jsonNode.isLong()) {
            return jsonNode.longValue();
        }

        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                BTypes.typeInt, getTypeName(jsonNode));
    }

    /**
     * Convert {@link JsonNode} to float.
     *
     * @param jsonNode {@link JsonNode} to be converted
     * @return BFloat value of the JSON, if its a double or a float JSON node. Error, otherwise.
     */
    private static double jsonNodeToFloat(JsonNode jsonNode) {
        if (jsonNode.isFloat() || jsonNode.isDouble()) {
            return jsonNode.doubleValue();
        }

        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                BTypes.typeFloat, getTypeName(jsonNode));
    }

    /**
     * Convert {@link JsonNode} to boolean.
     *
     * @param jsonNode {@link JsonNode} to be converted
     * @return BBoolean value of the JSON, if its a boolean node. Error, otherwise.
     */
    private static boolean jsonNodeToBool(JsonNode jsonNode) {
        if (jsonNode.isBoolean()) {
            return jsonNode.booleanValue();
        }

        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                BTypes.typeBoolean, getTypeName(jsonNode));
    }

    /**
     * Convert a JSON node to a map.
     *
     * @param jsonNode JSON to convert
     * @return If the provided JSON is of object-type, this method will return a {@link BMap} containing the values
     * of the JSON object. Otherwise a {@link BallerinaException} will be thrown.
     */
    private static BMap<String, ?> jsonNodeToBMap(JsonNode jsonNode) {
        BMap<String, BValue> map = BTypes.typeMap.getEmptyValue();
        if (!jsonNode.isObject()) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                    getComplexObjectTypeName(JsonNodeType.OBJECT), getTypeName(jsonNode));
        }

        Iterator<Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Entry<String, JsonNode> field = fields.next();
            map.put(field.getKey(), getBValue(field.getValue()));
        }
        return map;
    }

    /**
     * Convert a BJSON to a user defined struct.
     *
     * @param bjson      JSON to convert
     * @param structType Type (definition) of the target struct
     * @return If the provided JSON is of object-type, this method will return a {@link BStruct} containing the values
     * of the JSON object. Otherwise the method will throw a {@link BallerinaException}.
     */
    public static BStruct convertJSONToStruct(BJSON bjson, BStructType structType, PackageInfo pkgInfo) {
        return convertJSONNodeToStruct(bjson.value(), structType, pkgInfo);
    }

    /**
     * Convert a BJSON to a user defined struct.
     *
     * @param jsonNode   JSON to convert
     * @param structType Type (definition) of the target struct
     * @param pkgInfo 
     * @return If the provided JSON is of object-type, this method will return a {@link BStruct} containing the values
     * of the JSON object. Otherwise the method will throw a {@link BallerinaException}.
     */
    public static BStruct convertJSONNodeToStruct(JsonNode jsonNode, BStructType structType, PackageInfo pkgInfo) {
        if (!jsonNode.isObject()) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                    getComplexObjectTypeName(JsonNodeType.OBJECT), getTypeName(jsonNode));
        }

        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;

        BStruct bStruct = new BStruct(structType);
        StructInfo structInfo = pkgInfo.getStructInfo(structType.getName());
        for (StructFieldInfo fieldInfo : structInfo.getFieldInfoEntries()) {
            BType fieldType = fieldInfo.getFieldType();
            String fieldName = fieldInfo.getName();
            try {
                boolean containsField = jsonNode.has(fieldName);
                DefaultValueAttributeInfo defaultValAttrInfo = null;
                JsonNode jsonValue = null;

                if (containsField) {
                    jsonValue = jsonNode.get(fieldName);
                } else {
                    defaultValAttrInfo = (DefaultValueAttributeInfo) getAttributeInfo(fieldInfo,
                            AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE);
                }
                switch (fieldType.getTag()) {
                    case TypeTags.INT_TAG:
                        longRegIndex++;
                        if (containsField) {
                            bStruct.setIntField(longRegIndex, jsonNodeToInt(jsonValue));
                        } else if (defaultValAttrInfo != null) {
                            bStruct.setIntField(longRegIndex, defaultValAttrInfo.getDefaultValue().getIntValue());
                        }
                        break;
                    case TypeTags.FLOAT_TAG:
                        doubleRegIndex++;
                        if (containsField) {
                            bStruct.setFloatField(doubleRegIndex, jsonNodeToFloat(jsonValue));
                        } else if (defaultValAttrInfo != null) {
                            bStruct.setFloatField(doubleRegIndex, defaultValAttrInfo.getDefaultValue().getFloatValue());
                        }
                        break;
                    case TypeTags.STRING_TAG:
                        stringRegIndex++;
                        if (containsField) {
                            String stringVal;
                            if (jsonValue.isTextual()) {
                                stringVal = jsonValue.textValue();
                            } else {
                                stringVal = jsonValue.toString();
                            }
                            bStruct.setStringField(stringRegIndex, stringVal);
                        } else if (defaultValAttrInfo != null) {
                            bStruct.setStringField(stringRegIndex,
                                    defaultValAttrInfo.getDefaultValue().getStringValue());
                        }
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        booleanRegIndex++;
                        if (containsField) {
                            bStruct.setBooleanField(booleanRegIndex, jsonNodeToBool(jsonValue) ? 1 : 0);
                        } else if (defaultValAttrInfo != null) {
                            bStruct.setBooleanField(booleanRegIndex,
                                    defaultValAttrInfo.getDefaultValue().getBooleanValue() ? 1 : 0);
                        }
                        break;
                    default:
                        refRegIndex++;
                        if (!containsField) {
                            break;
                        }
                        if ((jsonValue == null || jsonValue.isNull())) {
                            bStruct.setRefField(refRegIndex, null);
                        } else if (fieldType instanceof BJSONType || fieldType instanceof BAnyType) {
                            bStruct.setRefField(refRegIndex, new BJSON(jsonValue));
                        } else if (fieldType instanceof BMapType) {
                            bStruct.setRefField(refRegIndex, jsonNodeToBMap(jsonValue));
                        } else if (fieldType instanceof BStructType) {
                            bStruct.setRefField(refRegIndex,
                                    convertJSONNodeToStruct(jsonValue, (BStructType) fieldType, pkgInfo));
                        } else if (fieldType instanceof BArrayType) {
                            bStruct.setRefField(refRegIndex,
                                    jsonNodeToBArray(jsonValue, (BArrayType) fieldType, pkgInfo));
                        } else {
                            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                                    fieldName, getTypeName(jsonValue));
                        }
                }
            } catch (BallerinaException e) {
                handleError(e, fieldName);
            }
        }

        return bStruct;
    }

    /**
     * Check the compatibility of casting a JSON to a target type.
     * 
     * @param json json to cast
     * @param targetType Target type
     * @return Runtime compatibility for casting
     */
    public static boolean checkJSONCast(JsonNode json, BType targetType) {
        switch (targetType.getTag()) {
            case TypeTags.STRING_TAG:
                return json.isTextual();
            case TypeTags.INT_TAG:
                return json.isInt() || json.isLong();
            case TypeTags.FLOAT_TAG:
                return json.isFloat() || json.isDouble();
            case TypeTags.ARRAY_TAG:
                if (!json.isArray()) {
                    return false;
                }

                boolean castable;
                BArrayType arrayType = (BArrayType) targetType;
                ArrayNode array = (ArrayNode) json;
                for (int i = 0; i < array.size(); i++) {
                    castable = checkJSONCast(array.get(i), arrayType.getElementType());
                    if (!castable) {
                        return false;
                    }
                }
                return true;
            default:
                return true;
        }
    }

    /**
     * Returns the keys of a JSON as a {@link BStringArray}.
     * 
     * @param json {@link BJSON} to get the keys
     * @return Keys of the JSON as a {@link BStringArray}
     */
    public static BStringArray getKeys(BJSON json) {
        if (json == null) {
            return new BStringArray();
        }

        JsonNode node = json.value();

        if (node.getNodeType() != JsonNodeType.OBJECT) {
            return new BStringArray();
        }

        List<String> keys = new ArrayList<String>();
        Iterator<String> keysItr = ((ObjectNode) node).fieldNames();
        while (keysItr.hasNext()) {
            keys.add(keysItr.next());
        }
        return new BStringArray(keys.toArray(new String[keys.size()]));
    }

    /**
     * Remove a field from JSON. Has no effect if the JSON if not object types or if the given field doesn't exists.
     * 
     * @param json JSON object
     * @param fieldName Name of the field to remove
     */
    public static void remove(BJSON json, String fieldName) {
        JsonNode node = json.value();

        if (node.getNodeType() != JsonNodeType.OBJECT) {
            return;
        }

        ((ObjectNode) node).remove(fieldName);
    }

    /**
     * Convert a JSON node to an array.
     *
     * @param jsonNode JSON to convert
     * @param targetArrayType Type of the target array
     * @return If the provided JSON is of array type, this method will return a {@link BArrayType} containing the values
     *         of the JSON array. Otherwise the method will throw a {@link BallerinaException}.
     */
    private static BNewArray jsonNodeToBArray(JsonNode jsonNode, BArrayType targetArrayType, PackageInfo pkgInfo) {
        if (!jsonNode.isArray()) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                    getComplexObjectTypeName(JsonNodeType.ARRAY), getTypeName(jsonNode));
        }

        BType elementType = targetArrayType.getElementType();
        BRefValueArray refValueArray;

        ArrayNode arrayNode = (ArrayNode) jsonNode;
        switch (elementType.getTag()) {
            case TypeTags.INT_TAG:
                return jsonNodeToBIntArray(arrayNode);
            case TypeTags.FLOAT_TAG:
                return jsonNodeToBFloatArray(arrayNode);
            case TypeTags.STRING_TAG:
                return jsonNodeToBStringArray(arrayNode);
            case TypeTags.BOOLEAN_TAG:
                return jsonNodeToBBooleanArray(arrayNode);
            case TypeTags.ANY_TAG:
                refValueArray = new BRefValueArray(elementType);
                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode element = arrayNode.get(i);
                    refValueArray.add(i, (BRefType) getBValue(element));
                }
                return refValueArray;
            default:
                refValueArray = new BRefValueArray(elementType);
                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode element = arrayNode.get(i);
                    if (elementType == BTypes.typeMap) {
                        refValueArray.add(i, jsonNodeToBMap(element));
                    } else if (elementType instanceof BStructType) {
                        refValueArray.add(i, convertJSONNodeToStruct(element, (BStructType) elementType, pkgInfo));
                    } else if (elementType instanceof BArrayType) {
                        refValueArray.add(i, jsonNodeToBArray(element, (BArrayType) elementType, pkgInfo));
                    } else {
                        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                                elementType, getTypeName(element));
                    }
                }
                return refValueArray;
        }
    }

    private static BIntArray jsonNodeToBIntArray(ArrayNode arrayNode) {
        BIntArray intArray = new BIntArray();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode jsonValue = arrayNode.get(i);
            intArray.add(i, jsonNodeToInt(jsonValue));
        }
        return intArray;
    }

    private static BFloatArray jsonNodeToBFloatArray(ArrayNode arrayNode) {
        BFloatArray floatArray = new BFloatArray();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode jsonValue = arrayNode.get(i);
            floatArray.add(i, jsonNodeToFloat(jsonValue));
        }
        return floatArray;
    }

    private static BStringArray jsonNodeToBStringArray(ArrayNode arrayNode) {
        BStringArray stringArray = new BStringArray();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode jsonValue = arrayNode.get(i);
            String value;
            if (jsonValue.isTextual()) {
                value = jsonValue.textValue();
            } else {
                value = jsonValue.toString();
            }
            stringArray.add(i, value);
        }
        return stringArray;
    }

    private static BBooleanArray jsonNodeToBBooleanArray(ArrayNode arrayNode) {
        BBooleanArray booleanArray = new BBooleanArray();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode jsonValue = arrayNode.get(i);
            booleanArray.add(i, jsonNodeToBool(jsonValue) ? 1 : 0);
        }
        return booleanArray;
    }

    /**
     * Get the corresponding BValue to hold the json-value, depending on its type.
     * 
     * @param json json node to get the BValue
     * @return BValue represents provided json
     */
    private static BValue getBValue(JsonNode json) {
        if (json == null || json.isNull()) {
            return null;
        } else if (json.isTextual()) {
            return new BString(json.textValue());
        } else if (json.isInt() || json.isLong()) {
            return new BInteger(json.longValue());
        } else if (json.isFloat() || json.isDouble()) {
            return new BFloat(json.doubleValue());
        } else if (json.isBoolean()) {
            return new BBoolean(json.booleanValue());
        }
        
        return new BJSON(json);
    }
    
    public static String getTypeName(JsonNode jsonValue) {
        JsonNodeType nodeType = jsonValue.getNodeType();
        switch(nodeType) {
            case NUMBER:
                NumberType numberType = jsonValue.numberType();
                switch(numberType) {
                    case DOUBLE:
                        return NumberType.FLOAT.name().toLowerCase();
                    case LONG:
                        return NumberType.INT.name().toLowerCase();
                    default:
                        return numberType.name().toLowerCase();
                }
            case ARRAY:
            case OBJECT:
                return getComplexObjectTypeName(nodeType);
            default:
                return nodeType.name().toLowerCase();
        }
    }
    
    private static String getComplexObjectTypeName(JsonNodeType nodeType) {
        return "json-" + nodeType.name().toLowerCase();
    }
    
    private static void handleError(BallerinaException e, String fieldName) {
        String errorMsg = e.getCause() == null ? "error while mapping '" + fieldName + "': " : "";
        throw new BallerinaException(errorMsg + e.getMessage(), e);
    }

    private static AttributeInfo getAttributeInfo(AttributeInfoPool attrInfoPool, AttributeInfo.Kind attrInfoKind) {
        for (AttributeInfo attributeInfo : attrInfoPool.getAttributeInfoEntries()) {
            if (attributeInfo.getKind() == attrInfoKind) {
                return attributeInfo;
            }
        }
        return null;
    }
}
