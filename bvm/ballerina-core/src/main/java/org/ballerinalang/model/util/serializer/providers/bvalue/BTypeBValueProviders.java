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
package org.ballerinalang.model.util.serializer.providers.bvalue;

import org.ballerinalang.model.types.BAnyType;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BObjectType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ObjectTypeInfo;
import org.ballerinalang.util.codegen.RecordTypeInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Contain SerializationBValueProviders for BType derived classes.
 *
 * @since 0.98.1
 */
public class BTypeBValueProviders {
    public static final String ELEM_TYPE = "elemType";
    private static final String PACKAGE_PATH = "packagePath";
    private static final String TYPE_NAME = "typeName";
    private static final String VALUE_CLASS = "valueClass";

    private BTypeBValueProviders() {
    }

    /**
     * Provide mapping between {@link BAnyType} and {@link BValue} representation of it.
     */
    public static class BAnyTypeBValueProvider implements SerializationBValueProvider<BAnyType> {

        @Override
        public Class<?> getType() {
            return BAnyType.class;
        }

        @Override
        public String typeName() {
            return getType().getName();
        }

        @Override
        public BValue toBValue(BAnyType type, BValueSerializer serializer) {
            BMap<String, BValue> map = new BMap<>();
            String packagePath = type.getPackagePath();
            String typeName = type.getName();
            Class<?> clazz = type.getValueClass();

            if (packagePath != null) {
                map.put(PACKAGE_PATH, new BString(packagePath));
            }
            map.put(TYPE_NAME, new BString(typeName));
            map.put(VALUE_CLASS, serializer.toBValue(clazz, null));

            return BValueProviderHelper.wrap(typeName(), map);
        }

        @Override
        public BAnyType toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
            BMap<String, BValue> map =
                    (BMap<String, BValue>) BValueProviderHelper.getPayload((BMap<String, BValue>) bValue);
            String typeName = map.get(TYPE_NAME).stringValue();
            String pkgPath = null;
            BValue pkgP = map.get(PACKAGE_PATH);
            if (pkgP != null) {
                pkgPath = pkgP.stringValue();
            }

            try {
                Constructor<BAnyType> constructor = BAnyType.class.getDeclaredConstructor(String.class, String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(typeName, pkgPath);
            } catch (InstantiationException | IllegalAccessException |
                    InvocationTargetException | NoSuchMethodException e) {
                return null;
            }
        }
    }

    /**
     * Provide mapping between {@link BArrayType} and {@link BValue} representation of it.
     */
    public static class BArrayTypeBValueProvider implements SerializationBValueProvider<BArrayType> {

        public static final String DIMENSIONS = "dimensions";
        public static final String SIZE = "size";

        @Override
        public Class<?> getType() {
            return BArrayType.class;
        }

        @Override
        public String typeName() {
            return getType().getName();
        }

        @Override
        public BValue toBValue(BArrayType type, BValueSerializer serializer) {
            BMap<String, BValue> map = new BMap<>();
            BType elementType = type.getElementType();
            int dimensions = type.getDimensions();
            int size = type.getSize();

            map.put(ELEM_TYPE, serializer.toBValue(elementType, null));
            map.put(DIMENSIONS, new BInteger(dimensions));
            map.put(SIZE, new BInteger(size));

            return BValueProviderHelper.wrap(typeName(), map);
        }

        @Override
        @SuppressWarnings("unchecked")
        public BArrayType toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
            BMap<String, BValue> map =
                    (BMap<String, BValue>) BValueProviderHelper.getPayload((BMap<String, BValue>) bValue);
            BType elemType = (BType) bValueDeserializer.deserialize(map.get(ELEM_TYPE), BType.class);
            int size = (int) ((BInteger) map.get(SIZE)).intValue();

