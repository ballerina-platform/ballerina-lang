/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.task;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;

/**
 * This class invokes the Ballerina onTrigger function, and if an error occurs while invoking that function, it invokes
 * the onError function.
 */
public class TaskExecutor {

    public static void execute(NativeCallableUnit fn, Context parentCtx, FunctionRefCPEntry onTriggerFunction,
                               FunctionRefCPEntry onErrorFunction, ProgramFile programFile) {
        boolean isErrorFnCalled = false;
        try {
            // Invoke the onTrigger function.
            BValue[] results = BLangFunctions.invokeCallable(onTriggerFunction.getFunctionInfo(),
                    new BValue[0]);
            // If there are results, that mean an error has been returned
            if (onErrorFunction != null && results.length > 0 && results[0] != null) {
                isErrorFnCalled = true;
                BLangFunctions.invokeCallable(onErrorFunction.getFunctionInfo(), results);
            }
        } catch (BLangRuntimeException e) {

            //Call the onError function in case of error.
            if (onErrorFunction != null && !isErrorFnCalled) {
                BLangFunctions.invokeCallable(onErrorFunction.getFunctionInfo(),
                        new BValue[] { BLangVMErrors.createError(parentCtx, e.getMessage()) });
            }
        }
    }
}
