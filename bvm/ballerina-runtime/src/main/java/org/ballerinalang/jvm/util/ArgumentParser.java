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
package org.ballerinalang.jvm.util;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.RuntimeUtils.ParamInfo;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String DEFAULT_PARAM_DELIMITER = "=";
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
     * @param funcInfo {@link ParamInfo} array for the entry function
     * @param args     the string array of arguments specified
     * @param hasRestParam whether function accepts rest arguments
     * @return the {@link Object} array containing the arguments to invoke the function
     */
    public static Object[] extractEntryFuncArgs(ParamInfo[] funcInfo, String[] args,
                                                boolean hasRestParam) {
        // first arg is reserved for strand
        Object[] bValueArgs = new Object[funcInfo.length * 2 + 1];
        Map<String, ParamInfo> namedArgs = new HashMap<>();

        //create map of defaultable params and there indices
        int defaultableCount = 0;
        for (ParamInfo info : funcInfo) {
            if (info.hasDefaultable) {
                info.index = defaultableCount;
                namedArgs.put(info.name, info);
                defaultableCount++;
            }
        }

        int requiredParamsCount = hasRestParam ? funcInfo.length - defaultableCount - 1 :
                funcInfo.length - defaultableCount;

        // first populate defaultable values and extract rest of args
        List<String> requiredAndRestArgs = new ArrayList<>();

        int defaultableArgIndex = requiredParamsCount * 2 + 1;

        if (defaultableCount > 0) {
            // false will exist for not provided defaultable values, others will get replaced.
            Arrays.fill(bValueArgs, defaultableArgIndex, bValueArgs.length, false);
        }

        for (String arg : args) {
            if (isDefaultParamCandidate(arg) && namedArgs.containsKey(getParamName(arg))) {
                ParamInfo info = namedArgs.get(getParamName(arg));
                bValueArgs[info.index * 2 + defaultableArgIndex] = getBValue(info.type, getValueString(arg));
                bValueArgs[info.index * 2 + defaultableArgIndex + 1] = true;
            } else {
                requiredAndRestArgs.add(arg);
            }
        }

        if (requiredAndRestArgs.size() < requiredParamsCount) {
            throw BallerinaErrors.createUsageError("insufficient arguments to call the 'main' function");
        }

        if (requiredAndRestArgs.size() > requiredParamsCount && !hasRestParam) {
            throw BallerinaErrors.createUsageError("too many arguments to call the 'main' function");
        }

        // now go through required args and populate value[]
        for (int i = 0; i < requiredParamsCount; i++) {
            ParamInfo functionInfo = funcInfo[i];
            String arg = requiredAndRestArgs.get(i);
            bValueArgs[2 * i + 1] = getBValue(functionInfo.type, arg);
            bValueArgs[2 * i + 2] = true;

        }

        // populate var args
        if (hasRestParam) {
            // getVarargs() // vararg start index = requiredParamsCount
            bValueArgs[funcInfo.length * 2 - 1] = getRestArgArray(funcInfo[funcInfo.length - 1].type,
                    requiredParamsCount, requiredAndRestArgs);
            bValueArgs[funcInfo.length * 2] = true;
        }

        return bValueArgs;
    }

    private static boolean isDefaultParamCandidate(String arg) {
        return arg.startsWith(DEFAULT_PARAM_PREFIX) && arg.contains(DEFAULT_PARAM_DELIMITER);
    }

    private static String getParamName(String arg) {
        return arg.split(DEFAULT_PARAM_DELIMITER, 2)[0].substring(1).trim();
    }

    private static String getValueString(String arg) {
        return arg.split(DEFAULT_PARAM_DELIMITER, 2)[1];
    }

    public static Object getBValue(BType type, String value) {
        switch (type.getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.ANY_TAG:
                return value;
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
                    throw BallerinaErrors.createUsageError("invalid argument '" + value + "', expected XML value");
                }
            case TypeTags.JSON_TAG:
                try {
                    return JSONParser.parse(value);
                } catch (BallerinaException e) {
                    throw BallerinaErrors.createUsageError("invalid argument '" + value + "', expected JSON value");
                }
            case TypeTags.RECORD_TYPE_TAG:
                try {
                    return JSONUtils.convertJSONToRecord(JSONParser.parse(value), (BStructureType) type);
                } catch (BallerinaException e) {
                    throw BallerinaErrors.createUsageError("invalid argument '" + value
                            + "', error constructing record of type: " + type + ": "
                            + e.getLocalizedMessage().split(JSON_PARSER_ERROR)[0]);
                }
            case TypeTags.TUPLE_TAG:
                if (!value.startsWith("[") || !value.endsWith("]")) {
                    throw BallerinaErrors.createUsageError("invalid argument '"
                            + value + "', " + "expected tuple notation [\"[]\"] with tuple arg");
                }
                return parseTupleArg((BTupleType) type, value.substring(1, value.length() - 1));
            case TypeTags.ARRAY_TAG:
                try {
                    return JSONUtils.convertJSONToBArray(JSONParser.parse(value), (BArrayType) type);
                } catch (BallerinaException | ErrorValue e) {
                    throw BallerinaErrors.createUsageError("invalid argument '" + value
                            + "', expected array elements of " + "type: " + ((BArrayType) type).getElementType());
                }
            case TypeTags.MAP_TAG:
                try {
                    return JSONUtils.jsonToMap(JSONParser.parse(value), (BMapType) type);
                } catch (ErrorValue | BallerinaException e) {
                    throw BallerinaErrors.createUsageError("invalid argument '" + value
                            + "', expected map argument of element type: " + ((BMapType) type).getConstrainedType());
                }
            case TypeTags.UNION_TAG:
                return parseUnionArg((BUnionType) type, value);
            default:
                throw BallerinaErrors.createError(UNSUPPORTED_TYPE_PREFIX + " '" + type + "'");
        }
    }

    private static long getIntegerValue(String argument) {
        try {
            if (argument.toUpperCase().startsWith(HEX_PREFIX)) {
                return Long.parseLong(argument.toUpperCase().replace(HEX_PREFIX, ""), 16);
            }
            return Long.parseLong(argument);
        } catch (NumberFormatException e) {
            throw BallerinaErrors.createUsageError("invalid argument '" + argument + "', expected integer value");
        }
    }

    private static double getFloatValue(String argument) {
        try {
            return Double.parseDouble(argument);
        } catch (NumberFormatException e) {
            throw BallerinaErrors.createUsageError("invalid argument '" + argument + "', expected float value");
        }
    }

    private static DecimalValue getDecimalValue(String argument) {
        try {
            return new DecimalValue(argument);
        } catch (NumberFormatException e) {
            throw BallerinaErrors.createUsageError("invalid argument '" + argument + "', expected decimal value");
        }
    }

    private static boolean getBooleanValue(String argument) {
        if (!TRUE.equalsIgnoreCase(argument) && !FALSE.equalsIgnoreCase(argument)) {
            throw BallerinaErrors.createUsageError("invalid argument '" + argument
                    + "', expected boolean value 'true' or " + "'false'");
        }
        return Boolean.parseBoolean(argument);
    }

    private static int getByteValue(String argument) {
        int byteValue; // TODO: 7/4/18 Allow byte literals?
        try {
            byteValue = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw BallerinaErrors.createUsageError("invalid argument '" + argument + "', expected byte value");
        }

        if (!RuntimeUtils.isByteLiteral(byteValue)) {
            throw BallerinaErrors.createUsageError("invalid argument '" + argument +
                    "', expected byte value, found int");
        }

        return byteValue;
    }

    private static ArrayValue getRestArgArray(BType type, int index, List<String> args) {
        BType elementType = ((BArrayType) type).getElementType();
        try {
            switch (elementType.getTag()) {
                case TypeTags.ANY_TAG:
                case TypeTags.STRING_TAG:
                    ArrayValue stringArrayArgs = new ArrayValue(BTypes.typeString);
                    for (int i = index; i < args.size(); i++) {
                        stringArrayArgs.add(i - index, args.get(i));
                    }
                    return stringArrayArgs;
                case TypeTags.INT_TAG:
                    ArrayValue intArrayArgs = new ArrayValue(BTypes.typeInt);
                    for (int i = index; i < args.size(); i++) {
                        intArrayArgs.add(i - index, getIntegerValue(args.get(i)));
                    }
                    return intArrayArgs;
                case TypeTags.FLOAT_TAG:
                    ArrayValue floatArrayArgs = new ArrayValue(BTypes.typeFloat);
                    for (int i = index; i < args.size(); i++) {
                        floatArrayArgs.add(i - index, getFloatValue(args.get(i)));
                    }
                    return floatArrayArgs;
                case TypeTags.BOOLEAN_TAG:
                    ArrayValue booleanArrayArgs = new ArrayValue(BTypes.typeBoolean);
                    for (int i = index; i < args.size(); i++) {
                        booleanArrayArgs.add(i - index, getBooleanValue(args.get(i)) ? 1 : 0);
                    }
                    return booleanArrayArgs;
                case TypeTags.BYTE_TAG:
                    ArrayValue byteArrayArgs = new ArrayValue(BTypes.typeByte);
                    for (int i = index; i < args.size(); i++) {
                        byteArrayArgs.add(i - index, (byte) getByteValue(args.get(i)));
                    }
                    return byteArrayArgs;
                default:
                    ArrayValue refValueArray = new ArrayValue();
                    for (int i = index; i < args.size(); i++) {
                        refValueArray.add(i - index, getBValue(elementType, args.get(i)));
                    }
                    return refValueArray;
            }
        } catch (BallerinaException e) {
            throw BallerinaErrors.createError(e.getLocalizedMessage().replace(INVALID_ARG, INVALID_ARG_AS_REST_ARG));
        } catch (Exception e) {
            //Ideally shouldn't reach here
            throw BallerinaErrors.createUsageError("error parsing rest arg: " + e.getLocalizedMessage());
        }
    }

    private static ArrayValue parseTupleArg(BTupleType type, String tupleArg) {
        String stringSpecificationErrorSuffix = "', expected argument in the format \\\"str\\\" for tuple element of "
                + "type 'string'";
        String[] tupleElements = tupleArg.split(COMMA);

        if (tupleElements.length != type.getTupleTypes().size()) {
            throw BallerinaErrors.createUsageError("invalid argument '[" + tupleArg
                    + "]', element count mismatch for tuple " + "type: '" + type + "'");
        }

        ArrayValue tupleValues = new ArrayValue(type);
        int index = 0;
        for (BType elementType : type.getTupleTypes()) {
            String tupleElement = tupleElements[index].trim();
            try {
                if (elementType.getTag() == TypeTags.STRING_TAG) {
                    if (!tupleElement.startsWith("\"") || !tupleElement.endsWith("\"")) {
                        throw BallerinaErrors.createUsageError("invalid tuple element argument '" + tupleElement
                                + stringSpecificationErrorSuffix);
                    }
                    tupleElement = tupleElement.substring(1, tupleElement.length() - 1);
                }
                tupleValues.add(index, getBValue(elementType, tupleElement));
                index++;
            } catch (BallerinaException | ErrorValue e) {
                String localizedMessage = e.getLocalizedMessage();
                if (localizedMessage.startsWith(UNSUPPORTED_TYPE_PREFIX)) {
                    throw BallerinaErrors.createUsageError(
                            "unsupported element type for tuple as entry function argument: " + elementType);
                } else if (!localizedMessage.endsWith(stringSpecificationErrorSuffix)) {
                    throw BallerinaErrors.createUsageError("invalid tuple member argument '" + tupleElement + "', "
                            + "expected value of type '" + elementType + "'");
                }
                throw e;
            }
        }
        return tupleValues;
    }

    private static Object parseUnionArg(BUnionType type, String unionArg) {
        List<BType> unionMemberTypes = type.getMemberTypes();

        if (unionMemberTypes.contains(BTypes.typeNull) && NIL.equals(unionArg)) {
            return null;
        }

        if (unionMemberTypes.contains(BTypes.typeString)) {
            return unionArg;
        }

        for (int memberTypeIndex = 0; memberTypeIndex < unionMemberTypes.size(); ) {
            try {
                BType memberType = unionMemberTypes.get(memberTypeIndex);
                if (memberType.getTag() == TypeTags.NULL_TAG) {
                    memberTypeIndex++;
                    continue;
                }
                return getBValue(memberType, unionArg);
            } catch (ErrorValue e) {
                memberTypeIndex++;
            }
        }
        throw BallerinaErrors.createUsageError("invalid argument '" + unionArg + "' specified for union type: "
                + (type.isNilable() ? type.toString().replace("|null", "|()") : type));
    }
}
