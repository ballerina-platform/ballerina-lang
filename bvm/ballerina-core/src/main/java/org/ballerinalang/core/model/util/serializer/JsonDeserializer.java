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
package org.ballerinalang.core.model.util.serializer;

import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.util.serializer.providers.instance.ListInstanceProvider;
import org.ballerinalang.core.model.util.serializer.providers.instance.MapInstanceProvider;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.ARRAY_TAG;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.COMPLEX_KEY_MAP_TAG;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.COMPONENT_TYPE;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.ENUM_SEPERATOR;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.ENUM_TAG;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.EXISTING_TAG;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.ID_TAG;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.LENGTH_TAG;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.LIST_TAG;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.MAP_TAG;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.NULL_OBJ;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.TYPE_TAG;
import static org.ballerinalang.core.model.util.serializer.JsonSerializerConst.VALUE_TAG;
import static org.ballerinalang.core.model.util.serializer.ObjectHelper.findPrimitiveClass;

/**
 * Reconstruct Java object tree from JSON input.
 *
 * @since 0.982.0
 */
class JsonDeserializer implements BValueDeserializer {
    private final BValueProvider bValueProvider;
    private final HashMap<Long, Object> identityMap;
    private final BRefType<?> treeHead;
    private static final InstanceProviderRegistry instanceProvider;

    static {
        instanceProvider = InstanceProviderRegistry.getInstance();
        instanceProvider.add(new MapInstanceProvider());
        instanceProvider.add(new ListInstanceProvider());
    }

    JsonDeserializer(BRefType<?> objTree) {
        treeHead = objTree;
        bValueProvider = BValueProvider.getInstance();
        identityMap = new HashMap<>();
    }

