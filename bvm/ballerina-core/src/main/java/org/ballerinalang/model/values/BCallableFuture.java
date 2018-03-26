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
package org.ballerinalang.model.values;

import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

/**
 * Ballerina value for the callable "future" type.
 */
public class BCallableFuture implements BFuture {

    private String callableName;
    
    private AsyncInvocableWorkerResponseContext respCtx;
    
    public BCallableFuture(String callableName, AsyncInvocableWorkerResponseContext respCtx) {
        this.callableName = callableName;
        this.respCtx = respCtx;
    }
    
    @Override
    public String stringValue() {
        return "callable future: " + this.callableName;
    }

    @Override
    public BType getType() {
        return BTypes.typeFuture;
    }

    @Override
    public BValue copy() {
        return new BCallableFuture(this.callableName, this.respCtx);
    }

    @Override
    public WorkerResponseContext value() {
        return this.respCtx;
    }

    @Override
    public boolean cancel() {
        return this.respCtx.cancel();
    }

    @Override
    public boolean isDone() {
        return this.respCtx.isDone();
    }

    @Override
    public boolean isCancelled() {
        return this.respCtx.isCancelled();
    }

}
