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

import org.ballerinalang.model.values.BClosure;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.List;

/**
 * Implementation of @{@link SerializableRefType} to serialize and deserialize {@link BFunctionPointer} objects.
 *
 * @since 0.982.0
 */
public class SerializableBFuncPointer implements SerializableRefType {

    private String funcName;
    private String funcPkgPath;
    private List<BClosure> bClosures;

    public SerializableBFuncPointer(BFunctionPointer bfp) {
        funcName = bfp.value().getName();
        funcPkgPath = bfp.value().getPkgPath();
        bClosures = bfp.getClosureVars();
    }

    @Override
    public BRefType getBRefType(String key, ProgramFile programFile, SerializableState state, Deserializer
            deserializer) {
        PackageInfo packageInfo = programFile.getPackageInfo(funcPkgPath);
        if (packageInfo == null) {
            throw new BallerinaException("Package cannot be found  for path: " + funcPkgPath);
        } else {
            FunctionInfo functionInfo = packageInfo.getFunctionInfo(funcName);
            if (functionInfo == null) {
                throw new BallerinaException(funcName + " cannot be found in package: " + funcPkgPath);
            }
            BFunctionPointer bfp = new BFunctionPointer(functionInfo);
            bClosures.forEach(bClosure -> {
                bfp.addClosureVar(bClosure, bClosure.getType().getTag());
            });
            return bfp;
        }
    }

    @Override
    public void setContexts(BRefType refType, ProgramFile programFile, SerializableState state,
                            Deserializer deserializer) {

    }
}
