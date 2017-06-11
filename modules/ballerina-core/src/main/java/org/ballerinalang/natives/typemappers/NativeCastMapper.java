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

import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;

/**
 * For converting across native types.
 */
public class NativeCastMapper {

    public static final TriFunction<BValue, BType, Boolean, BValue[]> JSON_TO_JSON_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };
            
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
        
    public static final TriFunction<BValue, BType, Boolean, BValue[]> MAP_TO_MAP_FUNC =
        (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };
                
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
     * Function to cast a given struct to another. The compatibility is checked during the semantic analyzer phase.
     * Therefore the same value is returned as is, maintaining its originated type ({@link StructDef}) as meta info.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> STRUCT_TO_STRUCT_SAFE_FUNC =
        (rVal, targetType, returnErrors) -> {
            return new BValue[] { rVal, null };
        };
    
    /**
     * Function to cast a given struct to another. The compatibility check is failed during the semantic analyzer phase.
     * Therefore the compatibility is checked during runtime. If it is successful, same value is returned as is, 
     * maintaining its originated type ({@link StructDef}) as meta info.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> STRUCT_TO_STRUCT_UNSAFE_FUNC =
        (rVal, targetType, returnErrors) -> {
            if (rVal == null || TypeLattice.isAssignCompatible((StructDef) targetType, (StructDef) rVal.getType())) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING,
                targetType, rVal.getType());
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
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
                String errorMsg = BLangExceptionHelper
                        .getErrorMessage(RuntimeErrors.CASTING_FAILED_WITH_CAUSE, rVal.getType(), BTypes.typeJSON,
                                         e.getMessage());
                return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
            }
        };
    
    /*
     * below functions are only needed if explicit casting happens from other types to any type.
     * ex - any n = (any)1;
     */
    
    public static final TriFunction<BValue, BType, Boolean, BValue[]> INT_TO_ANY_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> FLOAT_TO_ANY_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> STRING_TO_ANY_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> BOOLEAN_TO_ANY_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> BLOB_TO_ANY_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> JSON_TO_ANY_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> XML_TO_ANY_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };
            
    public static final TriFunction<BValue, BType, Boolean, BValue[]> CONNECTOR_TO_ANY_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> MAP_TO_ANY_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };
    
    public static final TriFunction<BValue, BType, Boolean, BValue[]> STRUCT_TO_ANY_FUNC =
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_ANY_FUNC = 
            (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

    /**
     * Function to cast a given 'any' type value to an integer.
     * This function will return the {@link BInteger} representation, if the value stored in any variable 
     * is an integer. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_INT_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                String errorMsg =
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeInt);
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeAny, targetType);
            }
            if (rVal.getType() == BTypes.typeInt) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeInt);
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
        };

    /**
     * Function to cast a given 'any' type value to a float.
     * This function will return the {@link BFloat} representation, if the value stored in any variable
     * is a float. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_FLOAT_FUNC =
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                String errorMsg =
                    BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeFloat);
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeAny, targetType);
            }
            if (rVal.getType() == BTypes.typeFloat) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeFloat);
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
        };

    /**
     * Function to cast a given 'any' type value to a string.
     * This function will return the {@link BString} representation, if the value stored in variable
     * is a string. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_STRING_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
                    BTypes.typeString);
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeAny, targetType);
            }
            if (rVal.getType() == BTypes.typeString) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeString);
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
        };

    /**
     * Function to cast a given 'any' type value to a boolean.
     * This function will return the {@link BBoolean} representation, if the value stored in variable
     * is a boolean. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_BOOLEAN_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
                    BTypes.typeBoolean);
                return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeAny, targetType);
            }
            if (rVal.getType() == BTypes.typeBoolean) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeBoolean);
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
        };

    /**
     * Function to cast a given 'any' type value to a blob.
     * This function will return the {@link BBlob} representation, if the value stored in variable
     * is a blob. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_BLOB_FUNC =
            (rVal, targetType, returnErrors) -> {
                if (rVal == null) {
                    String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT,
                                                                           BTypes.typeBlob);
                    return TypeMappingUtils.getError(returnErrors, errorMsg, BTypes.typeAny, targetType);
                }
                if (rVal.getType() == BTypes.typeBlob) {
                    return new BValue[] { rVal, null };
                }
                String errorMsg = BLangExceptionHelper
                        .getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE, rVal.getType(),
                                         BTypes.typeBlob);
                return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
            };

    /**
     * Function to cast a given 'any' type value to a JSON.
     * This function will return the {@link BJSON} representation, if the value stored in variable
     * is a JSON. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_JSON_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            if (rVal.getType() == BTypes.typeJSON) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeJSON);
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
        };

    /**
     * Function to cast a given 'any' type value to a JSON.
     * This function will return the {@link BJSON} representation, if the value stored in variable
     * is a JSON. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_MESSAGE_FUNC =
            (rVal, targetType, returnErrors) -> {
                if (rVal == null) {
                    return new BValue[] { null, null };
                }
                if (rVal.getType() == BTypes.typeMessage) {
                    return new BValue[] { rVal, null };
                }
                String errorMsg = BLangExceptionHelper.getErrorMessage
                        (RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                        rVal.getType(), BTypes.typeMessage);
                return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
            };

    /**
     * Function to cast a given 'any' type value to a XML.
     * This function will return the {@link BXML} representation, if the value stored in variable
     * is a XML. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_XML_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            if (rVal.getType() == BTypes.typeXML) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeXML);
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
        };

    /**
     * Function to cast a given 'any' type value to a connector.
     * This function will return the {@link BConnector} representation, if the value stored in variable
     * is a connector. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_CONNECTOR_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            if (rVal.getType() == BTypes.typeConnector) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeConnector);
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
        };

    /**
     * Function to cast a given 'any' type value to a map.
     * This function will return the {@link BMAP} representation, if the value stored in variable
     * is a map. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_MAP_FUNC = 
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            if (rVal.getType() == BTypes.typeMap) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType(), BTypes.typeMap);
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
        };

    /**
     * Function to cast a given 'any' type value to a message.
     * This function will return the {@link BMessage} representation, if the value stored in variable
     * is a map. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_MSG_FUNC =
            (rVal, targetType, returnErrors) -> {
                if (rVal == null) {
                    return new BValue[] { null, null };
                }
                if (rVal.getType() == BTypes.typeMessage) {
                    return new BValue[] { rVal, null };
                }
                String errorMsg = BLangExceptionHelper.getErrorMessage
                        (RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE, rVal.getType(), BTypes.typeMessage);
                return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
            };
    
    /**
     * Function to cast a given 'any' type value to a struct.
     * This function will return the {@link BStruct} representation, if the value stored in variable
     * is a struct. An error, otherwise.
     */
    public static final TriFunction<BValue, BType, Boolean, BValue[]> ANY_TO_STRUCT_FUNC =
        (rVal, targetType, returnErrors) -> {
            if (rVal == null) {
                return new BValue[] { null, null };
            }
            if (rVal.getType() == targetType) {
                return new BValue[] { rVal, null };
            }
            String errorMsg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                rVal.getType().getSymbolName(), targetType.getSymbolName());
            return TypeMappingUtils.getError(returnErrors, errorMsg, rVal.getType(), targetType);
        };

    public static final TriFunction<BValue, BType, Boolean, BValue[]> XML_TO_XML_FUNC =
        (rVal, targetType, returnErrors) -> new BValue[] { rVal, null };

}
