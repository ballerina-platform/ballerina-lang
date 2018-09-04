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
 *
 */
package org.ballerinalang.persistence.serializable.responses;

import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.ForkJoinWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.responses.impl.SerializableAsyncResponse;
import org.ballerinalang.persistence.serializable.responses.impl.SerializableCallableResponse;
import org.ballerinalang.persistence.serializable.responses.impl.SerializableForkJoinResponse;

/**
 * This factory class provides an implementation of @{@link SerializableResponseContext}
 * on given @{@link org.ballerinalang.bre.bvm.WorkerResponseContext}.
 *
 * @since 0.982.0
 */
public class SerializableResponseContextFactory {

    public SerializableResponseContext getResponseContext(String respCtxKey, WorkerResponseContext respCtx,
                                                          SerializableState state, boolean updateTargetCtxIfExist) {
        if (respCtx instanceof AsyncInvocableWorkerResponseContext) {
            return new SerializableAsyncResponse(respCtxKey, (AsyncInvocableWorkerResponseContext) respCtx, state,
                                                 updateTargetCtxIfExist);
        } else if (respCtx instanceof ForkJoinWorkerResponseContext) {
            return new SerializableForkJoinResponse(respCtxKey, (ForkJoinWorkerResponseContext) respCtx, state,
                                                    updateTargetCtxIfExist);
        } else {
            return new SerializableCallableResponse(respCtxKey, (CallableWorkerResponseContext) respCtx, state,
                                                    updateTargetCtxIfExist);
        }
    }
}
