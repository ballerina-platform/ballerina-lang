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
package org.ballerinalang.connector.impl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.ControlStack;
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.debugger.DebuggerUtil;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Arrays;
import java.util.Map;

/**
 * {@code ResourceExecutor} This provides the implementation to execute resources within Ballerina.
 *
 * @since 0.94
 */
public class ResourceExecutor {

    /**
     * This method will execute the resource, given required details.
     * And it will use the future instance to notify interested parties about the
     * outcome of the execution.
     *
     * @param resource to be executed.
     * @param connectorFuture to notify.
     * @param properties to be passed to context.
     * @param bValues for parameters.
     */
    public static void execute(Resource resource, BServerConnectorFuture connectorFuture,
                               Map<String, Object> properties, BValue... bValues) {
        if (resource == null) {
            connectorFuture.notifyFailure(new BallerinaException("trying to execute a null resource"));
            return;
        }
        ResourceInfo resourceInfo = ((BResource) resource).getResourceInfo();
        ServiceInfo serviceInfo = resourceInfo.getServiceInfo();
        // Invoke VM.
        PackageInfo packageInfo = serviceInfo.getPackageInfo();
        ProgramFile programFile = packageInfo.getProgramFile();

        Context context = new Context(programFile);
        context.setServiceInfo(serviceInfo);
        context.setConnectorFuture(connectorFuture);

        //TODO remove this with a proper way
        if (properties != null) {
            properties.forEach(context::setProperty);
        }

        ControlStack controlStack = context.getControlStack();

        // Now create callee's stack-frame
        WorkerInfo defaultWorkerInfo = resourceInfo.getDefaultWorkerInfo();
        StackFrame calleeSF = new StackFrame(resourceInfo, defaultWorkerInfo, -1, new int[0]);
        controlStack.pushFrame(calleeSF);

        CodeAttributeInfo codeAttribInfo = defaultWorkerInfo.getCodeAttributeInfo();
        context.setStartIP(codeAttribInfo.getCodeAddrs());

        String[] stringReg = new String[codeAttribInfo.getMaxStringRegs()];
        Arrays.fill(stringReg, BLangConstants.STRING_NULL_VALUE);
        int[] intRegs = new int[codeAttribInfo.getMaxIntRegs()];
        long[] longRegs = new long[codeAttribInfo.getMaxLongRegs()];
        double[] doubleRegs = new double[codeAttribInfo.getMaxDoubleRegs()];
        byte[][] byteRegs = new byte[codeAttribInfo.getMaxByteRegs()][];
        BRefType[] refRegs = new BRefType[codeAttribInfo.getMaxRefRegs()];

        int stringParamCount = 0;
        int intParamCount = 0;
        int doubleParamCount = 0;
        int longParamCount = 0;
        int byteParamCount = 0;
        int refParamCount = 0;
        BType[] bTypes = resourceInfo.getParamTypes();

        if (bValues != null) {
            for (int i = 0; i < bValues.length; i++) {
                BType btype = bTypes[i];
                BValue value = bValues[i];

                // Set default values
                if (value == null) {
                    continue;
                }

                if (btype == BTypes.typeString) {
                    stringReg[stringParamCount++] = value.stringValue();
                } else if (btype == BTypes.typeBoolean) {
                    if ("true".equalsIgnoreCase(value.stringValue())) {
                        intRegs[intParamCount++] = 1;
                    } else if ("false".equalsIgnoreCase(value.stringValue())) {
                        intRegs[intParamCount++] = 0;
                    } else {
                        throw new BallerinaException("Unsupported parameter type for parameter " + value);
                    }
                } else if (btype == BTypes.typeFloat) {
                    doubleRegs[doubleParamCount++] = ((BFloat) value).floatValue();
                } else if (btype == BTypes.typeInt) {
                    longRegs[longParamCount++] = ((BInteger) value).intValue();
                } else if (btype == BTypes.typeBlob) {
                    byteRegs[byteParamCount++] = ((BBlob) value).blobValue();
                } else if (value instanceof BStruct || value instanceof BRefValueArray || btype == BTypes.typeJSON ||
                        btype == BTypes.typeXML) {
                    refRegs[refParamCount++] = (BRefType) value;
                } else {
                    connectorFuture.notifyFailure(new BallerinaException("unsupported " +
                            "parameter type for parameter " + value));
                }
            }
        }

        // It is given that first parameter of the resource is carbon message.
        calleeSF.setLongRegs(longRegs);
        calleeSF.setDoubleRegs(doubleRegs);
        calleeSF.setStringRegs(stringReg);
        calleeSF.setIntRegs(intRegs);
        calleeSF.setByteRegs(byteRegs);
        calleeSF.setRefRegs(refRegs);

        // Execute workers
        // Pass the incoming message variable into the worker invocations
        // Fix #2623
        StackFrame callerSF = new StackFrame(resourceInfo, defaultWorkerInfo, -1, new int[0]);
        callerSF.setRefRegs(new BRefType[1]);
        callerSF.getRefRegs()[0] = refRegs[0];

        BLangVM bLangVM = new BLangVM(packageInfo.getProgramFile());
        context.setAsResourceContext();
        context.startTrackWorker();
        Debugger debugger = programFile.getDebugger();
        if (debugger.isDebugEnabled()) {
            DebuggerUtil.initDebugContext(context, debugger);
        }
        bLangVM.run(context);
    }
}
