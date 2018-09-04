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
import org.ballerinalang.persistence.serializable.SerializableContext;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.responses.SerializableResponseContext;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class implements @{@link SerializableResponseContext} to serialize @{@link ForkJoinWorkerResponseContext}.
 *
 * @since 0.982.0
 */
public class SerializableAsyncResponse extends SerializableResponseContext {

    public int[] retRegIndexes;

    public int workerCount;

    private List<String> workerExecutionContexts;

    public SerializableAsyncResponse(String respCtxKey, AsyncInvocableWorkerResponseContext respCtx, SerializableState
            state, boolean updateTargetCtxIfExist) {
        super(respCtxKey);
        SerializableContext sTargetCtx = state.getContext(String.valueOf(respCtx.getTargetContext().hashCode()));
        if (sTargetCtx == null) {
            sTargetCtx = state.populateContext(respCtx.getTargetContext(), respCtx.getTargetContext().ip, true,
                                               updateTargetCtxIfExist, null);
        }
        targetCtxKey = sTargetCtx.ctxKey;
        retRegIndexes = respCtx.getRetRegIndexes();
        workerCount = respCtx.getWorkerCount();
        workerExecutionContexts = new ArrayList<>(respCtx.getWorkerExecutionContexts().size());
        respCtx.getWorkerExecutionContexts()
               .forEach(ctx -> workerExecutionContexts.add(String.valueOf(ctx.hashCode())));
    }

    @Override
    public WorkerResponseContext getResponseContext(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                                    SerializableState state, Deserializer deserializer) {
        AsyncInvocableWorkerResponseContext respCtx = new AsyncInvocableWorkerResponseContext(callableUnitInfo,
                                                                                              workerCount);
        respCtx.setWorkerExecutionContexts(workerExecutionContexts.stream()
                                                                  .map(s -> state.getExecutionContext(s, programFile,
                                                                                                      deserializer))
                                                                  .collect(Collectors.toList()));
        respCtx.joinTargetContextInfo(state.getExecutionContext(targetCtxKey, programFile, deserializer),
                                      retRegIndexes);
        return respCtx;
    }
}
