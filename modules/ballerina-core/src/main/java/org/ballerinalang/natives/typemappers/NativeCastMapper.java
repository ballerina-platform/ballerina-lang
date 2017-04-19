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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.natives.typemappers;

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import java.util.function.Function;

/**
 * Type mapper for converting across native types.
 */
public class NativeCastMapper {

    public static final Function<BValueType, BValueType> INT_TO_FLOAT_FUNC =
            (rVal) -> new BFloat(rVal.floatValue());

    public static final Function<BValueType, BValueType> INT_TO_STRING_FUNC =
            (rVal) -> new BString(rVal.stringValue());

    public static final Function<BValueType, BValueType> INT_TO_BOOLEAN_FUNC =
            (rVal) -> new BBoolean(rVal.intValue() != 0);

    public static final Function<BValueType, BValueType> INT_TO_INT_FUNC =
            (rVal) -> rVal;

    public static final Function<BValueType, BValueType> FLOAT_TO_STRING_FUNC =
            (rVal) -> new BString(rVal.stringValue());

    public static final Function<BValueType, BValueType> FLOAT_TO_BOOLEAN_FUNC =
            (rVal) -> new BBoolean(rVal.floatValue() != 0.0);

    public static final Function<BValueType, BValueType> FLOAT_TO_FLOAT_FUNC =
            (rVal) -> rVal;

    public static final Function<BValueType, BValueType> FLOAT_TO_INT_FUNC =
            (rVal) -> new BInteger(rVal.intValue());

    public static final Function<BValueType, BValueType> STRING_TO_INT_FUNC =
            (rVal) -> new BInteger(rVal.intValue());

    public static final Function<BValueType, BValueType> STRING_TO_FLOAT_FUNC =
            (rVal) -> new BFloat(rVal.floatValue());

    public static final Function<BValueType, BValueType> STRING_TO_BOOLEAN_FUNC =
            (rVal) -> new BBoolean(rVal.booleanValue());

    public static final Function<BValueType, BValueType> STRING_TO_STRING_FUNC =
            (rVal) -> rVal;

    public static final Function<BValueType, BValueType> BOOLEAN_TO_STRING_FUNC =
            (rVal) -> new BString(rVal.stringValue());

    public static final Function<BValueType, BValueType> BOOLEAN_TO_INT_FUNC =
            (rVal) -> rVal.booleanValue() ? new BInteger(1) : new BInteger(0);

    public static final Function<BValueType, BValueType> BOOLEAN_TO_FLOAT_FUNC =
            (rVal) -> rVal.booleanValue() ? new BFloat(1.0f) : new BFloat(0.0f);

    public static final Function<BValueType, BValueType> BOOLEAN_TO_BOOLEAN_FUNC =
            (rVal) -> rVal;

    public static final Function<BValueType, BValueType> JSON_TO_STRING_FUNC =
            (rVal) -> new BString(rVal.stringValue());

    public static final Function<BValueType, BValueType> XML_TO_STRING_FUNC =
            (rVal) -> new BString(rVal.stringValue());

    /**
     * below functions are only needed if explicit casting happens from other types to any type.
     * ex - any n = (any)1;
     */
    public static final Function<BValue, BValue> INT_TO_ANY_FUNC =
            (rVal) -> rVal;

    public static final Function<BValue, BValue> FLOAT_TO_ANY_FUNC =
            (rVal) -> rVal;

    public static final Function<BValue, BValue> STRING_TO_ANY_FUNC =
            (rVal) -> rVal;

    public static final Function<BValue, BValue> BOOLEAN_TO_ANY_FUNC =
            (rVal) -> rVal;

    public static final Function<BValue, BValue> JSON_TO_ANY_FUNC =
            (rVal) -> rVal;

    public static final Function<BValue, BValue> XML_TO_ANY_FUNC =
            (rVal) -> rVal;

    public static final Function<BValue, BValue> CONNECTOR_TO_ANY_FUNC =
            (rVal) -> rVal;

    public static final Function<BValue, BValue> EXCEPTION_TO_ANY_FUNC =
            (rVal) -> rVal;

    public static final Function<BValue, BValue> ANY_TO_ANY_FUNC =
            (rVal) -> rVal;

    public static final Function<BValue, BValue> ANY_TO_INT_FUNC =
            (rVal) -> {
                if (rVal == null) {
                    throw BLangExceptionHelper
                            .getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeInt);
                }
                if (rVal.getType() == BTypes.typeInt) {
                    return rVal;
                }
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                                                         rVal.getType(), BTypes.typeInt);
            };

    public static final Function<BValue, BValue> ANY_TO_FLOAT_FUNC =
            (rVal) -> {
                if (rVal == null) {
                    throw BLangExceptionHelper
                            .getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeFloat);
                }
                if (rVal.getType() == BTypes.typeFloat) {
                    return rVal;
                }
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                                                         rVal.getType(), BTypes.typeFloat);
            };

    public static final Function<BValue, BValue> ANY_TO_STRING_FUNC =
            (rVal) -> {
                if (rVal == null) {
                    throw BLangExceptionHelper
                            .getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeString);
                }
                if (rVal.getType() == BTypes.typeString) {
                    return rVal;
                }
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                                                         rVal.getType(), BTypes.typeString);
            };

    public static final Function<BValue, BValue> ANY_TO_BOOLEAN_FUNC =
            (rVal) -> {
                if (rVal == null) {
                    throw BLangExceptionHelper
                            .getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeBoolean);
                }
                if (rVal.getType() == BTypes.typeBoolean) {
                    return rVal;
                }
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                                                         rVal.getType(), BTypes.typeBoolean);
            };

    public static final Function<BValue, BValue> ANY_TO_JSON_FUNC =
            (rVal) -> {
                if (rVal == null) {
                    throw BLangExceptionHelper
                            .getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeJSON);
                }
                if (rVal.getType() == BTypes.typeJSON) {
                    return rVal;
                }
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                                                         rVal.getType(), BTypes.typeJSON);
            };

    public static final Function<BValue, BValue> ANY_TO_XML_FUNC =
            (rVal) -> {
                if (rVal == null) {
                    throw BLangExceptionHelper
                            .getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeXML);
                }
                if (rVal.getType() == BTypes.typeXML) {
                    return rVal;
                }
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                                                         rVal.getType(), BTypes.typeXML);
            };

    public static final Function<BValue, BValue> ANY_TO_CONNECTOR_FUNC =
            (rVal) -> {
                if (rVal == null) {
                    throw BLangExceptionHelper
                            .getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeConnector);
                }
                if (rVal.getType() == BTypes.typeConnector) {
                    return rVal;
                }
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                                                         rVal.getType(), BTypes.typeConnector);
            };

    public static final Function<BValue, BValue> ANY_TO_EXCEPTION_FUNC =
            (rVal) -> {
                if (rVal == null) {
                    throw BLangExceptionHelper
                            .getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_WITHOUT_INIT, BTypes.typeException);
                }
                if (rVal.getType() == BTypes.typeException) {
                    return rVal;
                }
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.CASTING_ANY_TYPE_TO_WRONG_VALUE_TYPE,
                                                         rVal.getType(), BTypes.typeException);
            };

}
