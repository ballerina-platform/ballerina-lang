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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.model.TableJSONDataSource;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BDecimalArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStreamingJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BTable;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.util.codegen.StructFieldInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Common utility methods used for JSON manipulation.
 * 
 * @since 0.87
 */
@SuppressWarnings("unchecked")
public class JSONUtils {

    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSI_PREFIX = "xsi";
    private static final String NIL = "nil";
    public static final String OBJECT = "object";
    public static final String ARRAY = "array";

    /**
     * Check whether JSON has particular field.
     *
     * @param json JSON to be considered.
     * @param elementName String name json field to be considered.
     * @return Boolean 'true' if JSON has given field.
     */
    public static boolean hasElement(BRefType<?> json, String elementName) {
        if (json.getType().getTag() != TypeTags.MAP_TAG) {
            return false;
        }
        return ((BMap<String, BValue>) json).hasKey(elementName);
    }

    /**
     * Convert {@link BIntArray} to JSON.
     *
     * @param intArray {@link BIntArray} to be converted to JSON
     * @return JSON representation of the provided intArray
     */
    private static BRefValueArray convertArrayToJSON(BIntArray intArray) {
        BRefValueArray json = new BRefValueArray(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < intArray.size(); i++) {
            long value = intArray.get(i);
            json.append(new BInteger(value));
        }
        return json;
    }

    /**
     * Convert {@link BFloatArray} to JSON.
     *
     * @param floatArray {@link BFloatArray} to be converted to JSON
     * @return JSON representation of the provided floatArray
     */
    private static BRefValueArray convertArrayToJSON(BFloatArray floatArray) {
        BRefValueArray json = new BRefValueArray(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < floatArray.size(); i++) {
            double value = floatArray.get(i);
            json.append(new BFloat(value));
        }
        return json;
    }

    /**
     * Convert {@link BDecimalArray} to JSON.
     *
     * @param decimalArray {@link BDecimalArray} to be converted to JSON
     * @return JSON representation of the provided decimalArray
     */
    private static BRefValueArray convertArrayToJSON(BDecimalArray decimalArray) {
        BRefValueArray json = new BRefValueArray(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < decimalArray.size(); i++) {
            BigDecimal value = decimalArray.get(i);
            json.append(new BDecimal(value));
        }
        return json;
    }

    /**
     * Convert {@link BStringArray} to JSON.
     *
     * @param stringArray {@link BStringArray} to be converted to JSON
     * @return JSON representation of the provided stringArray
     */
    private static BRefValueArray convertArrayToJSON(BStringArray stringArray) {
        BRefValueArray json = new BRefValueArray(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < stringArray.size(); i++) {
            String value = stringArray.get(i);
            json.append(new BString(value));
        }
        return json;
    }

    /**
     * Convert {@link BBooleanArray} to JSON.
     *
     * @param booleanArray {@link BBooleanArray} to be converted to JSON
     * @return JSON representation of the provided booleanArray
     */
    private static BRefValueArray convertArrayToJSON(BBooleanArray booleanArray) {
        BRefValueArray json = new BRefValueArray(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < booleanArray.size(); i++) {
            int value = booleanArray.get(i);
            json.append(new BBoolean(value == 1));
        }
        return json;
    }

