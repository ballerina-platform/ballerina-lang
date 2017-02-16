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

import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.wso2.ballerina.core.interpreter.BLangExecutor;
import org.wso2.ballerina.core.interpreter.CallableUnitInfo;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandlerUtils;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;

/**
 * {@code BLangProgramRunner} runs main and service programs.
 *
 * @since 0.8.0
 */
public class BLangProgramRunner {

    public void startServices(BLangProgram bLangProgram) {
        BLangPackage[] servicePackages = bLangProgram.getServicePackages();
        if (servicePackages.length == 0) {
            throw new RuntimeException("no service(s) found in '" + bLangProgram.getProgramFilePath() + "'");
        }

        int serviceCount = 0;
        for (BLangPackage servicePackage : servicePackages) {
            for (Service service : servicePackage.getServices()) {
                serviceCount++;
                service.setBLangProgram(bLangProgram);
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) ->
                        dispatcher.serviceRegistered(service));
            }
        }

        if (serviceCount == 0) {
            throw new RuntimeException("no service(s) found in '" + bLangProgram.getProgramFilePath() + "'");
        }

        // Create a runtime environment for this Ballerina application
        RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);
        bLangProgram.setRuntimeEnvironment(runtimeEnv);
    }

    public void runMain(BLangProgram bLangProgram, String[] args) {
        Context bContext = new Context();
        BallerinaFunction mainFunction = bLangProgram.getMainFunction();

        try {
            BValue[] argValues = new BValue[mainFunction.getStackFrameSize()];
            BArray<BString> arrayArgs = new BArray<>(BString.class);
            for (int i = 0; i < args.length; i++) {
                arrayArgs.add(i, new BString(args[i]));
            }

            argValues[0] = arrayArgs;

            CallableUnitInfo functionInfo = new CallableUnitInfo(mainFunction.getName(), mainFunction.getPackagePath(),
                    mainFunction.getNodeLocation());

            StackFrame stackFrame = new StackFrame(argValues, new BValue[0], functionInfo);
            bContext.getControlStack().pushFrame(stackFrame);

            // Invoke main function
            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);
            BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
            mainFunction.getCallableUnitBody().execute(executor);

            bContext.getControlStack().popFrame();
        } catch (Throwable ex) {
            String errorMsg = ErrorHandlerUtils.getErrorMessage(ex);
            String stacktrace = ErrorHandlerUtils.getMainFuncStackTrace(bContext, ex);
            throw new BLangRuntimeException(errorMsg + "\n" + stacktrace);
        }
    }
}
