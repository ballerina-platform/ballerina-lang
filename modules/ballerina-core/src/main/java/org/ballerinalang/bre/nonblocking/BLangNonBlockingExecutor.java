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
package org.ballerinalang.bre.nonblocking;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.model.LinkedNode;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.values.BException;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import static java.lang.Thread.sleep;

/**
 * {@link BLangNonBlockingExecutor} is a non-blocking and self driven Ballerina Executor.
 *
 * @since 0.8.0
 */
public class BLangNonBlockingExecutor extends BLangAbstractExecutionVisitor {

    public BLangNonBlockingExecutor(RuntimeEnvironment runtimeEnv, Context bContext) {
        super(runtimeEnv, bContext);
    }

    public void execute(ResourceInvocationExpr resourceIExpr) {
        resourceInvocation = true;
        startExecution(resourceIExpr);
    }

    public void execute(FunctionInvocationExpr functionInvocationExpr) {
        testFunctionInvocation = true;
        startExecution(functionInvocationExpr);
    }

    public void startExecution(LinkedNode linkedNode) {
        try {
            // TODO : Improve logic.
            // Intentionally catching exception outside of the loop and continueExecution after handlingException.
            // This can cause to grow stack depth. But throwing a runtimeExceptions is unlikely in normal execution
            // flow.
            try {
                linkedNode.accept(this);
                while (next != null) {
                    next.accept(this);
                }
            } catch (BLangRuntimeException e) {
                // Execution stop abnormally. Stop executing further.
            } catch (RuntimeException e) {
                // Handling any unhandled java runtime errors.
                handleBException(new BException(e.getMessage()));
                continueExecution();
            }
        } catch (Throwable throwable) {
            // Handling any unhandled java runtime errors, which can occurs during handleBException or any Java Error.
            if (resourceInvocation) {
                setStatus(STATUS_RESOURCE_TERMINATION);
                ErrorHandlerUtils.handleResourceInvocationError(bContext, lastActive, null, throwable);
            } else if (testFunctionInvocation) {
                setStatus(STATUS_TEST_TERMINATION);
                failedCause = throwable.getMessage();
            } else {
                setStatus(STATUS_MAIN_TERMINATION);
                ErrorHandlerUtils.handleMainFuncInvocationError(bContext, lastActive, null, throwable);
            }
        }
    }

    public void continueExecution() {
        try {
            while (next != null) {
                next.accept(this);
            }
        } catch (BLangRuntimeException e) {
            // Execution stop abnormally. Stop executing further.
        } catch (RuntimeException e) {
            // Handling any unhandled java runtime errors.
            handleBException(new BException(e.getMessage()));
            continueExecution();
        }
    }

    public void holdOn() {
        try {
            while (!isExecutionStopped()) {
                sleep(100);
            }
        } catch (InterruptedException ignore) {
        }
    }
}
