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
        continueExecution(resourceIExpr);
    }

    public void execute(FunctionInvocationExpr functionInvocationExpr) {
        continueExecution(functionInvocationExpr);
    }

    public void continueExecution(LinkedNode linkedNode) {
        linkedNode.accept(this);
        while (next != null) {
            try {
                next.accept(this);
            } catch (RuntimeException e) {
                handleBException(new BException(e.getMessage()));
            }
        }
    }

    public void continueExecution() {
        while (next != null) {
            try {
                next.accept(this);
            } catch (RuntimeException e) {
                handleBException(new BException(e.getMessage()));
            }
        }
    }
}
