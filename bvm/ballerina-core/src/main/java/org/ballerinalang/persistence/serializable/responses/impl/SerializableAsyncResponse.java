/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence.serializable.responses.impl;

import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.ForkJoinWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.responses.SerializableResponseContext;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;

/**
 * This class implements @{@link SerializableResponseContext} to serialize @{@link ForkJoinWorkerResponseContext}.
 *
 * @since 0.982.0
 */
public class SerializableAsyncResponse extends SerializableResponseContext {

    public int[] retRegIndexes;

    public int workerCount;

    public SerializableAsyncResponse(String respCtxKey, AsyncInvocableWorkerResponseContext respCtx) {
        this.respCtxKey = respCtxKey;
        retRegIndexes = respCtx.getRetRegIndexes();
        workerCount = respCtx.getWorkerCount();
    }

    @Override
    public WorkerResponseContext getResponseContext(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                                    SerializableState state, Deserializer deserializer) {
        return new AsyncInvocableWorkerResponseContext(callableUnitInfo, workerCount);
    }

    @Override
    public void addTargetContexts(WorkerResponseContext respCtx, SerializableState state) {
    }

    @Override
    public void joinTargetContextInfo(WorkerResponseContext respCtx, ProgramFile programFile, SerializableState state,
                                      Deserializer deserializer) {
    }
}
