/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang;

import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BNewArray;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.DefaultValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.LocalVariableInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParamDefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParameterAttributeInfo;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.util.codegen.attributes.AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE;
import static org.ballerinalang.util.codegen.attributes.AttributeInfo.Kind.PARAMETERS_ATTRIBUTE;
import static org.ballerinalang.util.codegen.attributes.AttributeInfo.Kind.PARAMETER_DEFAULTS_ATTRIBUTE;

/**
 * This class contains utilities to execute Ballerina main and service programs.
 *
 * @since 0.8.0
 */
public class BLangProgramRunner {

    private static final String DEFAULT_PARAM_PREFIX = "-";
    private static final String DEFAULT_PARAM_DELIMETER = "=";
    private static final String INCOMPATIBLE_TYPES = "incompatible types: ";
    private static final String COMMA = ",";

    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";

    public static void runService(ProgramFile programFile) {
        if (!programFile.isServiceEPAvailable()) {
            throw new BallerinaException("no services found in '" + programFile.getProgramFilePath() + "'");
        }

        // Get the service package
        PackageInfo servicesPackage = programFile.getEntryPackage();
        if (servicesPackage == null) {
            throw new BallerinaException("no services found in '" + programFile.getProgramFilePath() + "'");
        }

        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);

