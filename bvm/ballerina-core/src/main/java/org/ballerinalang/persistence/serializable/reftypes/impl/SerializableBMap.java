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

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.HashMap;

/**
 * Implementation of @{@link SerializableRefType} to serialize and deserialize {@link BMap} objects.
 *
 * @param <K> Key
 * @param <V> Value
 * @since 0.981.1
 */
public class SerializableBMap<K, V extends BValue> implements SerializableRefType {

    private String pkgPath;
    private String structName;

    private HashMap<K, Object> map = new HashMap<>();
    private HashMap<String, Object> nativeData = new HashMap<>();

    public SerializableBMap(BMap<K, V> bMap, SerializableState state) {
        structName = bMap.getType().getName();
        pkgPath = bMap.getType().getPackagePath();
        bMap.getNativeData().forEach((k, o) -> nativeData.put(k, state.serialize(o)));
        bMap.getMap().forEach((k, v) -> map.put(k, state.serialize(v)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public BRefType getBRefType(ProgramFile programFile, SerializableState state, Deserializer deserializer) {
        PackageInfo packageInfo = programFile.getPackageInfo(pkgPath);
        BMap<K, V> bMap;
        if (packageInfo != null) {
            StructureTypeInfo structInfo = packageInfo.getStructInfo(structName);
            if (structInfo == null) {
                throw new BallerinaException(structName + " not found in module " + pkgPath);
            }
            bMap = new BMap<>(structInfo.getType());
        } else {
            bMap = new BMap<>();
        }
        nativeData.forEach((s, o) -> bMap.addNativeData(s, state.deserialize(o, programFile, deserializer)));
        map.forEach((k, v) -> bMap.put(k, (V) state.deserialize(v, programFile, deserializer)));
        return bMap;
    }
}
