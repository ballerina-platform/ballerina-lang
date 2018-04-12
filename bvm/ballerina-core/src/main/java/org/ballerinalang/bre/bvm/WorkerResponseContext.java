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
public interface WorkerResponseContext extends ObservableContext {
    
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
     * Joins a new target worker execution context to the current response
     * context. This will add a new interested party to the current response,
     * which will be notified immediately if the response is already fulfilled,
     * by returning a worker execution context to execute, which would be the
     * calling context itself if there's no error (or else, after the error is 
     * resolved, the final executable worker execution context), or else, 
     * it will return null, and the caller should be notified later, by resuming 
     * the worker execution context later, when the response is fulfilled.
     * 
     * @param targetCtx
     *            the target worker execution context
     * @param retRegIndexes
     *            the return registry locations of the target execution context
     *            where the current worker execution context should report to
     * @return the worker execution context that should be executed after the join
     */
    WorkerExecutionContext joinTargetContextInfo(WorkerExecutionContext targetCtx, int[] retRegIndexes);
    
    /**
     * Creates and returns the data channels used to communicate between the workers.
     * 
     * @param name the unique name of the data channel name requested
     * @return the data channel 
     */
    WorkerDataChannel getWorkerDataChannel(String name);
    
}
