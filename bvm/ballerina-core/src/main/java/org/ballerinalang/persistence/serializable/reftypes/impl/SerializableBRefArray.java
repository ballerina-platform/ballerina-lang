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

import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;

/**
 * Implementation of @{@link SerializableRefType} to serialize and deserialize {@link BRefValueArray} objects.
 *
 * @since 0.981.1
 */
public class SerializableBRefArray implements SerializableRefType {

    private String structName;
    private String pkgPath;
    private ArrayList<Object> values = new ArrayList<>();

    public SerializableBRefArray(BRefValueArray bRefValueArray, SerializableState state) {
        BArrayType arrayType = (BArrayType) bRefValueArray.getType();
        if (arrayType != null) {
            structName = arrayType.getElementType().getName();
            pkgPath = arrayType.getElementType().getPackagePath();
        }
        for (int i = 0; i < bRefValueArray.size(); i++) {
            values.add(state.serialize(bRefValueArray.get(i)));
        }
    }

    @Override
    public BRefType getBRefType(ProgramFile programFile, SerializableState state, Deserializer deserializer) {
        PackageInfo packageInfo = programFile.getPackageInfo(pkgPath);
        BRefType[] bRefTypes = new BRefType[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Object deserialize = state.deserialize(values.get(i), programFile, deserializer);
            if (deserialize instanceof BRefType) {
                bRefTypes[i] = (BRefType) deserialize;
            } else {
                bRefTypes[i] = null;
            }
        }
        BType type = null;
        if (packageInfo != null) {
            StructureTypeInfo structInfo = packageInfo.getStructInfo(structName);
            if (structInfo == null) {
                throw new BallerinaException(structName + " not found in module " + pkgPath);
            }
            type = new BArrayType(structInfo.getType());
        }
        return new BRefValueArray(bRefTypes, type);
    }
}