            return new BArrayType(elemType, size);
        }
    }

    /**
     * Provide mapping between {@link BObjectType} and {@link BValue} representation of it.
     */
    public static class BObjectTypeBValueProvider implements SerializationBValueProvider<BObjectType> {

        private static final String PACKAGE_PATH = "packagePath";
        private static final String TYPE_NAME = "typeName";
        private static final String FLAGS = "flags";

        @Override
        public Class<?> getType() {
            return BObjectType.class;
        }

        @Override
        public String typeName() {
            return getType().getName();
        }

        @Override
        public BValue toBValue(BObjectType objType, BValueSerializer serializer) {
            BMap<String, BValue> map = new BMap<>();
            String packagePath = objType.getPackagePath();
            String typeName = objType.getName();
            int flags = objType.flags;

            map.put(PACKAGE_PATH, new BString(packagePath));
            map.put(TYPE_NAME, new BString(typeName));
            map.put(FLAGS, new BInteger(flags));

            return BValueProviderHelper.wrap(typeName(), map);
        }

        @Override
        public BObjectType toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
            BMap<String, BValue> map =
                    (BMap<String, BValue>) BValueProviderHelper.getPayload((BMap<String, BValue>) bValue);
            String typeName = map.get(TYPE_NAME).stringValue();
            String pkgPath = map.get(PACKAGE_PATH).stringValue();
            int flags = (int) ((BInteger) map.get(FLAGS)).intValue();

            ObjectTypeInfo objectTypeInfo = new ObjectTypeInfo();
            BObjectType bObjectType = new BObjectType(objectTypeInfo, typeName, pkgPath, flags);
            objectTypeInfo.setType(bObjectType);
            return bObjectType;
        }
    }

    /**
     * Provide mapping between {@link BRecordType} and {@link BValue} representation of it.
     */
    public static class BRecordTypeBValueProvider implements SerializationBValueProvider<BRecordType> {

        public static final String REST_FIELD_SIGNATURE_CP_INDEX = "restFieldSignatureCPIndex";
        public static final String REST_FIELD_TYPE_SIGNATURE = "restFieldTypeSignature";
        private static final String PACKAGE_PATH = "packagePath";
        private static final String TYPE_NAME = "typeName";
        private static final String FLAGS = "flags";

        @Override
        public Class<?> getType() {
            return BRecordType.class;
        }

        @Override
        public String typeName() {
            return getType().getName();
        }

        @Override
        public BValue toBValue(BRecordType recType, BValueSerializer serializer) {
            BMap<String, BValue> map = new BMap<>();
            String packagePath = recType.getPackagePath();
            String typeName = recType.getName();
            int flags = recType.flags;
            int restFieldSignatureCPIndex = recType.recordTypeInfo.getRestFieldSignatureCPIndex();
            String restFieldTypeSignature = recType.recordTypeInfo.getRestFieldTypeSignature();

            map.put(PACKAGE_PATH, new BString(packagePath));
            map.put(TYPE_NAME, new BString(typeName));
            map.put(FLAGS, new BInteger(flags));
            map.put(REST_FIELD_SIGNATURE_CP_INDEX, new BInteger(restFieldSignatureCPIndex));
            map.put(REST_FIELD_TYPE_SIGNATURE, new BString(restFieldTypeSignature));

            return BValueProviderHelper.wrap(typeName(), map);
        }

        @Override
        public BRecordType toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
            BMap<String, BValue> map =
                    (BMap<String, BValue>) BValueProviderHelper.getPayload((BMap<String, BValue>) bValue);
            String typeName = map.get(TYPE_NAME).stringValue();
            String pkgPath = map.get(PACKAGE_PATH).stringValue();
            int flags = (int) ((BInteger) map.get(FLAGS)).intValue();
            int cpIndex = (int) ((BInteger) map.get(REST_FIELD_SIGNATURE_CP_INDEX)).intValue();
            String typeSig = map.get(REST_FIELD_TYPE_SIGNATURE).stringValue();

            RecordTypeInfo recTypeInfo = new RecordTypeInfo();
            recTypeInfo.setRestFieldSignatureCPIndex(cpIndex);
            recTypeInfo.setRestFieldTypeSignature(typeSig);

            BRecordType bRecType = new BRecordType(recTypeInfo, typeName, pkgPath, flags);
            recTypeInfo.setType(bRecType);
            return bRecType;
        }
    }
}
