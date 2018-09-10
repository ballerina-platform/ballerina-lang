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
package org.ballerinalang.persistence.serializable.reftypes.impl;

import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.model.values.BCallableFuture;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Implementation of @{@link SerializableRefType} to serialize and deserialize {@link BFunctionPointer} objects.
 *
 * @since 0.982.0
 */
public class SerializableBFuture implements SerializableRefType {

    public String respCtxKey;

    private String callableName;

    private String pkgPath;

    public SerializableBFuture(BCallableFuture bFuture, SerializableState state) {
        CallableUnitInfo callableUnitInfo = ((AsyncInvocableWorkerResponseContext) bFuture.value())
                .getCallableUnitInfo();
        if (callableUnitInfo.attachedToType != null) {
            callableName = callableUnitInfo.attachedToType.getName() + "." + callableUnitInfo.getName();
        } else {
            callableName = callableUnitInfo.getName();
        }
        pkgPath = ((AsyncInvocableWorkerResponseContext) bFuture.value()).getCallableUnitInfo().getPkgPath();

        respCtxKey = state.addRespContext(bFuture.value()).getRespCtxKey();
    }

    @Override
    public BRefType getBRefType(ProgramFile programFile, SerializableState state,
                                Deserializer deserializer) {
        return new BCallableFuture(callableName, null);
    }

    @Override
    public void setContexts(BRefType refType, ProgramFile programFile, SerializableState state,
                            Deserializer deserializer) {
        PackageInfo packageInfo = programFile.getPackageInfo(pkgPath);
        if (packageInfo == null) {
            throw new BallerinaException("Package cannot be found  for path: " + pkgPath);
        } else {
            CallableUnitInfo callableUnitInfo = packageInfo.getFunctionInfo(callableName);
            BCallableFuture bCallableFuture = (BCallableFuture) refType;
            bCallableFuture.setRespCtx((AsyncInvocableWorkerResponseContext) state
                    .getResponseContext(respCtxKey, programFile, callableUnitInfo, deserializer));
        }
    }
}
