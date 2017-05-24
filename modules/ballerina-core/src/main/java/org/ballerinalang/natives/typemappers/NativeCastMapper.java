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
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeEdge;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * For converting across native types.
 */
public class NativeCastMapper {

    public static final BiFunction<BValueType, BType, BValueType> INT_TO_FLOAT_FUNC = 
            (rVal, targetType) -> new BFloat(rVal.floatValue());

    public static final BiFunction<BValueType, BType, BValueType> INT_TO_STRING_FUNC = 
            (rVal, targetType) -> new BString(rVal.stringValue());

    public static final BiFunction<BValueType, BType, BValueType> INT_TO_BOOLEAN_FUNC =
        (rVal, targetType) -> new BBoolean(rVal.intValue() != 0);

    public static final BiFunction<BValueType, BType, BValueType> INT_TO_INT_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValueType, BType, BValue> INT_TO_JSON_FUNC = 
            (rVal, targetType) -> new BJSON(rVal.stringValue());

    public static final BiFunction<BValueType, BType, BValueType> FLOAT_TO_STRING_FUNC =
            (rVal, targetType) -> new BString(rVal.stringValue());

    public static final BiFunction<BValueType, BType, BValueType> FLOAT_TO_BOOLEAN_FUNC =
            (rVal, targetType) -> new BBoolean(rVal.floatValue() != 0.0);

    public static final BiFunction<BValueType, BType, BValueType> FLOAT_TO_FLOAT_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValueType, BType, BValueType> FLOAT_TO_INT_FUNC = 
            (rVal, targetType) -> new BInteger(rVal.intValue());

    public static final BiFunction<BValueType, BType, BValue> FLOAT_TO_JSON_FUNC = 
            (rVal, targetType) -> new BJSON(rVal.stringValue());

    public static final BiFunction<BValueType, BType, BValueType> STRING_TO_INT_FUNC = 
            (rVal, targetType) -> new BInteger(rVal.intValue());

    public static final BiFunction<BValueType, BType, BValueType> STRING_TO_FLOAT_FUNC = 
            (rVal, targetType) -> new BFloat(rVal.floatValue());

    public static final BiFunction<BValueType, BType, BValueType> STRING_TO_BOOLEAN_FUNC =
        (rVal, targetType) -> new BBoolean(rVal.booleanValue());

    public static final BiFunction<BValueType, BType, BValueType> STRING_TO_STRING_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValueType, BType, BValue> STRING_TO_JSON_FUNC =
        (rVal, targetType) -> {
            String jsonStr = rVal.stringValue();
            // If this is a string-representation of complex JSON object, generate a BJSON out of it.
            if (jsonStr.matches("\\{.*\\}|\\[.*\\]")) {
                return new BJSON(jsonStr);
            }
            
            // Else, generate a BJSON with a quoted string.
            return new BJSON("\"" + jsonStr + "\"");
        };

    public static final BiFunction<BValueType, BType, BValueType> BOOLEAN_TO_STRING_FUNC =
        (rVal, targetType) -> new BString(rVal.stringValue());

    public static final BiFunction<BValueType, BType, BValueType> BOOLEAN_TO_INT_FUNC =
        (rVal, targetType) -> rVal.booleanValue() ? new BInteger(1) : new BInteger(0);

    public static final BiFunction<BValueType, BType, BValueType> BOOLEAN_TO_FLOAT_FUNC =
        (rVal, targetType) -> rVal.booleanValue() ? new BFloat(1.0f) : new BFloat(0.0f);

    public static final BiFunction<BValueType, BType, BValueType> BOOLEAN_TO_BOOLEAN_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValueType, BType, BValue> BOOLEAN_TO_JSON_FUNC = 
            (rVal, targetType) -> new BJSON(rVal.stringValue());

