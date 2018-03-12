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
 * This represents a result context of a group of workers.
 */
public interface WorkerResponseContext {
    
    /**
     * This is called by the workers to signal the response context of any
     * signal that needs to be passed into. For example, a worker returning, or
     * reporting an error upstream.
     * 
     * @param signal the signal to be passed in
     * @return the worker execution context, if not null, that should be
     *         executed after returning from this call, this is used to re-use
     *         the same calling thread to execute the following worker execution
     */
    WorkerExecutionContext signal(WorkerSignal signal);

    /**
     * This is called to (re-)execute the logic that would execute when the worker response context
     * is fulfilled. That is, when the function call which represent this workder response context
     * is returned, an error is generated etc.. An example implementation of this would be to set
     * the return values to the parent context's local variables.
     * 
     * @param runInCaller if the execution after this should run in the calling thread
     * @return the worker execution context that should be continued in the caller thread
     */
    WorkerExecutionContext onFulfillment(boolean runInCaller);
    
    /**
     * Update the target worker execution context information.
     * 
     * @param targetCtx the target worker execution context
     * @param retRegIndexes  the return registry locations of the target execution context
     *                       where the current worker execution context should report to
     */
    void updateTargetContextInfo(WorkerExecutionContext targetCtx, int[] retRegIndexes);
    
    /**
     * Creates and returns the data channels used to communicate between the workers.
     * 
     * @param name the unique name of the data channel name requested
     * @return the data channel 
     */
    WorkerDataChannel getWorkerDataChannel(String name);
    
    /**
     * This will block the calling thread until the current worker response context is fulfilled.
     */
    void waitForResponse();
    
}
