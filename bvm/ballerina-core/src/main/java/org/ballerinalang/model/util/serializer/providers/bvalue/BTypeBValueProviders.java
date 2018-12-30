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
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BObjectType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.util.serializer.BPacket;
import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ObjectTypeInfo;
import org.ballerinalang.util.codegen.RecordTypeInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contain SerializationBValueProviders for BType derived classes.
 *
 * @since 0.982.0
 */
public class BTypeBValueProviders {
    private static final String ELEM_TYPE = "elemType";
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
        public BPacket toBValue(BAnyType type, BValueSerializer serializer) {
            String packagePath = type.getPackagePath();
            String typeName = type.getName();
            Class<?> clazz = type.getValueClass();

            BPacket packet = BPacket.from(typeName(), null);
            if (packagePath != null) {
                packet.put(PACKAGE_PATH, new BString(packagePath));
            }
            packet.put(TYPE_NAME, new BString(typeName));
            packet.put(VALUE_CLASS, serializer.toBValue(clazz, null));

            return packet;
        }

        @Override
        @SuppressWarnings("unchecked")
        public BAnyType toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            String typeName = packet.get(TYPE_NAME).stringValue();
            String pkgPath = null;
            BValue pkgP = packet.get(PACKAGE_PATH);
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

        static final String DIMENSIONS = "dimensions";
        static final String SIZE = "size";

        @Override
        public Class<?> getType() {
            return BArrayType.class;
        }

        @Override
        public String typeName() {
            return getType().getName();
        }

        @Override
        public BPacket toBValue(BArrayType type, BValueSerializer serializer) {
            BType elementType = type.getElementType();
            int dimensions = type.getDimensions();
            int size = type.getSize();

            BPacket packet = BPacket.from(typeName(), null);
            packet.put(ELEM_TYPE, serializer.toBValue(elementType, null));
            packet.put(DIMENSIONS, new BInteger(dimensions));
            packet.put(SIZE, new BInteger(size));

            return packet;
        }

        @Override
        @SuppressWarnings("unchecked")
        public BArrayType toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            BType elemType = (BType) bValueDeserializer.deserialize(packet.get(ELEM_TYPE), BType.class);
            int size = (int) ((BInteger) packet.get(SIZE)).intValue();

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
        public BPacket toBValue(BObjectType objType, BValueSerializer serializer) {
            String packagePath = objType.getPackagePath();
            String typeName = objType.getName();
            int flags = objType.flags;

            BPacket packet = BPacket.from(typeName(), null);
            packet.put(PACKAGE_PATH, new BString(packagePath));
            packet.put(TYPE_NAME, new BString(typeName));
            packet.put(FLAGS, new BInteger(flags));

            return packet;
        }

        @SuppressWarnings("unchecked")
        @Override
        public BObjectType toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            String typeName = packet.get(TYPE_NAME).stringValue();
            String pkgPath = packet.get(PACKAGE_PATH).stringValue();
            int flags = (int) ((BInteger) packet.get(FLAGS)).intValue();

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

        static final String REST_FIELD_SIGNATURE_CP_INDEX = "restFieldSignatureCPIndex";
        static final String REST_FIELD_TYPE_SIGNATURE = "restFieldTypeSignature";
        private static final String PACKAGE_PATH = "packagePath";
        private static final String TYPE_NAME = "typeName";
        private static final String FLAGS = "flags";
        private static final String REST_FIELD_TYPE = "restFieldType";
        private static final String CLOSED = "closed";

        @Override
        public Class<?> getType() {
            return BRecordType.class;
        }

        @Override
        public String typeName() {
            return getType().getName();
        }

        @Override
        public BPacket toBValue(BRecordType recType, BValueSerializer serializer) {
            String packagePath = recType.getPackagePath();
            String typeName = recType.getName();
            int flags = recType.flags;
            int restFieldSignatureCPIndex = recType.recordTypeInfo.getRestFieldSignatureCPIndex();
            String restFieldTypeSignature = recType.recordTypeInfo.getRestFieldTypeSignature();

            BPacket packet = BPacket.from(typeName(), serializer.toBValue(recType.getFields(), null));
            packet.putString(PACKAGE_PATH, packagePath);
            packet.putString(TYPE_NAME, typeName);
            packet.put(FLAGS, new BInteger(flags));
            packet.put(REST_FIELD_SIGNATURE_CP_INDEX, new BInteger(restFieldSignatureCPIndex));
            packet.putString(REST_FIELD_TYPE_SIGNATURE, restFieldTypeSignature);
            packet.put(CLOSED, new BBoolean(recType.sealed));
            packet.put(REST_FIELD_TYPE, serializer.toBValue(recType.restFieldType, null));
            return packet;
        }

        @SuppressWarnings("unchecked")
        @Override
        public BRecordType toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
            String typeName = packet.get(TYPE_NAME).stringValue();
            String pkgPath = packet.get(PACKAGE_PATH).stringValue();
            int flags = (int) ((BInteger) packet.get(FLAGS)).intValue();
            int cpIndex = (int) ((BInteger) packet.get(REST_FIELD_SIGNATURE_CP_INDEX)).intValue();
            BBoolean closed = (BBoolean) packet.get(CLOSED);

            RecordTypeInfo recTypeInfo = new RecordTypeInfo();
            recTypeInfo.setRestFieldSignatureCPIndex(cpIndex);
            BValue restFieldTypeSig = packet.get(REST_FIELD_TYPE_SIGNATURE);
            if (restFieldTypeSig != null) {
                String typeSig = restFieldTypeSig.stringValue();
                recTypeInfo.setRestFieldTypeSignature(typeSig);
            }

            BRecordType bRecType = new BRecordType(recTypeInfo, typeName, pkgPath, flags);
            Map<String, BField> fields = (Map<String, BField>) bValueDeserializer.deserialize(packet.getValue(),
                                                                                              LinkedHashMap.class);
            bRecType.setFields(fields);

            recTypeInfo.setType(bRecType);
            bRecType.restFieldType = (BType) bValueDeserializer.deserialize(packet.get(REST_FIELD_TYPE), BType.class);
            bRecType.sealed = closed.booleanValue();

            return bRecType;
        }
    }
}