    public static final BiFunction<BValue, BType, BValueType> JSON_TO_STRING_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, 
                    BTypes.typeString);
        }
        try {
            return new BString(rVal.stringValue());
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, BTypes.typeJSON,
                    BTypes.typeString, e.getMessage());
        }
    };

    public static final BiFunction<BValue, BType, BValueType> JSON_TO_INT_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, 
                    BTypes.typeInt);
        }
        try {
            return JSONUtils.toBInteger((BJSON) rVal);
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, BTypes.typeJSON,
                    BTypes.typeInt, e.getMessage());
        }
    };

    public static final BiFunction<BValue, BType, BValueType> JSON_TO_FLOAT_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, 
                    BTypes.typeFloat);
        }
        try {
            return JSONUtils.toBFloat((BJSON) rVal);
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, BTypes.typeJSON,
                    BTypes.typeFloat, e.getMessage());
        }
    };
    
    public static final BiFunction<BValue, BType, BValue> JSON_TO_BOOLEAN_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, 
                    BTypes.typeBoolean);
        }
        try {
            return JSONUtils.toBBoolean((BJSON) rVal);
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, BTypes.typeJSON,
                    BTypes.typeBoolean, e.getMessage());
        }
    };
    
    /**
     * Function to cast a given JSON to a map.
     * All the fields in the JSON will be stored in the map, as is.
     */
    public static final BiFunction<BValue, BType, BValue> JSON_TO_MAP_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        try {
            return JSONUtils.toBMap((BJSON) rVal);
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, BTypes.typeJSON,
                    BTypes.typeMap, e.getMessage());
        }
    };
    
    /**
     * Function to cast a given JSON to a struct.
     * Casting will be successful if and only if, source JSON has at least the set of fields,
     * (matching both in name and type) that the target struct expects.
     */
    public static final BiFunction<BValue, BType, BValue> JSON_TO_STRUCT_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        try {
            return JSONUtils.toBStruct((BJSON) rVal, (StructDef) targetType);
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, BTypes.typeJSON,
                    targetType, e.getMessage());
        }
    };
    
    /**
     * Function to cast a given map to a JSON.
     * If the map contains reference type fields, those will be converted to their corresponding JSON
     * representations. i.e: Fields that are of map/struct type, will be converted to JSON objects. Fields 
     * are of array-type, will be converted to JSON array.
     */
    public static final BiFunction<BValue, BType, BValue> MAP_TO_JSON_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        try {
            return JSONUtils.toJSON((BMap<BString, BValue>) rVal);
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, BTypes.typeMap,
                    BTypes.typeJSON, e.getMessage());
        }
    };

    /**
     * Function to cast a given map to a struct.
     * Casting will be successful if and only if, source map has at least the set of fields,
     * (matching both in name and type) that the target struct expects.
     */
    public static final BiFunction<BValue, BType, BValue> MAP_TO_STRUCT_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
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
                
                // If the two types are not compatible, check for possibility of implicit casting
                if (mapVal != null && !isCompatible(targetFieldType, mapVal.getType())) {
                    if (mapVal instanceof BArray<?>) {
                        // TODO: remove this once the array casting is supported
                        throw new BallerinaException("error while mapping '" + key + "': array casting is not " +
                            "supported yet. expected '" + targetFieldType + "', found' " + mapVal.getType() + "'");
                    }
                    TypeEdge newEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(mapVal.getType(), 
                            targetFieldType, null);
                    if (newEdge == null) {
                        throw BLangExceptionHelper.getRuntimeException(
                            RuntimeErrors.INCOMPATIBLE_FIELD_TYPE_FOR_CASTING, key, targetFieldType, mapVal.getType());
                    }
                    mapVal = newEdge.getTypeMapperFunction().apply(mapVal, targetFieldType);
                }
            } catch (BallerinaException e) {
                handleError(rVal.getType(), targetType, e, key);
            }
            
            structMemoryBlock[getStructMemoryOffset(targetVarDef)] = mapVal;
        }
        return new BStruct(structDef, structMemoryBlock);
    };
    
    /**
     * Function to cast a given struct to another. The compatibility is checked during the semantic analyzer phase.
     * Therefore the same value is returned as is, maintaining its originated type ({@link StructDef}) as meta info.
     */
    public static final BiFunction<BValue, BType, BValue> STRUCT_TO_STRUCT_FUNC = (rVal, targetType) -> {
        return rVal;
    };
    
    /**
     * Function to cast a given struct to a map.
     * All the fields in the struct will be stored in the map as is.
     */
    public static final BiFunction<BValue, BType, BValue> STRUCT_TO_MAP_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        BStruct struct = (BStruct) rVal;
        StructDef structDef = (StructDef) struct.getType();
        BMap<BString, BValue> map = BTypes.typeMap.getEmptyValue();
        for (VariableDefStmt fieldDef : structDef.getFieldDefStmts()) {
            VariableDef varDef = fieldDef.getVariableDef();
            String key = varDef.getSymbolName().getName();
            map.put(new BString(key), struct.getValue(getStructMemoryOffset(varDef)));
        }
        return map;
    };
    
    /**
     * Function to cast a given struct to a JSON.
     * If the struct contains reference type fields, those will be converted to their corresponding JSON
     * representations. i.e: Fields that are of map/struct type, will be converted to JSON objects. Fields 
     * are of array-type, will be converted to JSON array.
     */
    public static final BiFunction<BValue, BType, BValue> STRUCT_TO_JSON_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        try {
            return JSONUtils.toJSON((BStruct) rVal);
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, rVal.getType(),
                    BTypes.typeJSON, e.getMessage());
        }
    };
    
    /**
     * Function to cast a given array to a JSON array.
     * If the array contains reference type elements, those will be converted to their corresponding JSON
     * representations. i.e: Elements that are of map/struct type, will be converted to JSON objects. Elements 
     * that are of array-type, will be converted to JSON array.
     */
    public static final BiFunction<BValue, BType, BValue> ARRAY_TO_JSON_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        try {
            return JSONUtils.toJSON((BArray) rVal);
        } catch (BallerinaException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, rVal.getType(),
                    BTypes.typeJSON, e.getMessage());
        }
    };
    
    public static final BiFunction<BValue, BType, BValueType> XML_TO_STRING_FUNC =
            (rVal, targetType) -> new BString(rVal.stringValue());
    
    /*
     * below functions are only needed if explicit casting happens from other types to any type.
     * ex - any n = (any)1;
     */
    
    public static final BiFunction<BValue, BType, BValue> INT_TO_ANY_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValue, BType, BValue> FLOAT_TO_ANY_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValue, BType, BValue> STRING_TO_ANY_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValue, BType, BValue> BOOLEAN_TO_ANY_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValue, BType, BValue> JSON_TO_ANY_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValue, BType, BValue> XML_TO_ANY_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValue, BType, BValue> CONNECTOR_TO_ANY_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValue, BType, BValue> MAP_TO_ANY_FUNC = (rVal, targetType) -> rVal;
    
    public static final BiFunction<BValue, BType, BValue> STRUCT_TO_ANY_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValue, BType, BValue> ANY_TO_ANY_FUNC = (rVal, targetType) -> rVal;

    public static final BiFunction<BValue, BType, BValue> ANY_TO_INT_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, 
                    BTypes.typeInt);
        }
        if (rVal.getType() == BTypes.typeInt) {
            return rVal;
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
            rVal.getType(), BTypes.typeInt);
    };

    public static final BiFunction<BValue, BType, BValue> ANY_TO_FLOAT_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
                BTypes.typeFloat);
        }
        if (rVal.getType() == BTypes.typeFloat) {
            return rVal;
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
            rVal.getType(), BTypes.typeFloat);
    };

    public static final BiFunction<BValue, BType, BValue> ANY_TO_STRING_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
                BTypes.typeString);
        }
        if (rVal.getType() == BTypes.typeString) {
            return rVal;
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
            rVal.getType(), BTypes.typeString);
    };

    public static final BiFunction<BValue, BType, BValue> ANY_TO_BOOLEAN_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
                BTypes.typeBoolean);
        }
        if (rVal.getType() == BTypes.typeBoolean) {
            return rVal;
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
            rVal.getType(), BTypes.typeBoolean);
    };

    /**
     * Function to cast a given any type to a JSON.
     * each type will be converted to their corresponding JSON representations. 
     * i.e: map/struct types will be converted to JSON objects. Array-types will be converted to JSON array.
     */
    public static final BiFunction<BValue, BType, BValue> ANY_TO_JSON_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        } else if (rVal.getType() == BTypes.typeJSON) {
            return rVal;
        } else if (rVal.getType() == BTypes.typeInt) {
            return INT_TO_JSON_FUNC.apply((BValueType) rVal, targetType);
        } else if (rVal.getType() == BTypes.typeString) {
            return STRING_TO_JSON_FUNC.apply((BValueType) rVal, targetType);
        } else if (rVal.getType() == BTypes.typeFloat) {
            return FLOAT_TO_JSON_FUNC.apply((BValueType) rVal, targetType);
        } else if (rVal.getType() == BTypes.typeBoolean) {
            return BOOLEAN_TO_JSON_FUNC.apply((BValueType) rVal, targetType);
        } else if (rVal.getType() == BTypes.typeMap) {
            return MAP_TO_JSON_FUNC.apply(rVal, targetType);
        } else if (rVal.getType() instanceof StructDef) {
            return STRUCT_TO_JSON_FUNC.apply(rVal, targetType);
        } else if (rVal.getType() instanceof BArrayType) {
            return ARRAY_TO_JSON_FUNC.apply(rVal, targetType);
        }
        
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeJSON);
    };

    public static final BiFunction<BValue, BType, BValue> ANY_TO_XML_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        if (rVal.getType() == BTypes.typeXML) {
            return rVal;
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
            rVal.getType(), BTypes.typeXML);
    };

    public static final BiFunction<BValue, BType, BValue> ANY_TO_MESSAGE_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        if (rVal.getType() == BTypes.typeMessage) {
            return rVal;
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeXML);
    };

    public static final BiFunction<BValue, BType, BValue> ANY_TO_CONNECTOR_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        if (rVal.getType() == BTypes.typeConnector) {
            return rVal;
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
            rVal.getType(), BTypes.typeConnector);
    };

    public static final BiFunction<BValue, BType, BValue> ANY_TO_MAP_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        if (rVal.getType() == BTypes.typeMap) {
            return rVal;
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
            rVal.getType(), BTypes.typeMap);
    };
    
    public static final BiFunction<BValue, BType, BValue> ANY_TO_STRUCT_FUNC = (rVal, targetType) -> {
        if (rVal == null) {
            return null;
        }
        if (rVal.getType() == targetType) {
            return rVal;
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType().getSymbolName(), targetType.getSymbolName());
    };

    /**
     * Check whether a given source type can be assigned to a destination type.
     * 
     * @param lType Destination type
     * @param rType Source Type
     * @return Flag indicating whether the given source type can be assigned to the destination type.
     */
    public static boolean isCompatible(BType lType, BType rType) {
        if (lType == rType) {
            return true;
        }

        if (lType == BTypes.typeAny) {
            return true;
        }

        if (!BTypes.isValueType(lType) && (rType == BTypes.typeNull)) {
            return true;
        }

        return false;
    }
    
    private static int getStructMemoryOffset(VariableDef varDef) {
        return ((StructVarLocation) varDef.getMemoryLocation()).getStructMemAddrOffset();
    }
    
    private static void handleError(BType sourceType, BType targetType, BallerinaException e, String fieldName) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        throw new BallerinaException("cannot cast '" + sourceType + "' to type '" + targetType + "': " + 
                cause.getMessage(), cause);
    }
}
