/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.util;

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.JSONUtils;
import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.types.BArrayType;
import io.ballerina.runtime.types.BMapType;
import io.ballerina.runtime.types.BStructureType;
import io.ballerina.runtime.types.BTupleType;
import io.ballerina.runtime.types.BUnionType;
import io.ballerina.runtime.util.RuntimeUtils.ParamInfo;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ArrayValueImpl;
import io.ballerina.runtime.values.DecimalValue;
import io.ballerina.runtime.values.ErrorValue;
import io.ballerina.runtime.values.TupleValueImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Argument Parser class used to parse function args specified on the CLI.
 *
 * @since 0.995.0
 */
public class ArgumentParser {

    private static final String DEFAULT_PARAM_PREFIX = "-";
    private static final String NAMED_ARG_DELIMITER = "=";
    private static final String INVALID_ARG = "invalid argument: ";
    private static final String INVALID_ARG_AS_REST_ARG = "invalid argument as rest argument: ";
    private static final String UNSUPPORTED_TYPE_PREFIX = "unsupported type expected with entry function";
    private static final String JSON_PARSER_ERROR = "at line: ";
    private static final String COMMA = ",";

    private static final String NIL = "()";
    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";
    private static final String HEX_PREFIX = "0X";

    /**
     * Method to retrieve the {@link Object} array containing the arguments to invoke the function specified as the
     * entry function. First element is ignored to keep the strand.
     *
     * @param funcInfo     {@link ParamInfo} array for the entry function
     * @param args         the string array of arguments specified
     * @param hasRestParam whether function accepts rest arguments
     * @return the {@link Object} array containing the arguments to invoke the function
     */
    public static Object[] extractEntryFuncArgs(ParamInfo[] funcInfo, String[] args,
                                                boolean hasRestParam) {
        Object[] bValueArgs = null;
        try {
            bValueArgs = getEntryFuncArgs(funcInfo, args, hasRestParam);
        } catch (ErrorValue e) {
            RuntimeUtils.handleUsageError(e.getMessage());
        }
        return bValueArgs;
    }

    private static Object[] getEntryFuncArgs(ParamInfo[] funcInfo, String[] args,
                                             boolean hasRestParam) {
        // first arg is reserved for strand
        Object[] bValueArgs = new Object[funcInfo.length * 2 + 1];
        Map<String, ParamInfo> namedArgs = new HashMap<>();
        boolean isNamedArgFound = false;

        //create map of default params and there indices
        int defaultableCount = 0;
        for (int i = 0; i < funcInfo.length; i++) {
            ParamInfo info = funcInfo[i];
            info.index = i;
            namedArgs.put(info.name, info);
            if (info.hasDefaultable) {
                defaultableCount++;
            }
        }
        int argsCountExceptRestArgs = hasRestParam ? funcInfo.length - 1 : funcInfo.length;
        int requiredParamsCount = hasRestParam ? argsCountExceptRestArgs - defaultableCount :
                argsCountExceptRestArgs - defaultableCount;

        // populate positional args and named args
        List<String> restArgs = new ArrayList<>();
        int providedRequiredArgsCount = 0;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            boolean isNameArg = isNameArg(arg);
            // handle named args
            if (isNameArg) {
                isNamedArgFound = true;
                String paramName = getParamName(arg);
                ParamInfo info = namedArgs.get(paramName);
                if (info == null) {
                    throw ErrorCreator.createError(
                            StringUtils.fromString("undefined parameter: '" + paramName + "'"));
                }
                bValueArgs[info.index * 2 + 1] = getBValue(info.type, getValueString(arg));
                bValueArgs[info.index * 2 + 2] = true;
                if (!info.hasDefaultable) {
                    providedRequiredArgsCount++;
                }
            } else {
                if (isNamedArgFound) {
                    throw ErrorCreator.createError(StringUtils.fromString(
                            "positional argument not allowed after named arguments when " +
                                    "calling the 'main' function"));
                }
                // handle positional args
                if (i < argsCountExceptRestArgs) {
                    ParamInfo info = funcInfo[i];
                    bValueArgs[2 * i + 1] = getBValue(info.type, arg);
                    bValueArgs[2 * i + 2] = true;
                    if (!info.hasDefaultable) {
                        providedRequiredArgsCount++;
                    }
                } else {
                    // if arg is not a positional or name arg, it will be considered as rest arg.
                    restArgs.add(arg);
                }
            }
        }

