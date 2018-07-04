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
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
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
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

/**
 * This class contains utilities to execute Ballerina main and service programs.
 *
 * @since 0.8.0
 */
public class BLangProgramRunner {

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
        for (int index = 0; index < paramTypes.length; index++) {
            switch (paramTypes[index].getTag()) {
                case TypeTags.INT_TAG:
                    bValueArgs[index] = new BInteger(getIntegerValue(args[index]));
                    break;
                case TypeTags.FLOAT_TAG:
                    bValueArgs[index] = new BFloat(getFloatValue(args[index]));
                    break;
                case TypeTags.STRING_TAG:
                    bValueArgs[index] = new BString(args[index]);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    bValueArgs[index] = new BBoolean(getBooleanValue(args[index]));
                    break;
                case TypeTags.BYTE_TAG:
                    bValueArgs[index] = new BByte(getByteValue(args[index]));
                    break;
                case TypeTags.XML_TAG:
                    bValueArgs[index] = XMLUtils.parse(args[index]);
                    break;
                case TypeTags.JSON_TAG:
                    bValueArgs[index] = new BJSON(JsonParser.parse(args[index]));
                    break;
                case TypeTags.ARRAY_TAG:
                    if (index == paramTypes.length - 1) {
                        switch (((BArrayType) paramTypes[index]).getElementType().getTag()) {
                            case TypeTags.INT_TAG:
                                BIntArray intArrayArgs = new BIntArray();
                                for (int i = index; i < args.length; i++) {
                                    intArrayArgs.add(i - index, getIntegerValue(args[i]));
                                }
                                bValueArgs[index] = intArrayArgs;
                                break;
                            case TypeTags.FLOAT_TAG:
                                BFloatArray floatArrayArgs = new BFloatArray();
                                for (int i = index; i < args.length; i++) {
                                    floatArrayArgs.add(i - index, getFloatValue(args[i]));
                                }
                                bValueArgs[index] = floatArrayArgs;
                                break;
                            case TypeTags.STRING_TAG:
                                BStringArray stringArrayArgs = new BStringArray();
                                for (int i = index; i < args.length; i++) {
                                    stringArrayArgs.add(i - index, args[i]);
                                }
                                bValueArgs[index] = stringArrayArgs;
                                break;
                            case TypeTags.BOOLEAN_TAG:
                                BBooleanArray booleanArrayArgs = new BBooleanArray();
                                for (int i = index; i < args.length; i++) {
                                    booleanArrayArgs.add(i - index, getBooleanValue(args[i]) ? 1 : 0);
                                }
                                bValueArgs[index] = booleanArrayArgs;
                                break;
                            case TypeTags.BYTE_TAG:
                                BByteArray byteArrayArgs = new BByteArray();
                                for (int i = index; i < args.length; i++) {
                                    byteArrayArgs.add(i - index, getByteValue(args[i]));
                                }
                                bValueArgs[index] = byteArrayArgs;
                                break;
                            case TypeTags.XML_TAG:
                                BRefValueArray xmlArrayArgs = new BRefValueArray();
                                for (int i = index; i < args.length; i++) {
                                    xmlArrayArgs.add(i - index, XMLUtils.parse(args[i]));
                                }
                                bValueArgs[index] = xmlArrayArgs;
                                break;
                            case TypeTags.JSON_TAG:
                                BRefValueArray jsonArrayArgs = new BRefValueArray();
                                for (int i = index; i < args.length; i++) {
                                    jsonArrayArgs.add(i - index, new BJSON(JsonParser.parse(args[i])));
                                }
                                bValueArgs[index] = jsonArrayArgs;
                                break;
                            default:
                                //Ideally shouldn't reach here
                                throw new BallerinaException("array of unsupported element type: " + paramTypes[index]);
                        }
                        break;
                    }
                    throw new BallerinaException("array or rest parameter only supported as the final parameter");
            }
        }
        return bValueArgs;
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
}
