/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ActionInfo;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util Class for handling Error in Ballerina VM.
 */
public class BLangVMErrorHandlerUtil {

    private static final Logger logger = LoggerFactory.getLogger(BLangVMErrorHandlerUtil.class);

    public static final String ERROR_PCK = "ballerina.lang.errors";
    public static final String STRUCT_ERROR = "Error";
    public static final String STRUCT_STACKTRACE = "StackTrace";
    public static final String STRUCT_STACKTRACE_ITEM = "StackTraceItem";


    /**
     * Generate Error from current error.
     *
     * @param context     current Context
     * @param programFile {@link ProgramFile} instance that is executing
     * @param ip          current instruction pointer
     * @param structInfo  {@link StructInfo} of the error that need to be generated
     * @param values      field values of the error Struct.
     * @return generated error {@link BStruct}
     */
    public static BStruct generateError(Context context, ProgramFile programFile, int ip, StructInfo structInfo,
                                 BValue... values) {
        // TODO : Validate for Type Equivalence for error.
        PackageInfo errorPackageInfo = programFile.getPackageInfo(ERROR_PCK);
        if (structInfo == null) {
            structInfo = errorPackageInfo.getStructInfo(STRUCT_ERROR);
        }
        BStruct error = createBStruct(structInfo, values);
        // Set StackTrace.
        StructInfo stackTrace = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE);
        BStruct bStackTrace = createBStruct(stackTrace, generateStackTraceItems(context, programFile, ip - 1));
        error.setStackTrace(bStackTrace);
        return error;
    }


    /**
     * Set StackTrace for given Error Struct.
     *
     * @param context     current Context
     * @param programFile {@link ProgramFile} instance that is executing
     * @param ip          current instruction pointer
     * @param error       error Struct to be set the stackTrace.
     */
    public static void setStackTrace(Context context, ProgramFile programFile, int ip, BStruct error) {
        PackageInfo errorPackageInfo = programFile.getPackageInfo(ERROR_PCK);
        if (error == null) {
            // This shouldn't execute.
            logger.warn("Error struct is null. Default Error is created.");
            StructInfo structInfo = errorPackageInfo.getStructInfo(STRUCT_ERROR);
            error = createBStruct(structInfo);
        }
        StructInfo stackTrace = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE);
        BStruct bStackTrace = createBStruct(stackTrace, generateStackTraceItems(context, programFile, ip - 1));
        error.setStackTrace(bStackTrace);
    }

    /**
     * Generate StackTraceItem array.
     *
     * @param context     current Context
     * @param programFile {@link ProgramFile} instance that is executing
     * @param ip          current instruction pointer
     * @return generated StackTraceItem struct array
     */
    public static BRefValueArray generateStackTraceItems(Context context, ProgramFile programFile, int ip) {
        BRefValueArray stackTraceItems = new BRefValueArray();
        PackageInfo errorPackageInfo = programFile.getPackageInfo(ERROR_PCK);
        StructInfo stackTraceItem = errorPackageInfo.getStructInfo(STRUCT_STACKTRACE_ITEM);
        ControlStackNew controlStack = context.getControlStackNew();
        int currentIP = ip;
        BValue[] values;
        // Ignore caller stack frame.
        for (int i = controlStack.fp; i >= 1; i--) {
            values = new BValue[4];
            StackFrame stackFrame = controlStack.getStack()[i];
            CallableUnitInfo callableUnitInfo = stackFrame.callableUnitInfo;
            String parentScope = "";
            if (callableUnitInfo instanceof ResourceInfo) {
                parentScope = ((ResourceInfo) callableUnitInfo).getServiceInfo().getName() + ".";
            } else if (callableUnitInfo instanceof ActionInfo) {
                parentScope = ((ActionInfo) callableUnitInfo).getConnectorInfo().getName() + ".";
            }
            values[0] = new BString(parentScope + callableUnitInfo.getName());
            values[1] = new BString(callableUnitInfo.getPkgPath());
            LineNumberInfo lineNumberInfo = callableUnitInfo.getPackageInfo().getLineNumberInfo(currentIP);
            values[2] = new BString(lineNumberInfo.getFileName());
            values[3] = new BInteger(lineNumberInfo.getLineNumber());
            stackTraceItems.add(i - 1, createBStruct(stackTraceItem, values));
            // Always get the previous instruction pointer.
            currentIP = stackFrame.retAddrs - 1;
        }
        return stackTraceItems;
    }

    /**
     * Create BStruct for given StructInfo and BValues.
     *
     * @param structInfo {@link StructInfo} of the BStruct
     * @param values     field values of the BStruct.
     * @return BStruct instance.
     */
    public static BStruct createBStruct(StructInfo structInfo, BValue... values) {
        BStruct bStruct = new BStruct(structInfo.getType());
        bStruct.setFieldTypes(structInfo.getFieldTypes());
        bStruct.init(structInfo.getFieldCount());

        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;
        for (int i = 0; i < structInfo.getFieldTypes().length; i++) {
            BType paramType = structInfo.getFieldTypes()[i];
            if (values.length < i + 1) {
                break;
            }
            switch (paramType.getTag()) {
                case TypeTags.INT_TAG:
                    ++longRegIndex;
                    if (values[i] != null) {
                        bStruct.setIntField(longRegIndex, ((BInteger) values[i]).intValue());
                    }
                    break;
                case TypeTags.FLOAT_TAG:
                    ++doubleRegIndex;
                    if (values[i] != null) {
                        bStruct.setFloatField(doubleRegIndex, ((BFloat) values[i]).floatValue());
                    }
                    break;
                case TypeTags.STRING_TAG:
                    ++stringRegIndex;
                    if (values[i] != null) {
                        bStruct.setStringField(stringRegIndex, values[i].stringValue());
                    }
                    break;
                case TypeTags.BOOLEAN_TAG:
                    ++booleanRegIndex;
                    if (values[i] != null) {
                        bStruct.setBooleanField(booleanRegIndex, ((BBoolean) values[i]).booleanValue() ? 1 : 0);
                    }
                    break;
                default:
                    ++refRegIndex;
                    if (values[i] != null) {
                        bStruct.setRefField(refRegIndex, (BRefType) values[i]);
                    }
            }
        }
        return bStruct;
    }

    public static String getPrintableStackTrace(BStruct error) {
        StringBuilder sb = new StringBuilder();
//        sb.append(error.getType().getName()).append(" "); // TODO: Uncomment me once we get type support.
        sb.append(getErrorMsg(error)).append("\n");
        BStruct stackTrace = error.getStackTrace();
        BRefValueArray stackTraceItems = (BRefValueArray) stackTrace.getRefField(0);
        for (long i = stackTraceItems.size() - 1; i > 0; i--) {
            BStruct item = (BStruct) stackTraceItems.get(i);
            sb.append("    at ");
            if (item.getStringField(1) == null || item.getStringField(1).equals(".")) {
                sb.append(item.getStringField(0));
            } else {
                sb.append(item.getStringField(1)).append(":").append(item.getStringField(0));
            }
            sb.append("(").append(item.getStringField(2)).append(":").append(item.getIntField(0)).append(")");
            if (i > 1) {
                sb.append("\n");
            }
        }
        if (error.getRefField(0) != null) {
            sb.append("\n\t caused by ").append(getPrintableStackTrace((BStruct) error.getRefField(0)));
        }
        return sb.toString();
    }

    public static String getErrorMsg(BStruct error) {
        String errorType = error.getType().getSig().getName();
        if (error.getType().getSig().getPkgPath() == null || error.getType().getSig().getPkgPath() != ".") {
            errorType = error.getType().getSig().getPkgPath() + ":" + errorType;
        }
        return makeFirstLetterLowerCase(errorType + " { msg : \"" + error.getStringField(0) + "\" }");
    }

    private static String makeFirstLetterLowerCase(String s) {
        if (s == null) {
            return null;
        }
        char c[] = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}