        if (providedRequiredArgsCount < requiredParamsCount) {
            throw ErrorCreator.createError(StringUtils.fromString("insufficient arguments to call the 'main' " +
                                                                           "function"));
        }

        if (!hasRestParam && !restArgs.isEmpty()) {
            throw ErrorCreator.createError(StringUtils.fromString("too many arguments to call the 'main' function"));
        }

        // populate var args
        if (hasRestParam) {
            bValueArgs[funcInfo.length * 2 - 1] = getRestArgArray(funcInfo[funcInfo.length - 1].type, restArgs);
            bValueArgs[funcInfo.length * 2] = true;
        }

        // handle default values which does not have any arg value.
        for (int i = requiredParamsCount; i < argsCountExceptRestArgs; i++) {
            int defaultableArgIndex = i * 2 + 1;
            if (bValueArgs[defaultableArgIndex + 1] == null) {
                bValueArgs[defaultableArgIndex + 1] = false;
                bValueArgs[defaultableArgIndex] = getDefaultBValue(funcInfo[i].type);
            }
        }
        return bValueArgs;
    }

    private static boolean isNameArg(String arg) {
        return arg.startsWith(DEFAULT_PARAM_PREFIX) && arg.contains(NAMED_ARG_DELIMITER);
    }

    private static String getParamName(String arg) {
        return arg.split(NAMED_ARG_DELIMITER, 2)[0].substring(1).trim();
    }

    private static String getValueString(String arg) {
        return arg.split(NAMED_ARG_DELIMITER, 2)[1];
    }

    private static Object getBValue(Type type, String value) {
        switch (type.getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.ANY_TAG:
                return StringUtils.fromString(value);
            case TypeTags.INT_TAG:
                return getIntegerValue(value);
            case TypeTags.FLOAT_TAG:
                return getFloatValue(value);
            case TypeTags.DECIMAL_TAG:
                return getDecimalValue(value);
            case TypeTags.BOOLEAN_TAG:
                return getBooleanValue(value);
            case TypeTags.BYTE_TAG:
                return getByteValue(value);
            case TypeTags.XML_TAG:
                try {
                    return XMLFactory.parse(value);
                } catch (RuntimeException e) {
                    throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + value + "', " +
                                                                                   "expected XML value"));
                }
            case TypeTags.JSON_TAG:
                try {
                    return JSONParser.parse(value);
                } catch (BallerinaException e) {
                    throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + value + "', " +
                                                                                   "expected JSON value"));
                }
            case TypeTags.RECORD_TYPE_TAG:
                try {
                    return JSONUtils.convertJSONToRecord(JSONParser.parse(value), (BStructureType) type);
                } catch (BallerinaException e) {
                    throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + value
                            + "', error constructing record of type: " + type + ": "
                            + e.getLocalizedMessage().split(JSON_PARSER_ERROR)[0]));
                }
            case TypeTags.TUPLE_TAG:
                if (!value.startsWith("[") || !value.endsWith("]")) {
                    throw ErrorCreator.createError(StringUtils.fromString("invalid argument '"
                            + value + "', " + "expected tuple notation [\"[]\"] with tuple arg"));
                }
                return parseTupleArg((BTupleType) type, value.substring(1, value.length() - 1));
            case TypeTags.ARRAY_TAG:
                try {
                    return JSONUtils.convertJSONToBArray(JSONParser.parse(value), (BArrayType) type);
                } catch (BallerinaException | ErrorValue e) {
                    throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + value
                            + "', expected array elements of " + "type: " + ((BArrayType) type).getElementType()));
                }
            case TypeTags.MAP_TAG:
                try {
                    return JSONUtils.jsonToMap(JSONParser.parse(value), (BMapType) type);
                } catch (ErrorValue | BallerinaException e) {
                    throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + value
                            + "', expected map argument of element type: " + ((BMapType) type).getConstrainedType()));
                }
            case TypeTags.UNION_TAG:
                return parseUnionArg((BUnionType) type, value);
            default:
                throw ErrorCreator.createError(StringUtils.fromString(UNSUPPORTED_TYPE_PREFIX + " '" + type + "'"));
        }
    }

    private static Object getDefaultBValue(Type type) {
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.BYTE_TAG:
                return 0;
            case TypeTags.BOOLEAN_TAG:
                return false;
            default:
                return null;
        }
    }
    
    private static long getIntegerValue(String argument) {
        try {
            if (argument.toUpperCase().startsWith(HEX_PREFIX)) {
                return Long.parseLong(argument.toUpperCase().replace(HEX_PREFIX, ""), 16);
            }
            return Long.parseLong(argument);
        } catch (NumberFormatException e) {
            throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + argument + "', expected " +
                                                                           "integer value"));
        }
    }

    private static double getFloatValue(String argument) {
        try {
            return Double.parseDouble(argument);
        } catch (NumberFormatException e) {
            throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + argument + "', expected " +
                                                                           "float value"));
        }
    }

    private static DecimalValue getDecimalValue(String argument) {
        try {
            return new DecimalValue(argument);
        } catch (NumberFormatException e) {
            throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + argument + "', expected " +
                                                                           "decimal value"));
        }
    }

    private static boolean getBooleanValue(String argument) {
        if (!TRUE.equalsIgnoreCase(argument) && !FALSE.equalsIgnoreCase(argument)) {
            throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + argument
                    + "', expected boolean value 'true' or " + "'false'"));
        }
        return Boolean.parseBoolean(argument);
    }

    private static int getByteValue(String argument) {
        int byteValue; // TODO: 7/4/18 Allow byte literals?
        try {
            byteValue = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + argument + "', expected " +
                                                                           "byte value"));
        }

        if (!RuntimeUtils.isByteLiteral(byteValue)) {
            throw ErrorCreator.createError(StringUtils.fromString("invalid argument '" + argument +
                    "', expected byte value, found int"));
        }

        return byteValue;
    }

    private static ArrayValue getRestArgArray(Type type, List<String> args) {
        Type elementType = ((BArrayType) type).getElementType();
        try {
            switch (elementType.getTag()) {
                case TypeTags.ANY_TAG:
                case TypeTags.STRING_TAG:
                    ArrayValue stringArrayArgs = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_STRING));
                    for (int i = 0; i < args.size(); i++) {
                        stringArrayArgs.add(i, args.get(i));
                    }
                    return stringArrayArgs;
                case TypeTags.INT_TAG:
                    ArrayValue intArrayArgs = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_INT));
                    for (int i = 0; i < args.size(); i++) {
                        intArrayArgs.add(i, getIntegerValue(args.get(i)));
                    }
                    return intArrayArgs;
                case TypeTags.FLOAT_TAG:
                    ArrayValue floatArrayArgs = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_FLOAT));
                    for (int i = 0; i < args.size(); i++) {
                        floatArrayArgs.add(i, getFloatValue(args.get(i)));
                    }
                    return floatArrayArgs;
                case TypeTags.BOOLEAN_TAG:
                    ArrayValue booleanArrayArgs = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_BOOLEAN));
                    for (int i = 0; i < args.size(); i++) {
                        booleanArrayArgs.add(i, getBooleanValue(args.get(i)) ? 1 : 0);
                    }
                    return booleanArrayArgs;
                case TypeTags.BYTE_TAG:
                    ArrayValue byteArrayArgs = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_BYTE));
                    for (int i = 0; i < args.size(); i++) {
                        byteArrayArgs.add(i, (byte) getByteValue(args.get(i)));
                    }
                    return byteArrayArgs;
                default:
                    ArrayValue refValueArray = new ArrayValueImpl((BArrayType) type);
                    for (int i = 0; i < args.size(); i++) {
                        refValueArray.add(i, getBValue(elementType, args.get(i)));
                    }
                    return refValueArray;
            }
        } catch (BallerinaException e) {
            throw ErrorCreator
                    .createError(StringUtils.fromString(e.getLocalizedMessage().replace(INVALID_ARG,
                                                                                        INVALID_ARG_AS_REST_ARG)));
        } catch (Exception e) {
            //Ideally shouldn't reach here
            throw ErrorCreator
                    .createError(StringUtils.fromString("error parsing rest arg: " + e.getLocalizedMessage()));
        }
    }

    private static ArrayValue parseTupleArg(BTupleType type, String tupleArg) {
        String stringSpecificationErrorSuffix = "', expected argument in the format \\\"str\\\" for tuple element of "
                + "type 'string'";
        String[] tupleElements = tupleArg.split(COMMA);

        if (tupleElements.length != type.getTupleTypes().size()) {
            throw ErrorCreator.createError(StringUtils.fromString("invalid argument '[" + tupleArg
                    + "]', element count mismatch for tuple " + "type: '" + type + "'"));
        }

        ArrayValue tupleValues = new TupleValueImpl(type);
        int index = 0;
        for (Type elementType : type.getTupleTypes()) {
            String tupleElement = tupleElements[index].trim();
            try {
                if (elementType.getTag() == TypeTags.STRING_TAG) {
                    if (!tupleElement.startsWith("\"") || !tupleElement.endsWith("\"")) {
                        throw ErrorCreator.createError(
                                StringUtils.fromString("invalid tuple element argument '" + tupleElement
                                + stringSpecificationErrorSuffix));
                    }
                    tupleElement = tupleElement.substring(1, tupleElement.length() - 1);
                }
                tupleValues.add(index, getBValue(elementType, tupleElement));
                index++;
            } catch (BallerinaException | ErrorValue e) {
                String localizedMessage = e.getLocalizedMessage();
                if (localizedMessage.startsWith(UNSUPPORTED_TYPE_PREFIX)) {
                    throw ErrorCreator.createError(StringUtils.fromString(
                            "unsupported element type for tuple as entry function argument: " + elementType));
                } else if (!localizedMessage.endsWith(stringSpecificationErrorSuffix)) {
                    throw ErrorCreator
                            .createError(
                                    StringUtils.fromString("invalid tuple member argument '" + tupleElement + "', "
                                                                        + "expected value of type '" + elementType +
                                                                        "'"));
                }
                throw e;
            }
        }
        return tupleValues;
    }

    private static Object parseUnionArg(BUnionType type, String unionArg) {
        List<Type> unionMemberTypes = type.getMemberTypes();

        if (unionMemberTypes.contains(PredefinedTypes.TYPE_NULL) && NIL.equals(unionArg)) {
            return null;
        }

        if (unionMemberTypes.contains(PredefinedTypes.TYPE_STRING)) {
            return getBValue(PredefinedTypes.TYPE_STRING, unionArg);
        }

        for (int memberTypeIndex = 0; memberTypeIndex < unionMemberTypes.size(); ) {
            try {
                Type memberType = unionMemberTypes.get(memberTypeIndex);
                if (memberType.getTag() == TypeTags.NULL_TAG) {
                    memberTypeIndex++;
                    continue;
                }
                return getBValue(memberType, unionArg);
            } catch (ErrorValue e) {
                memberTypeIndex++;
            }
        }
        throw ErrorCreator.createError(
                StringUtils.fromString("invalid argument '" + unionArg + "' specified for union type: "
                + (type.isNilable() ? type.toString().replace("|null", "|()") : type)));
    }
}