    /**
     * Deserialize {@link BValue} tree into Java object graph.
     *
     * @param destinationType Starting node type for object graph.
     * @return reconstructed object graph.
     */
    Object deserialize(Class<?> destinationType) {
        if (ObjectHelper.isInstantiable(destinationType)) {
            return deserialize(treeHead, destinationType);
        } else {
            throw new BallerinaException(String.format("%s is not instantiable", destinationType.getName()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object deserialize(BValue jValue, Class<?> targetType) {
        if (jValue == null) {
            return null;
        }
        switch (jValue.getType().getTag()) {
            case TypeTags.MAP_TAG:
            case TypeTags.JSON_TAG:
                BMap<String, BValue> jBMap = (BMap<String, BValue>) jValue;
                Object obj = deserializeComplexType(jBMap, targetType);
                addObjReference(jBMap, obj);
                return obj;
            case TypeTags.ARRAY_TAG:
                return deserializeBRefValueArray((BValueArray) jValue, targetType);
            case TypeTags.STRING_TAG:
                return jValue.stringValue();
            case TypeTags.INT_TAG:
                return castLong(((BInteger) jValue).intValue(), targetType);
            case TypeTags.FLOAT_TAG:
                return castFloat((BFloat) jValue, targetType);
            case TypeTags.BOOLEAN_TAG:
                return ((BBoolean) jValue).booleanValue();
            default:
                throw new BallerinaException(
                        String.format("Unknown BValue type to deserialize: %s", jValue.getClass().getSimpleName()));
        }
    }

    private Object castFloat(BFloat jValue, Class<?> targetType) {
        if ((targetType == Float.class || targetType == float.class)) {
            return (float) jValue.floatValue();
        }
        return jValue.floatValue();
    }

    private Object castLong(long jValue, Class<?> targetType) {
        if (targetType == Short.class || targetType == short.class) {
            return (short) jValue;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return (int) jValue;
        }
        if (targetType == Byte.class || targetType == byte.class) {
            return (byte) jValue;
        }
        if (targetType == Character.class || targetType == char.class) {
            return (char) jValue;
        }
        return jValue;
    }

    /**
     * Create and populate array using {@param valueArray} and target type.
     *
     * @param valueArray source containing array data.
     * @param targetType destination array type.
     * @return new array of targetType type, filled with data from valueArray.
     */
    private Object deserializeBRefValueArray(BValueArray valueArray, Class<?> targetType) {
        int size = (int) valueArray.size();
        Class<?> componentType = targetType.getComponentType();
        Object target = Array.newInstance(componentType, size);
        return deserializeBRefValueArray(valueArray, target);
    }

    /**
     * Populate array using {@param valueArray} provided instance or array object.
     *
     * @param valueArray       source containing array data.
     * @param destinationArray target array to be filled with valueArray's data.
     * @return {@code destinationArray} filled with data from valueArray.
     */
    private Object deserializeBRefValueArray(BValueArray valueArray, Object destinationArray) {
        Class<?> componentType = destinationArray.getClass().getComponentType();
        for (int i = 0; i < valueArray.size(); i++) {
            Object obj = deserialize(valueArray.getRefValue(i), componentType);
            Array.set(destinationArray, i, obj);
        }
        return destinationArray;
    }

    private Object deserializeComplexType(BMap<String, BValue> jBMap, Class<?> targetType) {
        Object existing = findExistingReference(jBMap);
        if (existing != null) {
            return existing;
        }

        if (isNullObj(jBMap)) {
            return null;
        }

        Object object = null;
        String typeName = resolveTargetTypeName(targetType, jBMap);
        // try BValueProvider
        if (typeName != null) {
            SerializationBValueProvider provider = bValueProvider.find(typeName);
            if (provider != null) {
                object = provider.toObject(BPacket.toPacket(jBMap), this);
                addObjReference(jBMap, object);
            }
        }

        // try reflective reconstruction
        if (object == null) {
            Object emptyInstance = createInstance(jBMap, targetType);
            // add the obj reference here so that inner deserialization may refer this object
            addObjReference(jBMap, emptyInstance);
            object = deserializeInto(emptyInstance, jBMap, targetType);

            // check to make sure deserializeInto returns the populated 'emptyInstance'.
            // It's important  that it does not create own objects as it may interfere with
            // handling of existing references.
            if (object != emptyInstance) {
                throw new BallerinaException("Internal error: deserializeInto should not create own objects.");
            }
        }

        // execute java.io.Serializable.readResolve if available
        Object resolved = readResolve(jBMap, targetType, object);
        if (resolved != null) {
            return resolved;
        }
        return object;
    }

    private boolean isNullObj(BMap<String, BValue> jBMap) {
        return jBMap.hasKey(NULL_OBJ);
    }

    @SuppressWarnings("unchecked")
    private Object deserializeInto(Object instance, BMap<String, BValue> data, Class<?> targetType) {
        BValue valueNode = data.get(VALUE_TAG);
        if (data.hasKey(TYPE_TAG)) {
            BValue type = data.get(TYPE_TAG);
            switch (type.stringValue()) {
                case MAP_TAG:
                    return deserializeMap((BMap<String, BValue>) valueNode, (Map) instance);
                case LIST_TAG:
                    return deserializeList((BValueArray) valueNode, targetType, (List) instance);
                case ENUM_TAG:
                    return instance;
                case ARRAY_TAG:
                    return deserializeBRefValueArray((BValueArray) valueNode, instance);
                default:
                    // no op
            }
        }

        /* If all the data are contained in data it self, use data as valueNode */
        if (valueNode == null) {
            valueNode = data;
        }
        return deserializeReflectively(instance, (BMap<String, BValue>) valueNode);
    }

    private String resolveTargetTypeName(Class<?> targetType, BMap<String, BValue> jBMap) {
        if (jBMap.hasKey(TYPE_TAG)) {
            BValue jType = jBMap.get(TYPE_TAG);
            return jType.stringValue();
        } else if (targetType != null) {
            return ObjectHelper.getTrimmedClassName(targetType);
        }
        throw new IllegalStateException("Target type should either come from jBMap or via targetType param");
    }

    public void addObjReference(BMap<String, BValue> jBMap, Object object) {
        BInteger objId = (BInteger) jBMap.get(ID_TAG);
        if (objId != null) {
            identityMap.put(objId.intValue(), object);
        }
    }

    private Object findExistingReference(BMap<String, BValue> jBMap) {
        if (!jBMap.hasKey(EXISTING_TAG)) {
            return null;
        }
        BInteger existingObjId = (BInteger) jBMap.get(EXISTING_TAG);
        Object existingObjRef = getExistingObjRef(existingObjId.intValue());
        if (existingObjRef == null) {
            throw new BallerinaException("Can not find existing reference: " + existingObjId);
        }
        return existingObjRef;
    }

    @Override
    public Object getExistingObjRef(long key) {
        return identityMap.get(key);
    }


    /**
     * Create a empty java object instance for target type.
     *
     * @param jsonNode Data to be populated.
     * @param target   type of which the object to be created.
     * @return instance of given type.
     */
    private Object createInstance(BMap<String, BValue> jsonNode, Class<?> target) {
        if (target != null && Enum.class.isAssignableFrom(target)) {
            return createEnumInstance(jsonNode);
        }
        if (jsonNode.hasKey(TYPE_TAG)) {
            BValue typeNode = jsonNode.get(TYPE_TAG);
            String type = typeNode.stringValue();
            if (type.equals(ARRAY_TAG)) {
                return createArrayInstance(jsonNode);
            }
            return getObjectOf(type);
        }
        return getObjectOf(target);
    }

    private Object createArrayInstance(BMap<String, BValue> jsonNode) {
        BString ct = (BString) jsonNode.get(COMPONENT_TYPE);
        String componentType = ct.stringValue();

        BInteger bSize = (BInteger) jsonNode.get(LENGTH_TAG);
        int size = (int) bSize.intValue();

        Class<?> clazz = findClass(componentType);
        if (clazz != null) {
            return Array.newInstance(clazz, size);
        }
        throw new BallerinaException("Can not create array instance of: " + componentType + "[]");
    }

    @SuppressWarnings("unchecked")
    private Object createEnumInstance(BMap jsonNode) {
        String enumName = jsonNode.get(VALUE_TAG).stringValue();
        int separatorPos = enumName.lastIndexOf(ENUM_SEPERATOR);
        String type = enumName.substring(0, separatorPos);
        String enumConst = enumName.substring(separatorPos + 1);

        Class enumClass = instanceProvider.findInstanceProvider(type).getTypeClass();
        return Enum.valueOf(enumClass, enumConst);
    }

    private Object getObjectOf(Class<?> clazz) {
        String className = ObjectHelper.getTrimmedClassName(clazz);
        return getObjectOf(className);
    }

    private Object getObjectOf(String type) {
        TypeInstanceProvider typeProvider = instanceProvider.findInstanceProvider(type);
        return typeProvider.newInstance();
    }

    @SuppressWarnings("unchecked")
    private Object deserializeReflectively(Object instance, BMap<String, BValue> payload) {
        setFields(instance, payload);
        return instance;
    }

    private void setFields(Object target, BMap<String, BValue> fieldMap) {
        Objects.requireNonNull(target);
        Class<?> targetClass = target.getClass();
        HashMap<String, Field> allFields = ObjectHelper.getAllFields(targetClass, 0);

        for (String fieldName : fieldMap.keys()) {
            if (fieldName.equals(ID_TAG)) {
                // it's a metadata entry.
                continue;
            }
            if (!allFields.containsKey(fieldName)) {
                throw new BallerinaException(String.format("Can not find field '%s' from JSON in %s class",
                        fieldName, targetClass.getName()));
            }
            Field field = allFields.get(fieldName);
            BValue value = fieldMap.get(fieldName);
            Object object = deserialize(value, field.getType());
            ObjectHelper.setField(target, field, object);
        }
    }

    @SuppressWarnings("unchecked")
    private Object deserializeList(BValueArray jArray, Class<?> targetType, List targetList) {
        Class<?> componentType = targetType.getComponentType();
        for (BRefType<?> value : jArray.getValues()) {
            Object item = deserialize(value, componentType);
            targetList.add(item);
        }
        return targetList;
    }

    @SuppressWarnings("unchecked")
    private Object deserializeMap(BMap<String, BValue> payload, Map target) {
        BValue complexKeyMap = payload.get(COMPLEX_KEY_MAP_TAG);
        if (complexKeyMap instanceof BMap) {
            return deserializeComplexKeyMap((BMap<String, BValue>) complexKeyMap, payload, target);
        }

        for (String key : payload.keys()) {
            if (key.equals(COMPLEX_KEY_MAP_TAG)) {
                // don't process this entry here, as this is the complex key-map entry
                continue;
            }
            BValue value = payload.get(key);

            Class<?> fieldType = Object.class;
            if (value instanceof BMap) {
                BMap<String, BValue> item = (BMap<String, BValue>) value;
                if (item.hasKey(TYPE_TAG)) {
                    BValue val = item.get(TYPE_TAG);
                    String typeName = val.stringValue();
                    fieldType = findClass(typeName);
                }
            } else if (value instanceof BBoolean) {
                fieldType = BBoolean.class;
            }
            target.put(key, deserialize(value, fieldType));
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    private Object deserializeComplexKeyMap(BMap<String, BValue> complexKeyMap,
                                            BMap<String, BValue> payload, Map targetMap) {
        for (String key : payload.keys()) {
            if (key.equals(COMPLEX_KEY_MAP_TAG)) {
                // don't process this entry here, as this is the complex key-map entry
                continue;
            }
            BValue value = payload.get(key);
            BValue complexKey = complexKeyMap.get(key);
            Object ckObj = deserialize(complexKey, Object.class);

            targetMap.put(ckObj, deserialize(value, Object.class));
        }
        return targetMap;
    }

    private Class<?> findClass(String typeName) {
        Class<?> primitiveClass = findPrimitiveClass(typeName);
        if (primitiveClass != null) {
            return primitiveClass;
        }

        SerializationBValueProvider provider = bValueProvider.find(typeName);
        if (provider != null) {
            return provider.getType();
        }

        TypeInstanceProvider typeProvider = instanceProvider.findInstanceProvider(typeName);
        return typeProvider.getTypeClass();
    }

    /**
     * Comply with {@link java.io.Serializable} interface's {@code readResolve} method, if it's available.
     *
     * @param jBMap      Json Object structure to create the instance from.
     * @param targetType Type of object to be created.
     * @param object     Object instance on which the readResolve method to be executed.
     * @return if readResolve is available then the return value of it.
     */
    private Object readResolve(BMap<String, BValue> jBMap, Class<?> targetType, Object object) {
        Class<?> clz;
        if (targetType != null) {
            clz = targetType;
        } else {
            String targetTypeName = resolveTargetTypeName(null, jBMap);
            clz = findClass(targetTypeName);
        }

        if (clz != null && Serializable.class.isAssignableFrom(clz)) {
            return ObjectHelper.invokeReadResolveOn(object, clz);
        }
        return null;
    }
}
