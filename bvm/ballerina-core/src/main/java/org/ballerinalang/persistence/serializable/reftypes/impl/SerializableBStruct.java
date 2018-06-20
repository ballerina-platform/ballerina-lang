/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.NativeDataKey;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.persistence.StateStore;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructureTypeInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.HashMap;

public class SerializableBStruct implements SerializableRefType {

    private String pkgPath;
    private String structName;
    private int flags;

    private HashMap<String, Object> nativeData = new HashMap<>();

    private long[] longFields;
    private double[] doubleFields;
    private String[] stringFields;
    private int[] intFields;
    private byte[][] byteFields;
    private ArrayList<Object> refFields;

    public SerializableBStruct() {}

    public SerializableBStruct(BStruct bStruct, SerializableState state) {

        structName = bStruct.getType().getName();
        pkgPath = bStruct.getType().getPackagePath();
        flags = bStruct.getType().flags;

        longFields = bStruct.longFields;
        doubleFields = bStruct.doubleFields;
        stringFields = bStruct.stringFields;
        intFields = bStruct.intFields;
        byteFields = bStruct.byteFields;

        for (String key : bStruct.nativeData.keySet()) {
            Object o = bStruct.nativeData.get(key);
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
        refFields = state.serializeRefFields(bStruct.refFields);
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
        BStruct bStruct = new BStruct(structInfo.getType());

        for (String key : nativeData.keySet()) {
            Object o = nativeData.get(key);
            if (o == null) {
                bStruct.nativeData.put(key, null);
                continue;
            }

            Object v = state.deserialize(o, programFile);
            if (v instanceof NativeDataKey) {
                StateStore.getDataMapper().
                        mapNativeData(state.getSerializationId(), (NativeDataKey) o, bStruct);
                bStruct.nativeData.put(key, null);
            } else {
                bStruct.nativeData.put(key, v);
            }
        }

        bStruct.longFields = longFields;
        bStruct.doubleFields = doubleFields;
        bStruct.intFields = intFields;
        bStruct.stringFields = stringFields;
        bStruct.byteFields = byteFields;
        bStruct.refFields = state.deserializeRefFields(refFields, programFile);
        return bStruct;
    }

    public String serialize() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static SerializableBStruct deserialize(String jsonString) {
        Gson gson = new GsonBuilder().create();
        SerializableBStruct serializableBStruct = gson.fromJson(jsonString, SerializableBStruct.class);
        return serializableBStruct;
    }
}
