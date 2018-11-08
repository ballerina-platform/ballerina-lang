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
package org.ballerinalang.util.cli;

import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BDecimalArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.DefaultValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.LocalVariableInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParamDefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParameterAttributeInfo;
import org.ballerinalang.util.codegen.attributes.TaintTableAttributeInfo;
import org.ballerinalang.util.exceptions.BLangUsageException;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.model.types.BTypes.getTypeFromName;
import static org.ballerinalang.util.BLangConstants.MAIN_FUNCTION_NAME;
import static org.ballerinalang.util.codegen.attributes.AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE;
import static org.ballerinalang.util.codegen.attributes.AttributeInfo.Kind.PARAMETERS_ATTRIBUTE;
import static org.ballerinalang.util.codegen.attributes.AttributeInfo.Kind.PARAMETER_DEFAULTS_ATTRIBUTE;
import static org.ballerinalang.util.codegen.attributes.AttributeInfo.Kind.TAINT_TABLE;

/**
 * Argument Parser class used to parse function args specified on the CLI.
 *
 * @since 0.982.0
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
    private static final String BINARY_PREFIX = "0B";
    private static final String HEX_PREFIX = "0X";

    /**
     * Method to retrieve the {@link BValue} array containing the arguments to invoke the function specified as the
     * entry function.
     *
     * @param entryFuncInfo {@link FunctionInfo} for the entry function
     * @param args          the string array of arguments specified
     * @return  the {@link BValue} array containing the arguments to invoke the function
     */
    public static BValue[] extractEntryFuncArgs(FunctionInfo entryFuncInfo, String[] args) {
        BType[] paramTypes = entryFuncInfo.getParamTypes();
        BValue[] bValueArgs = new BValue[paramTypes.length];

        ParameterAttributeInfo parameterAttributeInfo =
                (ParameterAttributeInfo) entryFuncInfo.getAttributeInfo(PARAMETERS_ATTRIBUTE);
        int requiredParamsCount = parameterAttributeInfo.requiredParamsCount;
        int defaultableParamsCount = parameterAttributeInfo.defaultableParamsCount;
        int restParamCount = parameterAttributeInfo.restParamCount;

        TaintTableAttributeInfo taintTableAttributeInfo =
                (TaintTableAttributeInfo) entryFuncInfo.getAttributeInfo(TAINT_TABLE);
        int totalParamCount = requiredParamsCount + defaultableParamsCount + restParamCount;
        if (!MAIN_FUNCTION_NAME.equals(entryFuncInfo.getName())
                && totalParamCount != 0 && taintTableAttributeInfo.rowCount - 1 != totalParamCount) {
            throw new BLangUsageException("function with sensitive parameters cannot be invoked as the entry function");
        }

        LocalVariableAttributeInfo localVariableAttributeInfo =
                (LocalVariableAttributeInfo) entryFuncInfo.getAttributeInfo(LOCAL_VARIABLES_ATTRIBUTE);
        ParamDefaultValueAttributeInfo paramDefaultValueAttributeInfo =
                (ParamDefaultValueAttributeInfo) entryFuncInfo.getAttributeInfo(PARAMETER_DEFAULTS_ATTRIBUTE);

        String[] requiredAndRestArgs = args;

        // if defaultable params exist, populate values specified as named args first, and retrieve the remaining args
        // (if any) specified for required params and the rest param
        if (defaultableParamsCount > 0) {
            requiredAndRestArgs = populateNamedArgsAndRetrieveRequiredAndRestArgs(args, bValueArgs,
                                                                                  localVariableAttributeInfo,
                                                                                  paramDefaultValueAttributeInfo,
                                                                                  requiredParamsCount,
                                                                                  defaultableParamsCount);
        }

        if (requiredAndRestArgs.length < requiredParamsCount) {
            throw new BLangUsageException("insufficient arguments to call entry function '" + entryFuncInfo.getName()
                                                  + "'");
        }

        if (requiredAndRestArgs.length > requiredParamsCount && restParamCount == 0) {
            throw new BLangUsageException("too many arguments to call entry function '" + entryFuncInfo.getName()
                                                  + "'");
        }

        // populate values specified for required params
        for (int index = 0; index < requiredParamsCount; index++) {
            bValueArgs[index] = getBValue(paramTypes[index], requiredAndRestArgs[index]);
        }

        // populate values specified for the rest param
        if (restParamCount == 1) {
            bValueArgs[paramTypes.length - 1] = getRestArgArray(paramTypes[paramTypes.length - 1],
                                                                paramTypes.length - 1 - defaultableParamsCount,
                                                                requiredAndRestArgs);
        }
        return bValueArgs;
    }

    /**
     * Populate the value array with values for defaultable parameters and retrieve the modified argument list with
     * the values specified for defaultable parameters.
     *
     * This method would return the array of values specified for required params and a rest param.
     *
     * @param args                              the unmodified arg array
     * @param bValueArgs                        the {@link BValue} array containing the arguments to invoke the function
     * @param localVariableAttributeInfo        attribute info for the function parameters
     * @param paramDefaultValueAttributeInfo    default value information for defaultable params
     * @param requiredParamsCount               the number of required parameters of the function
     * @param defaultableParamsCount            the number of defaultable parameters of the function
     * @return  the argument array after values specified for defaultable parameters are removed
     */
    private static String[] populateNamedArgsAndRetrieveRequiredAndRestArgs(String[] args, BValue[] bValueArgs,
                                                        LocalVariableAttributeInfo localVariableAttributeInfo,
                                                        ParamDefaultValueAttributeInfo paramDefaultValueAttributeInfo,
                                                        int requiredParamsCount, int defaultableParamsCount) {
        Map<String, Integer> defaultableParamIndices = new HashMap<>();
        List<LocalVariableInfo> localVariableInfoList = localVariableAttributeInfo.getLocalVariables();

        populateDefaultValues(bValueArgs, localVariableInfoList, paramDefaultValueAttributeInfo, requiredParamsCount,
                              defaultableParamsCount, defaultableParamIndices);

        List<String> requiredAndRestArgs = new ArrayList<>();
        for (String arg : args) {
            if (isDefaultParamCandidate(arg) && defaultableParamIndices.containsKey(getParamName(arg))) {
                int index = defaultableParamIndices.get(getParamName(arg));
                bValueArgs[index] = getBValue(localVariableInfoList.get(index).getVariableType(), getValueString(arg));
            } else {
                requiredAndRestArgs.add(arg);
            }
        }
        return requiredAndRestArgs.toArray(new String[0]);
    }

    private static void populateDefaultValues(BValue[] bValueArgs,
                                              List<LocalVariableInfo> localVariableInfoList,
                                              ParamDefaultValueAttributeInfo paramDefaultValueAttributeInfo,
                                              int requiredParamsCount,
                                              int defaultableParamsCount,
                                              Map<String, Integer> defaultableParamIndices) {
        DefaultValue[] defaultValues = paramDefaultValueAttributeInfo.getDefaultValueInfo();
        int defaultValueIndex = 0;

        for (int defaultableParamIndex = requiredParamsCount;
             defaultableParamIndex < requiredParamsCount + defaultableParamsCount; defaultableParamIndex++) {
            LocalVariableInfo localVariableInfo = localVariableInfoList.get(defaultableParamIndex);
            bValueArgs[defaultableParamIndex] = getDefaultValue(localVariableInfo.getVariableType(),
                                                                defaultValues[defaultValueIndex++]);
            defaultableParamIndices.put(localVariableInfo.getVariableName(), defaultableParamIndex);
        }
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

    private static BValue getDefaultValue(BType type, DefaultValue value) {
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                return new BInteger(value.getIntValue());
            case TypeTags.STRING_TAG:
                return new BString(value.getStringValue());
            case TypeTags.FLOAT_TAG:
                return new BFloat(value.getFloatValue());
            case TypeTags.DECIMAL_TAG:
                return new BDecimal(value.getDecimalValue());
            case TypeTags.BOOLEAN_TAG:
                return new BBoolean(value.getBooleanValue());
            case TypeTags.BYTE_TAG:
                return new BByte(value.getByteValue());
            default: //shouldn't reach here
                throw new BLangUsageException("unsupported type specified as defaultable param: " + type);
        }
    }

    private static BValue getBValue(BType type, String value) {
        switch (type.getTag()) {
            case TypeTags.STRING_TAG:
            case TypeTags.ANY_TAG:
                return new BString(value);
            case TypeTags.INT_TAG:
                return new BInteger(getIntegerValue(value));
            case TypeTags.FLOAT_TAG:
                return new BFloat(getFloatValue(value));
            case TypeTags.DECIMAL_TAG:
                return new BDecimal(getDecimalValue(value));
            case TypeTags.BOOLEAN_TAG:
                return new BBoolean(getBooleanValue(value));
            case TypeTags.BYTE_TAG:
                return new BByte(getByteValue(value));
            case TypeTags.XML_TAG:
                try {
                    return XMLUtils.parse(value);
                } catch (BallerinaException e) {
                    throw new BLangUsageException("invalid argument '" + value + "', expected XML value");
                }
            case TypeTags.JSON_TAG:
                try {
                    return JsonParser.parse(value);
                } catch (BallerinaException e) {
                    throw new BLangUsageException("invalid argument '" + value + "', expected JSON value");
                }
            case TypeTags.RECORD_TYPE_TAG:
                try {
                    return JSONUtils.convertJSONToStruct(JsonParser.parse(value), (BStructureType) type);
                } catch (BallerinaException e) {
                    throw new BLangUsageException("invalid argument '" + value + "', error constructing record of"
                                      + " type: " + type + ": " + e.getLocalizedMessage().split(JSON_PARSER_ERROR)[0]);
                }
            case TypeTags.TUPLE_TAG:
                if (!value.startsWith("(") || !value.endsWith(")")) {
                    throw new BLangUsageException("invalid argument '" + value + "', expected tuple notation (\"()\") "
                                                          + "with tuple arg");
                }
                return parseTupleArg((BTupleType) type, value.substring(1, value.length() - 1));
            case TypeTags.ARRAY_TAG:
                try {
                    return JSONUtils.convertJSONToBArray(JsonParser.parse(value), (BArrayType) type);
                } catch (BallerinaException e) {
                    throw new BLangUsageException("invalid argument '" + value + "', expected array elements of "
                                                          + "type: " + ((BArrayType) type).getElementType());
                }
            case TypeTags.MAP_TAG:
                try {
                    return JSONUtils.jsonToBMap(JsonParser.parse(value), (BMapType) type);
                } catch (BallerinaException e) {
                    throw new BLangUsageException("invalid argument '" + value + "', expected map argument of element"
                                                          + " type: " + ((BMapType) type).getConstrainedType());
                }
            case TypeTags.UNION_TAG:
                return parseUnionArg((BUnionType) type, value);
            case TypeTags.TYPEDESC_TAG:
                try {
                    return new BTypeDescValue(getTypeFromName(value));
                } catch (IllegalStateException e) {
                    throw new BLangUsageException("invalid argument '" + value + "', unsupported/unknown typedesc "
                                                          + "expected with entry function");
                }
            default:
                throw new BLangUsageException(UNSUPPORTED_TYPE_PREFIX + " '" + type + "'");
        }
    }

    private static long getIntegerValue(String argument) {
        try {
            if (argument.toUpperCase().startsWith(BINARY_PREFIX)) {
                return Long.parseLong(argument.toUpperCase().replace(BINARY_PREFIX, ""), 2);
            } else if (argument.toUpperCase().startsWith(HEX_PREFIX)) {
                return Long.parseLong(argument.toUpperCase().replace(HEX_PREFIX, ""), 16);
            }
            return Long.parseLong(argument);
        } catch (NumberFormatException e) {
            throw new BLangUsageException("invalid argument '" + argument + "', expected integer value");
        }
    }

    private static double getFloatValue(String argument) {
        try {
            return Double.parseDouble(argument);
        } catch (NumberFormatException e) {
            throw new BLangUsageException("invalid argument '" + argument + "', expected float value");
        }
    }

    private static BigDecimal getDecimalValue(String argument) {
        try {
            return new BigDecimal(argument, MathContext.DECIMAL128);
        } catch (NumberFormatException e) {
            throw new BLangUsageException("invalid argument '" + argument + "', expected decimal value");
        }
    }

    private static boolean getBooleanValue(String argument) {
        if (!TRUE.equalsIgnoreCase(argument) && !FALSE.equalsIgnoreCase(argument)) {
            throw new BLangUsageException("invalid argument '" + argument + "', expected boolean value 'true' or "
                                                  + "'false'");
        }
        return Boolean.parseBoolean(argument);
    }

    private static byte getByteValue(String argument) {
        long longValue; // TODO: 7/4/18 Allow byte literals?
        try {
            longValue = Long.parseLong(argument);
        } catch (NumberFormatException e) {
            throw new BLangUsageException("invalid argument '" + argument + "', expected byte value");
        }
        if (!CPU.isByteLiteral(longValue)) {
            throw new BLangUsageException("invalid argument '" + argument + "', expected byte value, found int");
        }
        return (byte) longValue;
    }

    private static BNewArray getRestArgArray(BType type, int index, String[] args) {
        BType elementType = ((BArrayType) type).getElementType();
        try {
            switch (elementType.getTag()) {
                case TypeTags.ANY_TAG:
                case TypeTags.STRING_TAG:
                    BStringArray stringArrayArgs = new BStringArray();
                    for (int i = index; i < args.length; i++) {
                        stringArrayArgs.add(i - index, args[i]);
                    }
                    return stringArrayArgs;
                case TypeTags.INT_TAG:
                    BIntArray intArrayArgs = new BIntArray();
                    for (int i = index; i < args.length; i++) {
                        intArrayArgs.add(i - index, getIntegerValue(args[i]));
                    }
                    return intArrayArgs;
                case TypeTags.FLOAT_TAG:
                    BFloatArray floatArrayArgs = new BFloatArray();
                    for (int i = index; i < args.length; i++) {
                        floatArrayArgs.add(i - index, getFloatValue(args[i]));
                    }
                    return floatArrayArgs;
                case TypeTags.DECIMAL_TAG:
                    BDecimalArray decimalArrayArgs = new BDecimalArray();
                    for (int i = index; i < args.length; i++) {
                        decimalArrayArgs.add(i - index, getDecimalValue(args[i]));
                    }
                    return decimalArrayArgs;
                case TypeTags.BOOLEAN_TAG:
                    BBooleanArray booleanArrayArgs = new BBooleanArray();
                    for (int i = index; i < args.length; i++) {
                        booleanArrayArgs.add(i - index, getBooleanValue(args[i]) ? 1 : 0);
                    }
                    return booleanArrayArgs;
                case TypeTags.BYTE_TAG:
                    BByteArray byteArrayArgs = new BByteArray();
                    for (int i = index; i < args.length; i++) {
                        byteArrayArgs.add(i - index, getByteValue(args[i]));
                    }
                    return byteArrayArgs;
                default:
                    BRefValueArray refValueArray = new BRefValueArray();
                    for (int i = index; i < args.length; i++) {
                        refValueArray.add(i - index, (BRefType<?>) getBValue(elementType, args[i]));
                    }
                    return refValueArray;
            }
        } catch (BLangUsageException e) {
            throw new BLangUsageException(e.getLocalizedMessage().replace(INVALID_ARG, INVALID_ARG_AS_REST_ARG));
        } catch (Exception e) {
            //Ideally shouldn't reach here
            throw new BLangUsageException("error parsing rest arg: " + e.getLocalizedMessage());
        }
    }

    private static BRefValueArray parseTupleArg(BTupleType type, String tupleArg) {
        String stringSpecificationErrorSuffix = "', expected argument in the format \\\"str\\\" for tuple element of "
                + "type 'string'";
        String[] tupleElements = tupleArg.split(COMMA);

        if (tupleElements.length != type.getTupleTypes().size()) {
            throw new BLangUsageException("invalid argument '(" + tupleArg + ")', element count mismatch for tuple "
                                                  + "type: '" + type + "'");
        }

        BRefValueArray tupleValues = new BRefValueArray(type);
        int index = 0;
        for (BType elementType : type.getTupleTypes()) {
            String tupleElement = tupleElements[index].trim();
            try {
                if (elementType.getTag() == TypeTags.STRING_TAG) {
                    if (!tupleElement.startsWith("\"") || !tupleElement.endsWith("\"")) {
                        throw new BLangUsageException("invalid tuple element argument '" + tupleElement
                                                              + stringSpecificationErrorSuffix);
                    }
                    tupleElement = tupleElement.substring(1, tupleElement.length() - 1);
                }
                tupleValues.add(index, (BRefType) getBValue(elementType, tupleElement));
                index++;
            } catch (BallerinaException | BLangUsageException e) {
                String localizedMessage = e.getLocalizedMessage();
                if (localizedMessage.startsWith(UNSUPPORTED_TYPE_PREFIX)) {
                    throw new BLangUsageException("unsupported element type for tuple as entry function argument: "
                                                          + elementType);
                } else if (!localizedMessage.endsWith(stringSpecificationErrorSuffix)) {
                    throw new BLangUsageException("invalid tuple member argument '" + tupleElement
                                                          + "', expected value of type '" + elementType + "'");
                }
                throw e;
            }
        }
        return tupleValues;
    }

    private static BValue parseUnionArg(BUnionType type, String unionArg) {
        List<BType> unionMemberTypes = type.getMemberTypes();

        if (unionMemberTypes.contains(BTypes.typeNull) && NIL.equals(unionArg)) {
            return null;
        }

        if (unionMemberTypes.contains(BTypes.typeString)) {
            return new BString(unionArg);
        }

        for (int memberTypeIndex = 0; memberTypeIndex < unionMemberTypes.size();) {
            try {
                BType memberType = unionMemberTypes.get(memberTypeIndex);
                if (memberType.getTag() == TypeTags.NULL_TAG) {
                    memberTypeIndex++;
                    continue;
                }
                return getBValue(memberType, unionArg);
            } catch (BLangUsageException e) {
                memberTypeIndex++;
            }
        }
        throw new BLangUsageException("invalid argument '" + unionArg + "' specified for union type: "
                                          + (type.isNullable() ? type.toString().replace("|null", "|()") : type));
    }
}