    /**
     * Convert {@link BNewArray} to JSON.
     *
     * @param bArray {@link BNewArray} to be converted to JSON
     * @return JSON representation of the provided bArray
     */
    public static BRefValueArray convertArrayToJSON(BNewArray bArray) {
        if (bArray instanceof BIntArray) {
            return convertArrayToJSON((BIntArray) bArray);
        } else if (bArray instanceof BFloatArray) {
            return convertArrayToJSON((BFloatArray) bArray);
        } else if (bArray instanceof BDecimalArray) {
            return convertArrayToJSON((BDecimalArray) bArray);
        } else if (bArray instanceof BStringArray) {
            return convertArrayToJSON((BStringArray) bArray);
        } else if (bArray instanceof BBooleanArray) {
            return convertArrayToJSON((BBooleanArray) bArray);
        } else if (bArray instanceof BRefValueArray) {
            return convertRefArrayToJSON((BRefValueArray) bArray);
        }

        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, BTypes.typeJSON,
                bArray.getType());
    }

    /**
     * Convert {@link BRefValueArray} to JSON.
     *
     * @param refValueArray {@link BRefValueArray} to be converted to JSON
     * @return JSON representation of the provided refValueArray
     */
    @SuppressWarnings({ "rawtypes" })
    public static BRefValueArray convertRefArrayToJSON(BRefValueArray refValueArray) {
        BRefValueArray json = new BRefValueArray(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < refValueArray.size(); i++) {
            BRefType value = refValueArray.get(i);
            if (value == null) {
                json.append(null);
            }

            switch (value.getType().getTag()) {
                case TypeTags.JSON_TAG:
                    json.append(value);
                    break;
                case TypeTags.MAP_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.OBJECT_TYPE_TAG:
                    json.append(convertMapToJSON((BMap<String, BValue>) value, (BJSONType) BTypes.typeJSON));
                    break;
                case TypeTags.ARRAY_TAG:
                    json.append(convertArrayToJSON((BNewArray) value));
                    break;
                default:
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                            BTypes.typeJSON, value.getType());
            }
        }
        return json;
    }

    /**
     * Convert map value to JSON.
     *
     * @param map value {@link BMap} to be converted to JSON
     * @param targetType the target JSON type to be convert to
     * @return JSON representation of the provided array
     */
    public static BRefType<?> convertMapToJSON(BMap<String, BValue> map, BJSONType targetType) {
        if (map == null) {
            return null;
        }

        BMap<String, BValue> json = new BMap<>(targetType);
        if (targetType.getConstrainedType() == null) {
            for (Entry<String, BValue> structField : map.getMap().entrySet()) {
                String key = structField.getKey();
                BValue value = structField.getValue();
                populateJSON(json, key, value, BTypes.typeJSON);
            }
        } else {
            if (!CPU.checkCast(map, targetType.getConstrainedType())) {
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                        targetType, map.getType());
            }

            for (Entry<String, BField> fieldEntry :
                    ((BStructureType) targetType.getConstrainedType()).getFields().entrySet()) {
                String key = fieldEntry.getKey();
                BValue value = map.get(key);
                populateJSON(json, key, value, fieldEntry.getValue().fieldType);
            }
        }
        return json;
    }
    
    private static void populateJSON(BMap<String, BValue> json, String key, BValue value, BType exptType) {
        try {
            if (value == null) {
                json.put(key, null);
                return;
            }

            switch (value.getType().getTag()) {
                case TypeTags.INT_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.BOOLEAN_TAG:
                case TypeTags.JSON_TAG:
                    json.put(key, value);
                    break;
                case TypeTags.ARRAY_TAG:
                    json.put(key, convertArrayToJSON((BNewArray) value));
                    break;
                case TypeTags.MAP_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.OBJECT_TYPE_TAG:
                    json.put(key, convertMapToJSON((BMap<String, BValue>) value, (BJSONType) exptType));
                    break;
                default:
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                            BTypes.typeJSON, value.getType());
            }
        } catch (Exception e) {
            handleError(e, key);
        }
    }
    
    /**
     * Convert {@link BTable} to JSON.
     *
     * @param table {@link BTable} to be converted to {@link BStreamingJSON}
     * @param isInTransaction   Within a transaction or not
     * @return JSON representation of the provided table
     */
    public static BRefType<?> toJSON(BTable table, boolean isInTransaction) {
        TableJSONDataSource jsonDataSource = new TableJSONDataSource(table);
        if (table.isInMemoryTable()) {
            return jsonDataSource.build();
        }

        return new BStreamingJSON(jsonDataSource);
    }

    /**
     * Get an element from a JSON.
     * 
     * @param json JSON object to get the element from
     * @param elementName Name of the element to be retrieved
     * @return Element of JSON having the provided name, if the JSON is object type. Null otherwise.
     */
    public static BRefType<?> getElement(BValue json, String elementName) {
        if (json == null || !isJSONObject(json)) {
            return null;
        }
        
        try {
            return ((BMap<String, BRefType<?>>) json).get(elementName);
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_GET_ERROR, t.getMessage());
        }
    }
    
    /**
     * Set an element in a JSON. If an element with the given name already exists,
     * this method will update the existing element. Otherwise, a new element with
     * the given name will be added. If the JSON is not object type, then this
     * operation has no effect.
     * 
     * @param json JSON object to set the element
     * @param elementName Name of the element to be set
     * @param element JSON element
     */
    public static void setElement(BValue json, String elementName, BValue element) {
        if (json == null || !isJSONObject(json)) {
            return;
        }

        try {
            ((BMap<String, BValue>) json).put(elementName, element);
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
    public static boolean isJSONArray(BValue json) {
        if (json == null) {
            return false;
        }
        return json.getType().getTag() == TypeTags.ARRAY_TAG;
    }

    /**
     * Check whether provided JSON object is a JSON Object.
     *
     * @param json JSON to execute array condition.
     * @return returns true if provided JSON is a JSON Object.
     */
    public static boolean isJSONObject(BValue json) {
        if (json == null) {
            return false;
        }
        return json.getType().getTag() == TypeTags.JSON_TAG || json.getType().getTag() == TypeTags.MAP_TAG;
    }

    /**
     * Get an element from a JSON array.
     * 
     * @param jsonArray JSON array to get the element from
     * @param index Index of the element needed
     * @return Element at the given index, if the provided JSON is an array. Null, otherwise. 
     */
    public static BRefType<?> getArrayElement(BRefType<?> jsonArray, long index) {
        if (!isJSONArray(jsonArray)) {
            return null;
        }

        try {
            return ListUtils.execListGetOperation((BNewArray) jsonArray, index);
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_GET_ERROR, t.getMessage());
        }
    }

    /**
     * Set an element in the given position of a JSON array. This method will update the existing value.
     * If the JSON is not array type, then this operation has no effect.
     * 
     * @param json JSON array to set the element
     * @param index Index of the element to be set
     * @param element Element to be set
     */
    public static void setArrayElement(BValue json, long index, BRefType element) {
        if (!isJSONArray(json)) {
            return;
        }

        BArrayType jsonArray = (BArrayType) json.getType();
        BType elementType = jsonArray.getElementType();
        if (!CPU.checkCast(element, elementType)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                    elementType, (element != null) ? element.getType() : BTypes.typeNull);
        }

        try {
            ListUtils.execListAddOperation((BNewArray) json, index, element);
        } catch (Throwable t) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.JSON_SET_ERROR, t.getMessage());
        }
    }

    /**
     * Converts given JSON object to the corresponding XML.
     *
     * @param json JSON object to get the corresponding XML
     * @param attributePrefix String prefix used for attributes
     * @param arrayEntryTag String used as the tag in the arrays
     * @return BXML XML representation of the given JSON object
     */
    @SuppressWarnings("rawtypes")
    public static BXML convertToXML(BValue json, String attributePrefix, String arrayEntryTag) {
        BXML xml;
        if (json == null) {
            return new BXMLSequence();
        }

        List<BXML> omElementArrayList = traverseTree(json, attributePrefix, arrayEntryTag);
        if (omElementArrayList.size() == 1) {
            xml = omElementArrayList.get(0);
        } else {
            // There is a multi rooted node and create xml sequence from it
            BRefValueArray elementsSeq = new BRefValueArray();
            int count = omElementArrayList.size();
            for (int i = 0; i < count; i++) {
                elementsSeq.add(i, omElementArrayList.get(i));
            }
            xml = new BXMLSequence(elementsSeq);
        }
        return xml;
    }

    /**
     * Traverse a JSON root node and produces the corresponding XML items.
     *
     * @param node {@link JsonNode} to be traversed
     * @param attributePrefix String prefix used for attributes
     * @param arrayEntryTag String used as the tag in the arrays
     * @return List of XML items generated during the traversal.
     */
    @SuppressWarnings("rawtypes")
    private static List<BXML> traverseTree(BValue json, String attributePrefix, String arrayEntryTag) {
        List<BXML> xmlArray = new ArrayList<>();
        if (json instanceof BValueType) {
            BXML xml = XMLUtils.parse(json.stringValue());
            xmlArray.add(xml);
        } else {
            traverseJsonNode(json, null, null, xmlArray, attributePrefix, arrayEntryTag);
        }
        return xmlArray;
    }

    /**
     * Traverse a JSON node ad produces the corresponding xml items.
     *
     * @param json {@link JsonNode} to be traversed
     * @param nodeName name of the current traversing node
     * @param parentElement parent element of the current node
     * @param omElementArrayList List of XML items generated
     * @param attributePrefix String prefix used for attributes
     * @param arrayEntryTag String used as the tag in the arrays
     * @return List of XML items generated during the traversal.
     */
    @SuppressWarnings("rawtypes")
    private static OMElement traverseJsonNode(BValue json, String nodeName, OMElement parentElement,
                                              List<BXML> omElementArrayList, String attributePrefix,
                                              String arrayEntryTag) {
        OMElement currentRoot = null;
        if (nodeName != null) {
            // Extract attributes and set to the immediate parent.
            if (nodeName.startsWith(attributePrefix)) {
                if (!(json instanceof BValueType)) {
                    throw new BallerinaException("attribute cannot be an object or array");
                }
                if (parentElement != null) {
                    String attributeKey = nodeName.substring(1);
                    // Validate whether the attribute name is an XML supported qualified name, according to the XML
                    // recommendation.
                    XMLValidationUtils.validateXMLName(attributeKey);

                    parentElement.addAttribute(attributeKey, json.stringValue(), null);
                }
                return parentElement;
            }

            // Validate whether the tag name is an XML supported qualified name, according to the XML recommendation.
            XMLValidationUtils.validateXMLName(nodeName);

            currentRoot = OM_FACTORY.createOMElement(nodeName, null);
        }

        if (json == null) {
            OMNamespace xsiNameSpace = OM_FACTORY.createOMNamespace(XSI_NAMESPACE, XSI_PREFIX);
            currentRoot.addAttribute(NIL, "true", xsiNameSpace);
        } else {
            switch (json.getType().getTag()) {
                case TypeTags.JSON_TAG:
                    LinkedHashMap<String, BValue> map = ((BMap) json).getMap();
                    for (Entry<String, BValue> entry : map.entrySet()) {
                        currentRoot = traverseJsonNode(entry.getValue(), entry.getKey(), currentRoot,
                                omElementArrayList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost object
                            omElementArrayList.add(new BXMLItem(currentRoot));
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.ARRAY_TAG:
                    BRefValueArray array = (BRefValueArray) json;
                    for (int i = 0; i < array.size(); i++) {
                        currentRoot = traverseJsonNode(array.get(i), arrayEntryTag, currentRoot, omElementArrayList,
                                attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost array
                            omElementArrayList.add(new BXMLItem(currentRoot));
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.INT_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.BOOLEAN_TAG:
                    if (currentRoot == null) {
                        throw new BallerinaException("error in converting json to xml");
                    }
                    
                    OMText txt1 = OM_FACTORY.createOMText(currentRoot, json.stringValue());
                    currentRoot.addChild(txt1);
                    break;
                default:
                    throw new BallerinaException("error in converting json to xml");
            }
        }

        // Set the current constructed root the parent element
        if (parentElement != null) {
            parentElement.addChild(currentRoot);
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
    private static BInteger jsonNodeToInt(BValue json) {
        if (json == null || json.getType().getTag() != TypeTags.INT_TAG) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeInt, getTypeName(json));
        }

        return (BInteger) json;
    }

    /**
     * Convert {@link JsonNode} to float.
     *
     * @param jsonNode {@link JsonNode} to be converted
     * @return BFloat value of the JSON, if its a double or a float JSON node. Error, otherwise.
     */
    private static BFloat jsonNodeToFloat(BValue json) {
        if (json == null ||
                (json.getType().getTag() != TypeTags.INT_TAG && json.getType().getTag() != TypeTags.FLOAT_TAG)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeFloat, getTypeName(json));
        }

        if (json.getType().getTag() == TypeTags.INT_TAG) {
            return new BFloat(((BInteger) json).intValue());
        }

        return (BFloat) json;
    }

    /**
     * Convert JSON to decimal.
     *
     * @param json JSON to be converted
     * @return BDecimal value of the JSON, if it's a valid convertible JSON node. Error, otherwise.
     */
    private static BDecimal jsonNodeToDecimal(BValue json) {
        if (json == null ||
                (json.getType().getTag() != TypeTags.INT_TAG && json.getType().getTag() != TypeTags.FLOAT_TAG &&
                        json.getType().getTag() != TypeTags.DECIMAL_TAG)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeDecimal, getTypeName(json));
        }

        if (json.getType().getTag() == TypeTags.INT_TAG) {
            return new BDecimal(((BInteger) json).decimalValue());
        } else if (json.getType().getTag() == TypeTags.FLOAT_TAG) {
            return new BDecimal(((BFloat) json).decimalValue());
        }

        return (BDecimal) json;
    }

    /**
     * Convert {@link JsonNode} to boolean.
     *
     * @param jsonNode {@link JsonNode} to be converted
     * @return BBoolean value of the JSON, if its a boolean node. Error, otherwise.
     */
    private static BBoolean jsonNodeToBool(BValue json) {
        if (json == null || json.getType().getTag() != TypeTags.BOOLEAN_TAG) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeBoolean, getTypeName(json));
        }
        return (BBoolean) json;
    }

    /**
     * Convert a JSON node to a map.
     *
     * @param json JSON to convert
     * @param mapType MapType which the JSON is converted to.
     * @return If the provided JSON is of object-type, this method will return a {@link BMap} containing the values
     * of the JSON object. Otherwise a {@link BallerinaException} will be thrown.
     */
    public static BMap<String, ?> jsonToBMap(BValue json, BMapType mapType) {
        if (json == null || !isJSONObject(json)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                    getComplexObjectTypeName(OBJECT), getTypeName(json));
        }

        BMap<String, BValue> map = new BMap<>(mapType);
        BType mapConstraint = mapType.getConstrainedType();
        if (mapConstraint == null || mapConstraint.getTag() == TypeTags.ANY_TAG ||
                mapConstraint.getTag() == TypeTags.JSON_TAG) {
            ((BMap<String, BValue>) json).getMap().entrySet().forEach(entry -> {
                map.put(entry.getKey(), entry.getValue());
            });

            return map;
        }

        // We reach here if the map is constrained.
        ((BMap<String, BRefType<?>>) json).getMap().entrySet().forEach(entry -> {
            map.put(entry.getKey(), convertJSON(entry.getValue(), mapConstraint));
        });

        return map;
    }

    /**
     * Convert a BJSON to a user defined struct.
     *
     * @param json JSON to convert
     * @param structType Type (definition) of the target struct
     * @return If the provided JSON is of object-type, this method will return a {@link BMap} containing the values
     * of the JSON object. Otherwise the method will throw a {@link BallerinaException}.
     */
    public static BMap<String, BValue> convertJSONToStruct(BValue json, BStructureType structType) {
        if (json == null || !isJSONObject(json)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                    getComplexObjectTypeName(OBJECT), getTypeName(json));
        }

        BMap<String, BValue> bStruct = new BMap<>(structType);
        BMap<String, BRefType<?>> jsonObject = (BMap<String, BRefType<?>>) json;
        StructureTypeInfo structInfo = (StructureTypeInfo) structType.getTypeInfo();
        for (StructFieldInfo fieldInfo : structInfo.getFieldInfoEntries()) {
            BType fieldType = fieldInfo.getFieldType();
            String fieldName = fieldInfo.getName();
            try {
                // If the field does not exists in the JSON, set the default value for that struct field.
                if (!jsonObject.hasKey(fieldName)) {
                    bStruct.put(fieldName, fieldType.getZeroValue());
                    continue;
                }

                BRefType<?> jsonValue = jsonObject.get(fieldName);
                bStruct.put(fieldName, convertJSON(jsonValue, fieldType));
            } catch (Exception e) {
                handleError(e, fieldName);
            }
        }

        return bStruct;
    }

    public static BRefType<?> convertJSON(BRefType<?> jsonValue, BType targetType) {
        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
                return jsonNodeToInt(jsonValue);
            case TypeTags.FLOAT_TAG:
                return jsonNodeToFloat(jsonValue);
            case TypeTags.DECIMAL_TAG:
                return jsonNodeToDecimal(jsonValue);
            case TypeTags.STRING_TAG:
                return new BString(jsonValue.stringValue());
            case TypeTags.BOOLEAN_TAG:
                return jsonNodeToBool(jsonValue);
            case TypeTags.JSON_TAG:
                if (jsonValue != null && !CPU.checkCast(jsonValue, targetType)) {
                    throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                            targetType, getTypeName(jsonValue));
                }
                // fall through
            case TypeTags.ANY_TAG:
                return jsonValue;
            case TypeTags.UNION_TAG:
                BUnionType type = (BUnionType) targetType;
                if (jsonValue == null && type.isNullable()) {
                    return null;
                }
                List<BType> matchingTypes = type.getMemberTypes().stream()
                        .filter(memberType -> memberType != BTypes.typeNull).collect(Collectors.toList());
                if (matchingTypes.size() == 1) {
                    return convertJSON(jsonValue, matchingTypes.get(0));
                }
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return convertJSONToStruct(jsonValue, (BStructureType) targetType);
            case TypeTags.ARRAY_TAG:
                return convertJSONToBArray(jsonValue, (BArrayType) targetType);
            case TypeTags.MAP_TAG:
                return jsonToBMap(jsonValue, (BMapType) targetType);
            case TypeTags.NULL_TAG:
                if (jsonValue == null) {
                    return null;
                }
                // fall through
            default:
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, targetType,
                        getTypeName(jsonValue));
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE, targetType,
                getTypeName(jsonValue));
    }

    /**
     * Returns the keys of a JSON as a {@link BStringArray}.
     * 
     * @param json JSON to get the keys
     * @return Keys of the JSON as a {@link BStringArray}
     */
    public static BStringArray getKeys(BValue json) {
        if (json == null || !isJSONObject(json)) {
            return new BStringArray();
        }

        String[] keys = ((BMap<String, BValue>) json).keys();
        return new BStringArray(keys);
    }

    public static BRefType<?> convertUnionTypeToJSON(BRefType<?> source, BJSONType targetType) {
        if (source == null) {
            return null;
        }

        switch (source.getType().getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
                return source;
            case TypeTags.NULL_TAG:
                return null;
            case TypeTags.MAP_TAG:
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return convertMapToJSON((BMap<String, BValue>) source, targetType);
            case TypeTags.JSON_TAG:
                return source;
            default:
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                        BTypes.typeJSON, source.getType());
        }
    }

    /**
     * Remove a field from JSON. Has no effect if the JSON if not object types or if the given field doesn't exists.
     * 
     * @param json JSON object
     * @param fieldName Name of the field to remove
     */
    public static void remove(BValue json, String fieldName) {
        if (json == null || json.getType().getTag() != TypeTags.JSON_TAG) {
            return;
        }

        ((BMap<String, ?>) json).remove(fieldName);
    }

    /**
     * Convert a JSON node to an array.
     *
     * @param json JSON to convert
     * @param targetArrayType Type of the target array
     * @return If the provided JSON is of array type, this method will return a {@link BArrayType} containing the values
     *         of the JSON array. Otherwise the method will throw a {@link BallerinaException}.
     */
    public static BNewArray convertJSONToBArray(BValue json, BArrayType targetArrayType) {
        if (!(json instanceof BNewArray)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INCOMPATIBLE_TYPE,
                    getComplexObjectTypeName(ARRAY), getTypeName(json));
        }

        BType targetElementType = targetArrayType.getElementType();
        BRefValueArray jsonArray = (BRefValueArray) json;
        switch (targetElementType.getTag()) {
            case TypeTags.INT_TAG:
                return jsonArrayToBIntArray(jsonArray);
            case TypeTags.FLOAT_TAG:
                return jsonArrayToBFloatArray(jsonArray);
            case TypeTags.DECIMAL_TAG:
                return jsonArrayToBDecimalArray(jsonArray);
            case TypeTags.STRING_TAG:
                return jsonArrayToBStringArray(jsonArray);
            case TypeTags.BOOLEAN_TAG:
                return jsonArrayToBBooleanArray(jsonArray);
            case TypeTags.ANY_TAG:
                BRefValueArray array = new BRefValueArray(targetArrayType);
                for (int i = 0; i < jsonArray.size(); i++) {
                    array.add(i, jsonArray.get(i));
                }
                return array;
            default:
                array = new BRefValueArray(targetArrayType);
                for (int i = 0; i < jsonArray.size(); i++) {
                    array.append(convertJSON(jsonArray.get(i), targetElementType));
                }
                return array;
        }
    }

    private static BIntArray jsonArrayToBIntArray(BRefValueArray arrayNode) {
        BIntArray intArray = new BIntArray();
        for (int i = 0; i < arrayNode.size(); i++) {
            BRefType<?> jsonValue = arrayNode.get(i);
            intArray.add(i, ((BInteger) convertJSON(jsonValue, BTypes.typeInt)).intValue());
        }
        return intArray;
    }

    private static BFloatArray jsonArrayToBFloatArray(BRefValueArray arrayNode) {
        BFloatArray floatArray = new BFloatArray();
        for (int i = 0; i < arrayNode.size(); i++) {
            BRefType<?> jsonValue = arrayNode.get(i);
            floatArray.add(i, ((BFloat) convertJSON(jsonValue, BTypes.typeFloat)).floatValue());
        }
        return floatArray;
    }

    private static BDecimalArray jsonArrayToBDecimalArray(BRefValueArray arrayNode) {
        BDecimalArray decimalArray = new BDecimalArray();
        for (int i = 0; i < arrayNode.size(); i++) {
            BRefType<?> jsonValue = arrayNode.get(i);
            decimalArray.add(i, ((BDecimal) convertJSON(jsonValue, BTypes.typeDecimal)).decimalValue());
        }
        return decimalArray;
    }

    private static BStringArray jsonArrayToBStringArray(BRefValueArray arrayNode) {
        BStringArray stringArray = new BStringArray();
        for (int i = 0; i < arrayNode.size(); i++) {
            BRefType<?> jsonValue = arrayNode.get(i);
            String value = jsonValue.stringValue();
            stringArray.add(i, value);
        }
        return stringArray;
    }

    private static BBooleanArray jsonArrayToBBooleanArray(BRefValueArray arrayNode) {
        BBooleanArray booleanArray = new BBooleanArray();
        for (int i = 0; i < arrayNode.size(); i++) {
            BRefType<?> jsonValue = arrayNode.get(i);
            booleanArray.add(i, ((BBoolean) convertJSON(jsonValue, BTypes.typeBoolean)).booleanValue() ? 1 : 0);
        }
        return booleanArray;
    }
    
    public static String getTypeName(BValue jsonValue) {
        if (jsonValue == null) {
            return BTypes.typeNull.toString();
        }

        return jsonValue.getType().toString();
    }
    
    private static String getComplexObjectTypeName(String nodeType) {
        return "json-" + nodeType;
    }
    
    private static void handleError(Exception e, String fieldName) {
        String errorMsg = e.getCause() == null ? "error while mapping '" + fieldName + "': " : "";
        throw new BallerinaException(errorMsg + e.getMessage(), e);
    }
}
