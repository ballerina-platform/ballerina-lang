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
import org.ballerinalang.model.values.BException;

/**
 * {@link BLangNonBlockingExecutor} is a non-blocking and self driven Ballerina Executor.
 *
 * @since 0.8.0
 */
public class BLangNonBlockingExecutor extends BLangAbstractExecutionVisitor {

    private boolean execute;

    public BLangNonBlockingExecutor(RuntimeEnvironment runtimeEnv, Context bContext) {
        super(runtimeEnv, bContext);
        execute = true;
    }

    public void startExecution(LinkedNode linkedNode) {
        try {
            linkedNode.accept(this);
            continueExecution();
        } catch (RuntimeException e) {
            handleBException(new BException(e.getMessage()));
            continueExecution();
        }
        if (!execute) {
            notifyComplete();
            next = null;
        }
    }

    public void continueExecution() {
        try {
            while (next != null && execute) {
                next.accept(this);
            }
        } catch (RuntimeException e) {
            handleBException(new BException(e.getMessage()));
            continueExecution();
        }
    }

    @Override
    public void cancelExecution() {
        execute = false;
    }

    @Override
    public boolean isExecutionCanceled() {
        return !execute;
    }
}