        BLangFunctions.invokePackageInitFunctions(programFile);
        BLangFunctions.invokePackageStartFunctions(programFile);
    }

    public static void runMain(ProgramFile programFile, String[] args) {
        if (!programFile.isMainEPAvailable()) {
            throw new BallerinaException("main function not found in  '" + programFile.getProgramFilePath() + "'");
        }
        PackageInfo mainPkgInfo = programFile.getEntryPackage();
        if (mainPkgInfo == null) {
            throw new BallerinaException("main function not found in  '" + programFile.getProgramFilePath() + "'");
        }
        Debugger debugger = new Debugger(programFile);
        initDebugger(programFile, debugger);

        FunctionInfo mainFuncInfo = getMainFunction(mainPkgInfo);
        try {
            BLangFunctions.invokeEntrypointCallable(programFile, mainFuncInfo, extractMainArgs(mainFuncInfo, args));
        } finally {
            if (programFile.isServiceEPAvailable()) {
                return;
            }
            if (debugger.isDebugEnabled()) {
                debugger.notifyExit();
            }
            BLangFunctions.invokePackageStopFunctions(programFile);
        }
    }

    private static BValue[] extractMainArgs(FunctionInfo mainFuncInfo, String[] args) {
        BType[] paramTypes = mainFuncInfo.getParamTypes();
        BValue[] bValueArgs = new BValue[paramTypes.length];

        ParameterAttributeInfo parameterAttributeInfo =
                (ParameterAttributeInfo) mainFuncInfo.getAttributeInfo(PARAMETERS_ATTRIBUTE);
        int requiredParamsCount = parameterAttributeInfo.requiredParamsCount;
        int defaultableParamsCount = parameterAttributeInfo.defaultableParamsCount;
        int restParamCount = parameterAttributeInfo.restParamCount;

        LocalVariableAttributeInfo localVariableAttributeInfo =
                (LocalVariableAttributeInfo) mainFuncInfo.getAttributeInfo(LOCAL_VARIABLES_ATTRIBUTE);
        ParamDefaultValueAttributeInfo paramDefaultValueAttributeInfo =
                (ParamDefaultValueAttributeInfo) mainFuncInfo.getAttributeInfo(PARAMETER_DEFAULTS_ATTRIBUTE);

        if (defaultableParamsCount > 0) {
            args = populateNamedArgs(args, bValueArgs, localVariableAttributeInfo, paramDefaultValueAttributeInfo,
                                     requiredParamsCount, defaultableParamsCount);
        }


        for (int index = 0; index < paramTypes.length - defaultableParamsCount - restParamCount; index++) {
            bValueArgs[index] = getBValue(paramTypes[index], args[index]);
        }

        if (restParamCount == 1) {
            bValueArgs[paramTypes.length - 1] = getRestArgArray(paramTypes[paramTypes.length - 1],
                                                                paramTypes.length - 1 - defaultableParamsCount, args);
        }

        return bValueArgs;
    }

    private static void initDebugger(ProgramFile programFile, Debugger debugger) {
        programFile.setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            debugger.init();
            debugger.waitTillDebuggeeResponds();
        }
    }

    public static FunctionInfo getMainFunction(PackageInfo mainPkgInfo) {
        String errorMsg = "main function not found in  '" +
                mainPkgInfo.getProgramFile().getProgramFilePath() + "'";

        FunctionInfo mainFuncInfo = mainPkgInfo.getFunctionInfo("main");
        if (mainFuncInfo == null) {
            throw new BallerinaException(errorMsg);
        }

        if (mainFuncInfo.getRetParamTypes().length != 0) {
            throw new BallerinaException(errorMsg);
        }

        return mainFuncInfo;
    }

    private static String[] populateNamedArgs(String[] args, BValue[] bValueArgs,
                                              LocalVariableAttributeInfo localVariableAttributeInfo,
                                              ParamDefaultValueAttributeInfo paramDefaultValueAttributeInfo,
                                              int requiredParamsCount, int defaultableParamsCount) {
        Map<String, Integer> defaultableParamIndices = new HashMap<>();

        List<LocalVariableInfo> localVariableInfoList = localVariableAttributeInfo.getLocalVariables();
        DefaultValue[] defaultValues = paramDefaultValueAttributeInfo.getDefaultValueInfo();

        int defaultValueIndex = 0;

        for (int defaultableParamIndex = requiredParamsCount;
                 defaultableParamIndex < requiredParamsCount + defaultableParamsCount;
                 defaultableParamIndex = defaultableParamIndex + defaultValueIndex) {
            LocalVariableInfo localVariableInfo = localVariableInfoList.get(defaultableParamIndex);
            bValueArgs[defaultableParamIndex] = getDefaultValue(localVariableInfo.getVariableType(),
                                                                defaultValues[defaultValueIndex++]);
            defaultableParamIndices.put(localVariableInfo.getVariableName(), defaultableParamIndex);
        }

        ArrayList<String> modifiedArgs = new ArrayList<>();
        for (String arg : args) {
            if (isDefaultParamCandidate(arg) && defaultableParamIndices.containsKey(getParamName(arg))) {
                int index = defaultableParamIndices.get(getParamName(arg));
                bValueArgs[index] = getBValue(localVariableInfoList.get(index).getVariableType(), getValueString(arg));
            } else {
                modifiedArgs.add(arg);
            }
        }
        return modifiedArgs.toArray(new String[0]);
    }

    private static boolean isDefaultParamCandidate(String arg) {
        return arg.startsWith(DEFAULT_PARAM_PREFIX) && arg.contains(DEFAULT_PARAM_DELIMETER);
    }

    private static String getParamName(String arg) {
        return arg.split(DEFAULT_PARAM_DELIMETER, 2)[0].substring(1).trim();
    }

    private static String getValueString(String arg) {
        return arg.split(DEFAULT_PARAM_DELIMETER, 2)[1];
    }

    private static BValue getDefaultValue(BType type, DefaultValue value) {
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                return new BInteger(value.getIntValue());
            case TypeTags.STRING_TAG:
                return new BString(value.getStringValue());
            case TypeTags.FLOAT_TAG:
                return new BFloat(value.getFloatValue());
            case TypeTags.BOOLEAN_TAG:
                return new BBoolean(value.getBooleanValue());
            case TypeTags.BYTE_TAG:
                return new BByte(value.getByteValue());
            default: //shouldn't reach here
                throw new BallerinaException("unsupported type specified as defaultable param: " + type);
        }
    }

    private static BValue getBValue(BType type, String value) {
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
                return new BInteger(getIntegerValue(value));
            case TypeTags.FLOAT_TAG:
                return new BFloat(getFloatValue(value));
            case TypeTags.STRING_TAG:
                return new BString(value);
            case TypeTags.BOOLEAN_TAG:
                return new BBoolean(getBooleanValue(value));
            case TypeTags.BYTE_TAG:
                return new BByte(getByteValue(value));
            case TypeTags.XML_TAG:
                return XMLUtils.parse(value);
            case TypeTags.JSON_TAG:
                return JsonParser.parse(value);
            case TypeTags.RECORD_TYPE_TAG:
                return JSONUtils.convertJSONToStruct(JsonParser.parse(value), (BStructureType) type);
            case TypeTags.TUPLE_TAG:
                if (!value.startsWith("(") || !value.endsWith(")")) {
                    throw new BallerinaException("Expected tuple notation (\"(a, b)\") for tuple typed parameter");
                }
                return parseTupleArg((BTupleType) type, value.substring(1, value.length() - 1));
            case TypeTags.ARRAY_TAG:
                try {
                    return JSONUtils.convertJSONToBArray(JsonParser.parse(value), (BArrayType) type);
                } catch (BallerinaException e) {
                    if (e.getLocalizedMessage().startsWith(INCOMPATIBLE_TYPES)) {
                        throw new BallerinaException("incompatible types: expected array elements of type: "
                                                             + ((BArrayType) type).getElementType());
                    }
                    throw new BallerinaException("Expected array notation (\"[\\\"a\\\", \\\"b\\\", \\\"c\\\"]\") for "
                                                         + "array typed main function parameter");
                }
            case TypeTags.MAP_TAG:
                try {
                    return JSONUtils.jsonToBMap(JsonParser.parse(value), (BMapType) type);
                } catch (BallerinaException e) {
                    throw new BallerinaException("Expected map notation (\"{\\\"a\\\":\\\"b\\\"}\") for "
                                                         + "map typed main function parameter");
                }
            default:
                throw new BallerinaException("unsupported type expected with main function: " + type);
        }
    }

    private static long getIntegerValue(String argument) {
        try {
            return Long.parseLong(argument);
        } catch (NumberFormatException e) {
            throw new BallerinaException("invalid argument: " + argument + ", integer expected");
        }
    }

    private static double getFloatValue(String argument) {
        try {
            return Double.parseDouble(argument);
        } catch (NumberFormatException e) {
            throw new BallerinaException("invalid argument: " + argument + ", float expected");
        }
    }

    private static boolean getBooleanValue(String argument) {
        if (!TRUE.equalsIgnoreCase(argument) && !FALSE.equalsIgnoreCase(argument)) {
            throw new BallerinaException("invalid argument: " + argument + ", boolean expected");
        }
        return Boolean.parseBoolean(argument);
    }

    private static byte getByteValue(String argument) {
        long longValue; // TODO: 7/4/18 Allow byte literals?
        try {
            longValue = Long.parseLong(argument);
            if (!CPU.isByteLiteral(longValue)) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new BallerinaException("invalid argument: " + argument + ", byte expected");
        }
        return (byte) longValue;
    }

    private static BNewArray getRestArgArray(BType type, int index, String[] args) {
        switch (((BArrayType) type).getElementType().getTag()) {
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
            case TypeTags.STRING_TAG:
                BStringArray stringArrayArgs = new BStringArray();
                for (int i = index; i < args.length; i++) {
                    stringArrayArgs.add(i - index, args[i]);
                }
                return stringArrayArgs;
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
            case TypeTags.XML_TAG:
                BRefValueArray xmlArrayArgs = new BRefValueArray();
                for (int i = index; i < args.length; i++) {
                    xmlArrayArgs.add(i - index, XMLUtils.parse(args[i]));
                }
                return xmlArrayArgs;
            case TypeTags.JSON_TAG:
                BRefValueArray jsonArrayArgs = new BRefValueArray();
                for (int i = index; i < args.length; i++) {
                    jsonArrayArgs.add(i - index, JsonParser.parse(args[i]));
                }
                return jsonArrayArgs;
            default:
                //Ideally shouldn't reach here
                throw new BallerinaException("array of unsupported element type as main function argument: " + type);
        }
    }

    private static BRefValueArray parseTupleArg(BTupleType type, String tupleArg) {
        String[] tupleElements = tupleArg.split(COMMA);

        if (tupleElements.length != type.getTupleTypes().size()) {
            throw new BallerinaException("element count mismatch for tuple type: " + type);
        }

        BRefValueArray tupleValues = new BRefValueArray();
        int index = 0;
        try {
            for (BType elementType : type.getTupleTypes()) {
                tupleValues.add(index, (BRefType) getBValue(elementType, tupleElements[index]));
                index++;
            }
        } catch (BallerinaException e) {
            throw new BallerinaException("unsupported element type for tuple as main function argument: " + type);
        }
        return tupleValues;
    }
}
