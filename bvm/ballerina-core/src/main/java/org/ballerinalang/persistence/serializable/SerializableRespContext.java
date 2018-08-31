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
package org.ballerinalang.persistence.serializable;

import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;

/**
 * This class represents a serializable Ballerina response context.
 *
 * @since 0.981.1
 */
public class SerializableRespContext {

    String respCtxKey;

    String targetContextKey;

    public int[] retRegIndexes;

    public int workerCount;

    SerializableRespContext(String respCtxKey, CallableWorkerResponseContext respCtx, SerializableState state,
                            boolean updateTargetCtxIfExist) {
        this.respCtxKey = respCtxKey;
        SerializableContext sTargetCtx = state.getContext(String.valueOf(respCtx.getTargetContext().hashCode()));
        if (sTargetCtx == null) {
            sTargetCtx = state.populateContext(respCtx.getTargetContext(), respCtx.getTargetContext().ip, true,
                                               updateTargetCtxIfExist, null);
        }
        targetContextKey = sTargetCtx.ctxKey;
        retRegIndexes = respCtx.getRetRegIndexes();
        workerCount = respCtx.getWorkerCount();
    }

    CallableWorkerResponseContext getResponseContext(ProgramFile programFile, CallableUnitInfo callableUnitInfo,
                                                     SerializableState state, Deserializer deserializer) {
        CallableWorkerResponseContext respCtx = new CallableWorkerResponseContext(callableUnitInfo.getRetParamTypes(),
                                                                                  workerCount);
        respCtx.joinTargetContextInfo(state.getExecutionContext(targetContextKey, programFile, deserializer),
                                      retRegIndexes);
        return respCtx;
    }
}
