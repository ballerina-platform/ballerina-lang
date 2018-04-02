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
package org.ballerinalang.bre.bvm;

/**
 * This is an implementation of a {@link WorkerResponseContext} which doesn't do anything.
 * 
 * @since 0.965.0
 */
public class InitWorkerResponseContext implements WorkerResponseContext {

    private WorkerExecutionContext targetCtx;
    
    private boolean errored;
    
    public InitWorkerResponseContext(WorkerExecutionContext targetCtx) {
        this.targetCtx = targetCtx;
    }
    
    @Override
    public WorkerExecutionContext signal(WorkerSignal signal) {
        switch (signal.getType()) {
        case ERROR:
            this.errored = true;
            BLangScheduler.errorThrown(this.targetCtx, signal.getSourceContext().getError());
            break;
        case HALT:
            break;
        case RETURN:
            break;
        default:
            break;
        }
        return null;
    }
    
    public boolean isErrored() {
        return errored;
    }

    @Override
    public WorkerExecutionContext joinTargetContextInfo(WorkerExecutionContext targetCtx, int[] retRegIndexes) {
        return null;
    }

    @Override
    public WorkerDataChannel getWorkerDataChannel(String name) {
        return null;
    }

}
