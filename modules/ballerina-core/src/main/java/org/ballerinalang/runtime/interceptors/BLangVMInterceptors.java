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
package org.ballerinalang.runtime.interceptors;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMWorkers;
import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.codegen.CodeAttributeInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * BLangVMInterceptors is utility class for invoking an interceptor using Ballerina VM.
 */
public interface BLangVMInterceptors {

    Logger LOGGER = LoggerFactory.getLogger(BLangVMInterceptors.class);

    /**
     * Invokes given ResourceInterceptor with given message.
     *
     * @param resourceInterceptor ballerina interceptor to be invoked
     * @param bMessage            input arguments for the interceptor
     * @return return values from the interceptor
     */
    static ResourceInterceptor.Result invokeResourceInterceptor(ResourceInterceptor resourceInterceptor,
                                                                BMessage bMessage) {

        Context context = new Context(resourceInterceptor.getProgramFile());
        PackageInfo packageInfo = resourceInterceptor.getPackageInfo();
        FunctionInfo functionInfo = resourceInterceptor.getInterceptorFunction();

        ControlStackNew controlStackNew = context.getControlStackNew();

        // First Create the caller's stack frame. This frame contains zero local variables, but it contains enough
        // registers to hold function arguments as well as return values from the callee.
        org.ballerinalang.bre.bvm.StackFrame callerSF =
                new org.ballerinalang.bre.bvm.StackFrame(packageInfo, -1, new int[0]);
        controlStackNew.pushFrame(callerSF);
        // registers to store return values. (boolean, message)
        int[] retRegs = {0, 0};
        callerSF.setLongRegs(new long[0]);
        callerSF.setDoubleRegs(new double[0]);
        callerSF.setStringRegs(new String[0]);
        callerSF.setIntRegs(new int[1]);
        callerSF.setRefRegs(new BRefType[1]);
        callerSF.setByteRegs(new byte[0][]);

        // Now create callee's stackframe
        WorkerInfo defaultWorkerInfo = functionInfo.getDefaultWorkerInfo();
        org.ballerinalang.bre.bvm.StackFrame calleeSF =
                new org.ballerinalang.bre.bvm.StackFrame(functionInfo, defaultWorkerInfo, -1, retRegs);
        controlStackNew.pushFrame(calleeSF);

        CodeAttributeInfo codeAttribInfo = defaultWorkerInfo.getCodeAttributeInfo();

        long[] longLocalVars = new long[codeAttribInfo.getMaxLongLocalVars()];
        double[] doubleLocalVars = new double[codeAttribInfo.getMaxDoubleLocalVars()];
        String[] stringLocalVars = new String[codeAttribInfo.getMaxStringLocalVars()];
        // Setting the zero values for strings
        Arrays.fill(stringLocalVars, "");

        int[] intLocalVars = new int[codeAttribInfo.getMaxIntLocalVars()];
        byte[][] byteLocalVars = new byte[codeAttribInfo.getMaxByteLocalVars()][];
        BRefType[] refLocalVars = new BRefType[codeAttribInfo.getMaxRefLocalVars()];

        refLocalVars[0] = bMessage;

        calleeSF.setLongLocalVars(longLocalVars);
        calleeSF.setDoubleLocalVars(doubleLocalVars);
        calleeSF.setStringLocalVars(stringLocalVars);
        calleeSF.setIntLocalVars(intLocalVars);
        calleeSF.setByteLocalVars(byteLocalVars);
        calleeSF.setRefLocalVars(refLocalVars);

        // Execute workers
        BLangVMWorkers.invoke(resourceInterceptor.getProgramFile(), functionInfo, calleeSF, retRegs);

        BLangVM bLangVM = new BLangVM(resourceInterceptor.getProgramFile());
        context.setStartIP(codeAttribInfo.getCodeAddrs());
        bLangVM.run(context);

        if (context.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(context.getError());
            LOGGER.error("error in service interception, " + stackTraceStr);
            throw new BLangRuntimeException(BLangVMErrors.getErrorMessage(context.getError()));
        }

        return new ResourceInterceptor.Result(callerSF.getIntRegs()[0] == 1, (BMessage) callerSF.getRefRegs()[0]);
    }
}
