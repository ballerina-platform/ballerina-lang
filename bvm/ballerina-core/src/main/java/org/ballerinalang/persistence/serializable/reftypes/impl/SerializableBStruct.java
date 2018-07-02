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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.PersistenceUtils;
import org.ballerinalang.persistence.serializable.NativeDataKey;
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
 *
 * @since 0.976.0
 */
public class SerializableBMap<K, V extends BValue> implements SerializableRefType {

    private String pkgPath;
    private String structName;

    private HashMap<Object, Object> map = new HashMap<>();
    private HashMap<String, Object> nativeData = new HashMap<>();

    public SerializableBMap(BMap<K, V> bMap, SerializableState state) {

        structName = bMap.getType().getName();
        pkgPath = bMap.getType().getPackagePath();
        for (String key : bMap.getNativeDataKeySet()) {
            Object o = bMap.getNativeData(key);
            if (o == null) {
                nativeData.put(key, null);
                continue;
            }
            Object s = state.serialize(o);
            if (s != null) {
                nativeData.put(key, s);
            } else {
                NativeDataKey nativeDataKey =
                        new NativeDataKey(pkgPath, structName, key, o.getClass().getName());
                nativeData.put(key, nativeDataKey);
            }
        }
        bMap.getMap().forEach((k, v) -> {
            if (v == null) {
                map.put(k, null);
            } else {
                Object s = state.serialize(v);
                if (s != null) {
                    map.put(k, s);
                } else {
                    NativeDataKey nativeDataKey =
                            new NativeDataKey(pkgPath, structName, k.toString(), v.getClass().getName());
                    map.put(k, nativeDataKey);
                }
            }
        });
    }

    @Override
    public BRefType getBRefType(ProgramFile programFile, SerializableState state) {

        PackageInfo packageInfo = programFile.getPackageInfo(pkgPath);
        if (packageInfo == null) {
            throw new BallerinaException(pkgPath + " not found in program file: " +
                                                 programFile.getProgramFilePath().toString());
        }

        StructureTypeInfo structInfo = packageInfo.getStructInfo(structName);
        if (structInfo == null) {
            throw new BallerinaException(structName + " not found in package " + pkgPath);
        }
        BMap<K, V> bMap = new BMap<>(structInfo.getType());
        for (String key : nativeData.keySet()) {
            Object o = nativeData.get(key);
            if (o == null) {
                bMap.addNativeData(key, null);
                continue;
            }
            Object v = state.deserialize(o, programFile);
            if (v instanceof NativeDataKey) {
                PersistenceUtils.getDataMapper().mapNativeData(state.getSerializationId(),
                                                               (NativeDataKey) o, bMap);
                bMap.addNativeData(key, null);
            } else {
                bMap.addNativeData(key, v);
            }
        }
        bMap.getMap().forEach((k, v) -> {
            Object o = map.get(k);
            if (o == null) {
                bMap.put(k, null);

            } else {
                Object val = state.deserialize(o, programFile);
                if (val instanceof NativeDataKey) {
                    PersistenceUtils.getDataMapper().mapNativeData(state.getSerializationId(),
                                                                   (NativeDataKey) o, bMap);
                    bMap.put(k, null);
                } else {
                    bMap.put(k, v);
                }
            }
        });
        return bMap;
    }

    public String serialize() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static SerializableBMap deserialize(String jsonString) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, SerializableBMap.class);
    }
}
