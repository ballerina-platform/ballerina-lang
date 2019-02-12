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
package org.ballerinalang.util.program;

import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.bre.old.WorkerData;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.ServiceInfo;

import java.io.PrintStream;
import java.math.BigDecimal;

/**
 * Utilities related to the Ballerina VM.
 */
public class BLangVMUtils {

    public static final String SERVICE_INFO_KEY = "SERVICE_INFO";

    @SuppressWarnings("rawtypes")
    public static void populateWorkerDataWithValues(StackFrame data, int regIndex, BValue val, BType retType) {
        switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                if (val == null) {
                    data.longRegs[regIndex] = 0;
                    break;
                }
                data.longRegs[regIndex] = ((BInteger) val).intValue();
                break;
            case TypeTags.BYTE_TAG:
                if (val == null) {
                    data.intRegs[regIndex] = 0;
                    break;
                }
                data.intRegs[regIndex] = ((BByte) val).byteValue();
                break;
            case TypeTags.FLOAT_TAG:
                if (val == null) {
                    data.doubleRegs[regIndex] = 0;
                    break;
                }
                data.doubleRegs[regIndex] = ((BFloat) val).floatValue();
                break;
            case TypeTags.DECIMAL_TAG:
                if (val == null) {
                    data.refRegs[regIndex] = new BDecimal(BigDecimal.ZERO);
                    break;
                }
                data.refRegs[regIndex] = (BDecimal) val;
                break;
            case TypeTags.STRING_TAG:
                if (val == null) {
                    data.stringRegs[regIndex] = BLangConstants.STRING_EMPTY_VALUE;
                    break;
                }
                data.stringRegs[regIndex] = val.stringValue();
                break;
            case TypeTags.BOOLEAN_TAG:
                if (val == null) {
                    data.intRegs[regIndex] = 0;
                    break;
                }
                data.intRegs[regIndex] = ((BBoolean) val).booleanValue() ? 1 : 0;
                break;
            default:
                data.refRegs[regIndex] = (BRefType) val;
        }
    }

    public static WorkerData createWorkerData(WorkerDataIndex wdi) {
        return new WorkerData(wdi);
    }
    
    public static void mergeResultData(WorkerData sourceData, WorkerData targetData, BType[] types,
                                       int[] regIndexes) {
        int callersRetRegIndex;
        int longRegCount = 0;
        int doubleRegCount = 0;
        int stringRegCount = 0;
        int intRegCount = 0;
        int refRegCount = 0;

        for (int i = 0; i < types.length; i++) {
            BType retType = types[i];
            callersRetRegIndex = regIndexes[i];
            switch (retType.getTag()) {
                case TypeTags.INT_TAG:
                    targetData.longRegs[callersRetRegIndex] = sourceData.longRegs[longRegCount++];
                    break;
                case TypeTags.BYTE_TAG:
                    targetData.intRegs[callersRetRegIndex] = sourceData.intRegs[intRegCount++];
                    break;
                case TypeTags.FLOAT_TAG:
                    targetData.doubleRegs[callersRetRegIndex] = sourceData.doubleRegs[doubleRegCount++];
                    break;
                case TypeTags.STRING_TAG:
                    targetData.stringRegs[callersRetRegIndex] = sourceData.stringRegs[stringRegCount++];
                    break;
                case TypeTags.BOOLEAN_TAG:
                    targetData.intRegs[callersRetRegIndex] = sourceData.intRegs[intRegCount++];
                    break;
                default:
                    targetData.refRegs[callersRetRegIndex] = sourceData.refRegs[refRegCount++];
                    break;
            }
        }
    }

    public static void log(String msg) {
        PrintStream out = System.out;
        out.println(msg);
    }
    
    public static void setServiceInfo(Strand ctx, ServiceInfo serviceInfo) {
        ctx.globalProps.put(SERVICE_INFO_KEY, serviceInfo);
    }
    
    public static ServiceInfo getServiceInfo(Strand ctx) {
        return (ServiceInfo) ctx.globalProps.get(SERVICE_INFO_KEY);
    }
}
