/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.natives.typemappers;

import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import java.util.HashSet;
import java.util.Set;

/**
 * For converting across native types.
 * 
 * @since 0.88
 */
public class NativeConversionMapper {
    
    // TODO: set this only once during semantic analyzer.
    private static StructDef castErrorStruct;

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> INT_TO_INT_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };
            
    public static final TriFunction<BValueType, BType, Boolean, BValue[]> INT_TO_FLOAT_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] { new BFloat(rVal.floatValue()), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> INT_TO_STRING_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] { new BString(rVal.stringValue()), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> INT_TO_BOOLEAN_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { new BBoolean(rVal.intValue() != 0), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> INT_TO_JSON_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] { new BJSON(rVal.stringValue()), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> FLOAT_TO_FLOAT_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };
                    
    public static final TriFunction<BValueType, BType, Boolean, BValue[]> FLOAT_TO_STRING_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { new BString(rVal.stringValue()), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> FLOAT_TO_BOOLEAN_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { new BBoolean(rVal.floatValue() != 0.0), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> FLOAT_TO_INT_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] {new BInteger(rVal.intValue()), null};

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> FLOAT_TO_JSON_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] {new BJSON(rVal.stringValue()), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> STRING_TO_STRING_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };
                    
    public static final TriFunction<BValueType, BType, Boolean, BValue[]> STRING_TO_INT_FUNC = 
            (rVal, targetType, returnErrors) -> {
                try {
                    return new BValue[] {new BInteger(rVal.intValue()), null };
                } catch (Exception e) {
                    String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, 
                        BTypes.typeString, BTypes.typeInt, e.getMessage());
                    return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeString, targetType);
                }
            };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> STRING_TO_FLOAT_FUNC = 
            (rVal, targetType, returnErrors) -> {
                try {
                    return new BValue[] { new BFloat(rVal.floatValue()), null };
                } catch (Exception e) {
                    String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                        BTypes.typeString, BTypes.typeFloat, e.getMessage());
                    return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeString, targetType);
                }
            };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> STRING_TO_BOOLEAN_FUNC =
            (rVal, targetType, returnErrors) -> {
                try {
                    return new BValue[] {new BBoolean(rVal.booleanValue()), null };
                } catch (Exception e) {
                    String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                        BTypes.typeString, BTypes.typeBoolean, e.getMessage());
                    return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeString, targetType);
                }
            };
    public static final TriFunction<BValueType, BType, Boolean, BValue[]> STRING_TO_JSON_FUNC =
        (rVal, targetType, returnErrors) -> {
            String jsonStr = rVal.stringValue();
            // If this is a string-representation of complex JSON object, generate a BJSON out of it.
            if (jsonStr.matches("\\{.*\\}|\\[.*\\]")) {
                return new BValue[] { new BJSON(jsonStr), null };
            }
            
            // Else, generate a BJSON with a quoted string.
            return new BValue[] { new BJSON("\"" + jsonStr + "\""), null };
        };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> STRING_TO_XML_FUNC =
            (rVal, targetType, returnErrors) -> {
                try {
                    return new BValue[] { XMLUtils.parse(rVal.stringValue()), null };
                } catch (Exception e) {
                    String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, 
                        BTypes.typeString, BTypes.typeXML, e.getMessage());
                    return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeString, targetType);
                }
            };
                
    public static final TriFunction<BValueType, BType, Boolean, BValue[]> BOOLEAN_TO_BOOLEAN_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };
                
    public static final TriFunction<BValueType, BType, Boolean, BValue[]> BOOLEAN_TO_STRING_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { new BString(rVal.stringValue()), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> BOOLEAN_TO_INT_FUNC =
            (rVal, targetType, returnErrors) -> rVal.booleanValue() ? new BValue[] { new BInteger(1), null }
            : new BValue[] { new BInteger(0), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> BOOLEAN_TO_FLOAT_FUNC =
            (rVal, targetType, returnErrors) -> rVal.booleanValue() ? new BValue[] { new BFloat(1.0f), null }
            : new BValue[] { new BFloat(0.0f), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> BOOLEAN_TO_JSON_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] { new BJSON(rVal.stringValue()), null };

    public static final TriFunction<BValueType, BType, Boolean, BValue[]> NULL_TO_JSON_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { new BJSON("null"), null };
                    
    public static final TriFunction<BValue, BType, Boolean, BValue[]> JSON_TO_STRING_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
                    BTypes.typeString);
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
            try {
                return new BValue[] { new BString(rVal.stringValue()), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeString, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
        };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> JSON_TO_INT_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                String errorMsg =
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeInt);
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
            try {
                return new BValue[] { JSONUtils.toBInteger((BJSON) rVal), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeInt, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
        };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> JSON_TO_FLOAT_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                String errorMsg =
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeFloat);
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
            try {
                return new BValue[] { JSONUtils.toBFloat((BJSON) rVal), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeFloat, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
        };
    
    public static final TriFunction<BValue, BType, Boolean, BValue[]> JSON_TO_BOOLEAN_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
                    BTypes.typeBoolean);
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
            try {
                return new BValue[] { JSONUtils.toBBoolean((BJSON) rVal), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeBoolean, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
        };
    
    /**
     * Function to cast a given JSON to a map.
     * All the fields in the JSON will be stored in the map, as is.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> JSON_TO_MAP_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            try {
                return new BValue[] { JSONUtils.toBMap((BJSON) rVal), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeMap, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
        };
    
    /**
     * Function to cast a given JSON to a struct.
     * Casting will be successful if and only if, source JSON has at least the set of fields,
     * (matching both in name and type) that the target struct expects.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> JSON_TO_STRUCT_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            try {
                return new BValue[] { JSONUtils.toBStruct((BJSON) rVal, (StructDef) targetType), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, targetType, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
        };
    
    /**
     * Function to cast a given JSON to a XML.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> JSON_TO_XML_FUNC =
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            try {
                return new BValue[] { XMLUtils.jsonToXML((BJSON) rVal), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeJSON, BTypes.typeXML, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeJSON, targetType);
            }
        };

    /**
     * Function to cast a given map to a JSON.
     * If the map contains reference type fields, those will be converted to their corresponding JSON
     * representations. i.e: Fields that are of map/struct type, will be converted to JSON objects. Fields 
     * are of array-type, will be converted to JSON array.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> MAP_TO_JSON_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            try {
                return new BValue[] { JSONUtils.toJSON((BMap<BString, BValue>) rVal), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeMap, BTypes.typeJSON, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeMap, targetType);
            }
        };

    /**
     * Function to cast a given map to a struct.
     * Casting will be successful if and only if, source map has at least the set of fields,
     * (matching both in name and type) that the target struct expects.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> MAP_TO_STRUCT_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }

            StructDef structDef = (StructDef) targetType;
            BValue[] structMemoryBlock = new BValue[structDef.getStructMemorySize()];
            BMap<BString, ?> map = (BMap<BString, ?>) rVal;

            Set<String> keys = new HashSet<String>();
            map.keySet().forEach(key -> keys.add(key.stringValue()));

            for (VariableDefStmt fieldDef : structDef.getFieldDefStmts()) {
                VariableDef targetVarDef = fieldDef.getVariableDef();
                String key = targetVarDef.getSymbolName().getName();
                BType targetFieldType = targetVarDef.getType();
                BValue mapVal = null;
                try {
                    if (!keys.contains(key)) {
                        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.MISSING_FIELD, key);
                    }

                    mapVal = map.get(new BString(key));
                    if (mapVal == null && BTypes.isValueType(targetFieldType)) {
                        throw BLangExceptionHelper.getRuntimeException(
                            RuntimeErrors.INCOMPATIBLE_FIELD_TYPE_FOR_CASTING, key, targetFieldType, null);
                    }

                    if (mapVal != null && !TypeMappingUtils.isCompatible(targetFieldType, mapVal.getType())) {
                        throw BLangExceptionHelper.getRuntimeException(
                            RuntimeErrors.INCOMPATIBLE_FIELD_TYPE_FOR_CASTING, key, targetFieldType, mapVal.getType());
                    }
                    structMemoryBlock[getStructMemoryOffset(targetVarDef)] = mapVal;
                } catch (BallerinaException e) {
                    String errorMsg = "cannot cast '" + rVal.getType() + "' to type '" + targetType + ": " + 
                            e.getMessage();
                    return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeMap, targetType);
                }
            }
            return new BValue[] { new BStruct(structDef, structMemoryBlock), null };
        };
    
    /**
     * Function to cast a given struct to a map.
     * All the fields in the struct will be stored in the map as is.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> STRUCT_TO_MAP_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            BStruct struct = (BStruct) rVal;
            StructDef structDef = (StructDef) struct.getType();
            BMap<BString, BValue> map = BTypes.typeMap.getEmptyValue();
            for (VariableDefStmt fieldDef : structDef.getFieldDefStmts()) {
                VariableDef varDef = fieldDef.getVariableDef();
                String key = varDef.getSymbolName().getName();
                BValue value = struct.getValue(getStructMemoryOffset(varDef));
                map.put(new BString(key), value == null ? null : value.copy());
            }
            return new BValue[] { map, null };
        };
    
    /**
     * Function to cast a given struct to a JSON.
     * If the struct contains reference type fields, those will be converted to their corresponding JSON
     * representations. i.e: Fields that are of map/struct type, will be converted to JSON objects. Fields 
     * are of array-type, will be converted to JSON array.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> STRUCT_TO_JSON_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            try {
                return new BValue[] { JSONUtils.toJSON((BStruct) rVal), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    rVal.getType(), BTypes.typeJSON, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
            }
        };
    
    /**
     * Function to cast a given array to a JSON array.
     * If the array contains reference type elements, those will be converted to their corresponding JSON
     * representations. i.e: Elements that are of map/struct type, will be converted to JSON objects. Elements 
     * that are of array-type, will be converted to JSON array.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ARRAY_TO_JSON_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            try {
                return new BValue[] { JSONUtils.toJSON((BArray) rVal), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    rVal.getType(), BTypes.typeJSON, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
            }
        };



    /**
     * Function to cast a given datatable to a JSON.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> DATATABLE_TO_JSON_FUNC =
            (rVal, targetType, returnErrors) -> {
                if (rVal == null) {
                    return new BValue[] { null, null };
                }
                try {
                    return new BValue[] { JSONUtils.toJSON((BDataTable) rVal, false), null };
                } catch (BallerinaException e) {
                    String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                            rVal.getType(), BTypes.typeJSON, e.getMessage());
                    return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
                }
            };

    /**
     * Function to cast a given datatable to a XML.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> DATATABLE_TO_XML_FUNC =
            (rVal, targetType, returnErrors) -> {
                if (rVal == null) {
                    return new BValue[] { null, null };
                }
                try {
                    return new BValue[] { XMLUtils.datatableToXML((BDataTable) rVal, false), null };
                } catch (BallerinaException e) {
                    String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                            BTypes.typeDatatable, BTypes.typeXML, e.getMessage());
                    return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeDatatable, targetType);
                }
            };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> XML_TO_JSON_FUNC =
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            try {
                return new BValue[] { XMLUtils.toJSON((BXML) rVal), null };
            } catch (BallerinaException e) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE,
                    BTypes.typeXML, BTypes.typeJSON, e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeXML, targetType);
            }
        };

    private static int getStructMemoryOffset(VariableDef varDef) {
        return ((StructVarLocation) varDef.getMemoryLocation()).getStructMemAddrOffset();
    }
}
