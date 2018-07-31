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
package org.ballerinalang.persistence.serializable.serializer;

import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.serializer.type.BStringSerializationProvider;
import org.ballerinalang.persistence.serializable.serializer.type.ListSerializationProvider;
import org.ballerinalang.persistence.serializable.serializer.type.MapSerializationProvider;
import org.ballerinalang.persistence.serializable.serializer.type.SerializableBMapSerializationProvider;
import org.ballerinalang.persistence.serializable.serializer.type.SerializableContextSerializationProvider;
import org.ballerinalang.persistence.serializable.serializer.type.SerializableStateSerializationProvider;
import org.ballerinalang.persistence.serializable.serializer.type.SerializableWorkerDataSerializationProvider;
import org.ballerinalang.persistence.serializable.serializer.type.WorkerStateSerializationProvider;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Reconstruct Java object tree from JSON input.
 */
public class JsonDeserializer {
    private final SerializationProviderRegistry serializationProviderRegistry;
    private JsonNode jsonNode;
    private static final Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);

    JsonDeserializer(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
        serializationProviderRegistry = SerializationProviderRegistry.getInstance();
        registerTypeSerializationProviders(serializationProviderRegistry);
    }

    private void registerTypeSerializationProviders(SerializationProviderRegistry registry) {
        registry.addTypeProvider(new SerializableStateSerializationProvider());
        registry.addTypeProvider(new SerializableWorkerDataSerializationProvider());
        registry.addTypeProvider(new SerializableContextSerializationProvider());
        registry.addTypeProvider(new MapSerializationProvider());
        registry.addTypeProvider(new ListSerializationProvider());
        registry.addTypeProvider(new WorkerStateSerializationProvider());
        registry.addTypeProvider(new SerializableBMapSerializationProvider());
        registry.addTypeProvider(new BStringSerializationProvider());
    }

    public SerializableState deserialize() {
        return (SerializableState) deserialize(jsonNode);
    }

    private Object deserialize(JsonNode jsonNode) {
        switch (jsonNode.getType()) {
            case OBJECT:
                Object object = createInstance(jsonNode);
                return deserializeObject(jsonNode, object);
            case ARRAY:
                return deserializeArray(jsonNode);
            case STRING:
                return jsonNode.stringValue();
            case LONG:
                return jsonNode.longValue();
            case DOUBLE:
                return jsonNode.doubleValue();
            case BOOLEAN:
                return jsonNode.booleanValue();
            case NULL:
            default:
                return null;
        }
    }

    private Object createInstance(JsonNode jsonNode) {
        JsonNode typeNode = jsonNode.get("type");
        if (typeNode != null) {
            if (isEnum(typeNode)) {
                return createEnumInstance(jsonNode);
            }
            String type = typeNode.stringValue();
            return getObjectOf(type);
        }
        return null;
    }

    private Object createEnumInstance(JsonNode jsonNode) {
        String enumName = jsonNode.get("payload").stringValue();
        String[] frag = enumName.split("\\.");
        String type = frag[0];
        Class enumClass = serializationProviderRegistry.findTypeProvider(type).getTypeClass();
        String enumConst = frag[1];
        return Enum.valueOf(enumClass, enumConst);
    }

    private boolean isEnum(JsonNode typeNode) {
        return typeNode.stringValue() != null && typeNode.stringValue().toLowerCase().equals("enum");
    }

    private Object getObjectOf(String type) {
        TypeSerializationProvider typeProvider = serializationProviderRegistry.findTypeProvider(type);
        if (typeProvider != null) {
            return typeProvider.newInstance();
        }
        return null;
    }

    private Object deserializeArray(JsonNode jsonNode) {
        Object[] array = new Object[jsonNode.size()];
        for (int i = 0; i < jsonNode.size(); i++) {
            array[i] = deserialize(jsonNode.get(i));
        }
        return array;
    }

    private Object deserializeObject(JsonNode jsonNode, Object object) {
        String objType = jsonNode.get("type").stringValue();
        JsonNode payload = jsonNode.get("payload");

        if ("map".equals(objType.toLowerCase())) {
            return deserializeMap(payload, (Map) object);
        } else if ("list".equals(objType.toLowerCase())) {
            return deserializeList(payload, object);
        } else if ("enum".equals(objType.toLowerCase())) {
            return object;
        }

        for (Iterator<String> nameIter = payload.fieldNames(); nameIter.hasNext();) {
            String name = nameIter.next();
            JsonNode fieldNode = payload.get(name);
            setField(object, name, fieldNode);
        }
        return object;
    }

    private void setField(Object target, String fieldName, JsonNode fieldNode) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object obj = deserialize(fieldNode);
            field.set(target, cast(obj, field.getType()));
        } catch (NoSuchFieldException e) {
            String message = String.format("Field: %s is not found in %s class",
                    fieldName, target.getClass().getSimpleName());
            logger.error(message);
            throw new BallerinaException(String.format("Error while SerializableState reconstruction: %s", message));
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage());
            throw new BallerinaException();
        }
    }

    /**
     * Convert to desired type if special conditions are matched.
     * @param obj
     * @param targetType
     * @return
     */
    private Object cast(Object obj, Class targetType) {
        // JsonParser always treat integer numbers as longs, if target field is int then cast to int.
        if ((targetType == Integer.class && obj.getClass() == Long.class)
        || (targetType.getName().equals("int") && obj.getClass() == Long.class)) {
            return ((Long) obj).intValue();
        }

        // JsonParser always treat float numbers as doubles, if target field is float then cast to float.
        if ((targetType == Float.class && obj.getClass() == Double.class)
        || (targetType.getName().equals("float") && obj.getClass() == Double.class)) {
            return ((Double) obj).floatValue();
        }

        return obj;
    }

    private Object deserializeList(JsonNode payload, Object targetList) {
        if (targetList instanceof List) {
            List list = (List) targetList;
            Object[] array = (Object[]) deserializeArray(payload);
            for (int i = 0; i < array.length; i++) {
                list.add(array[i]);
            }
        }
        return targetList;
    }

    private Object deserializeMap(JsonNode payload, Map map) {
        Iterator<Map.Entry<String, JsonNode>> fields = payload.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            map.put(entry.getKey(), deserialize(entry.getValue()));
        }
        return map;
    }
}
