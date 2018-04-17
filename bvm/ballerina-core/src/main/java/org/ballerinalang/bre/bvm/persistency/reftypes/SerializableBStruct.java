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
package org.ballerinalang.bre.bvm.persistency.reftypes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.bre.bvm.persistency.PersistenceUtils;
import org.ballerinalang.bre.bvm.persistency.SerializableState;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.persistence.StateStore;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializableBStruct extends SerializableRefType {

    private String pkgPath;
    private String structName;
    private int flags;

    private Map<String, Object> nativeData = new HashMap<>();
    private List<NativeDataKey> nativeDataKeys = new ArrayList<>();

    private long[] longFields;
    private double[] doubleFields;
    private String[] stringFields;
    private int[] intFields;
    private byte[][] byteFields;

    private SerializableRefFields refFields;

    private RefTypeIndex[] refTypeIndices;
    private SerializableBStruct[] bStructs;
    private SerializableBString[] bStrings;

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
            if (o != null) {
                if (o instanceof BStruct) {
                    String bKey = state.addBStruct((BStruct) o);
                    nativeData.put(key, new SBStructKey(bKey));
                } else if (PersistenceUtils.isSerializable(o)) {
                    nativeData.put(key, o);
                } else {
                    NativeDataKey nativeDataKey =
                            new NativeDataKey(pkgPath, structName, key, o.getClass().getName());
                    nativeData.put(key, nativeDataKey);
                }
//                NativeDataKey nativeDataKey =
//                        new NativeDataKey(pkgPath, structName, key, o.getClass().getName());
//                nativeDataKeys.add(nativeDataKey);
            }
        }

        if (bStruct.refFields != null) {
            refFields = new SerializableRefFields(bStruct.refFields, state);
        }

//        if (bStruct.refFields != null) {
//            for ()
//        }

//        refTypeIndices = new RefTypeIndex[bStruct.refFields.length];
//        RefTypeCounter refTypeCounter = new RefTypeCounter(bStruct.refFields);
//        bStructs = new SerializableBStruct[refTypeCounter.getbStructCount()];
//        bStrings = new SerializableBString[refTypeCounter.getbStringCount()];
//        int refTypeIndex = 0;
//        int bStructIndex = 0;
//        int bStringIndex = 0;
//        for (int i = 0; i < bStruct.refFields.length; i++) {
//            BRefType refType = bStruct.refFields[i];
//            if (refType instanceof BStruct) {
//                bStructs[bStructIndex] = new SerializableBStruct((BStruct) refType);
//                refTypeIndices[refTypeIndex] =
//                        new RefTypeIndex(i, bStructIndex, RefTypeIndex.RefTypeName.BStruct);
//                bStructIndex++;
//                refTypeIndex++;
//            } else if (refType instanceof BString) {
//                bStrings[bStringIndex++] = new SerializableBString((BString) refType);
//                refTypeIndices[refTypeIndex++] =
//                        new RefTypeIndex(i, bStringIndex, RefTypeIndex.RefTypeName.BString);
//                bStringIndex++;
//                refTypeIndex++;
//            }
//        }
    }

    public BStruct getBSturct(ProgramFile programFile, SerializableState state) {
        PackageInfo packageInfo = programFile.getPackageInfo(pkgPath);
        if (packageInfo == null) {
            throw new BallerinaException(pkgPath + " not found in program file: " +
                    programFile.getProgramFilePath().toString());
        }

        StructInfo structInfo = packageInfo.getStructInfo(structName);
        if (structInfo == null) {
            throw new BallerinaException(structName + " not found in package " + pkgPath);
        }
        BStruct bStruct = new BStruct(structInfo.getType());

        for (String key : nativeData.keySet()) {
            Object o = nativeData.get(key);
            if (o == null) {
                continue;
            }
            if (o instanceof SBStructKey) {
                BStruct nativeDataBStruct = state.getBStruct(((SBStructKey)o).key, programFile);
                bStruct.nativeData.put(key, nativeDataBStruct);
            } else if (o instanceof NativeDataKey) {
                StateStore.getDataMapper().
                        mapNativeData(state.getSerializationId(), (NativeDataKey) o, bStruct);
            } else {
                bStruct.nativeData.put(key, o);
            }
        }

//        for (NativeDataKey nativeDataKey : nativeDataKeys) {
//            StateStore.getDataMapper().mapNativeData(state.getSerializationId(), nativeDataKey, bStruct);
//        }

        bStruct.longFields = longFields;
        bStruct.doubleFields = doubleFields;
        bStruct.intFields = intFields;
        bStruct.stringFields = stringFields;
        bStruct.byteFields = byteFields;

        if (refFields != null) {
            bStruct.refFields = refFields.getRefFeilds(programFile, state);
        }

//        bStruct.refFields = new BRefType[refTypeIndices.length];
//        for (int i = 0; i < refTypeIndices.length; i++) {
//            RefTypeIndex refTypeIndex = refTypeIndices[i];
//            switch (refTypeIndex.getRefTypeName()) {
//                case BStruct:
//                    BStruct childBStruct = bStructs[refTypeIndex.getSpecificTypeIndex()].
//                            getBSturct(programFile);
//                    bStruct.refFields[refTypeIndex.getRefTypeIndex()] = childBStruct;
//                    break;
//                case BString:
//                    BString bString = bStrings[refTypeIndex.getSpecificTypeIndex()].getBString();
//                    bStruct.refFields[refTypeIndex.getRefTypeIndex()] = bString;
//                    break;
//                default:
//                    throw new BallerinaException("Unknown ref type encountered.");
//            }
//        }
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

    class SBStructKey {
        public String key;

        public SBStructKey(String key) {
            this.key = key;
        }
    }
}
