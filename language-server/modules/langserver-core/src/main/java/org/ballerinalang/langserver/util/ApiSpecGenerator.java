/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.langserver.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ApiSpecGenerator {

    public static JsonObject convertJsonRpcMethod(JsonRpcMethod method) {
        Gson gson = new Gson();
        JsonObject result = new JsonObject();

        // Add method name
        result.addProperty("method", method.getMethodName());

        // Convert parameter types to array
        Type[] parameterTypes = method.getParameterTypes();
        JsonArray parameters = new JsonArray();
        if (parameterTypes != null) {
            for (Type paramType : parameterTypes) {
                Map<String, Object> convertedType = convertType(paramType);
                parameters.add(gson.toJsonTree(convertedType));
            }
        }
        result.add("parameters", parameters);

        // Convert return type
        Type returnType = method.getReturnType();
        if (returnType != null) {
            Object convertedReturnType = convertType(returnType);
            result.add("return", gson.toJsonTree(convertedReturnType));
        }

        return result;
    }

    private static Map<String, Object> convertType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        return processType(type, new HashSet<>());
    }

    private static Map<String, Object> processType(Type type, Set<Type> processedTypes) {
        Map<String, Object> schema = new HashMap<>();

        // Handle cyclic references
        if (processedTypes.contains(type)) {
            schema.put("type", "object");
            return schema;
        }
        processedTypes.add(type);

        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            handleClass(clazz, schema, processedTypes);
        } else if (type instanceof ParameterizedType) {
            handleParameterizedType((ParameterizedType) type, schema, processedTypes);
        } else if (type instanceof TypeVariable) {
            handleTypeVariable((TypeVariable<?>) type, schema);
        } else if (type instanceof WildcardType) {
            handleWildcardType((WildcardType) type, schema, processedTypes);
        }

        return schema;
    }

    private static void handleClass(Class<?> clazz, Map<String, Object> schema, Set<Type> processedTypes) {
        if (clazz.isPrimitive()) {
            handlePrimitive(clazz, schema);
        } else if (clazz.isEnum()) {
            handleEnum(clazz, schema);
        } else if (clazz.isArray()) {
            handleArray(clazz.getComponentType(), schema, processedTypes);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            schema.put("type", "array");
            schema.put("items", new HashMap<String, Object>() {{
                put("type", "object");
            }});
        } else if (Map.class.isAssignableFrom(clazz)) {
            handleMap(schema);
        } else if (clazz.equals(String.class)) {
            schema.put("type", "string");
        } else if (Number.class.isAssignableFrom(clazz)) {
            schema.put("type", "number");
        } else if (Boolean.class.equals(clazz)) {
            schema.put("type", "boolean");
        } else if (Date.class.isAssignableFrom(clazz)) {
            schema.put("type", "string");
            schema.put("format", "date-time");
        } else {
            handleObject(clazz, schema, processedTypes);
        }
    }

    private static void handlePrimitive(Class<?> clazz, Map<String, Object> schema) {
        if (clazz == int.class || clazz == long.class || clazz == short.class || clazz == byte.class) {
            schema.put("type", "integer");
        } else if (clazz == double.class || clazz == float.class) {
            schema.put("type", "number");
        } else if (clazz == boolean.class) {
            schema.put("type", "boolean");
        } else if (clazz == char.class) {
            schema.put("type", "string");
            schema.put("maxLength", 1);
        }
    }

    private static void handleEnum(Class<?> clazz, Map<String, Object> schema) {
        schema.put("type", "string");
        schema.put("enum", Arrays.asList(clazz.getEnumConstants()));
    }

    private static void handleArray(Type componentType, Map<String, Object> schema, Set<Type> processedTypes) {
        schema.put("type", "array");
        schema.put("items", processType(componentType, processedTypes));
    }

    private static void handleMap(Map<String, Object> schema) {
        schema.put("type", "object");
        schema.put("additionalProperties", true);
    }

    private static void handleObject(Class<?> clazz, Map<String, Object> schema, Set<Type> processedTypes) {
        schema.put("type", "object");
        Map<String, Object> properties = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            String fieldName = getFieldName(field);
            properties.put(fieldName, processType(field.getGenericType(), processedTypes));
        }

        if (!properties.isEmpty()) {
            schema.put("properties", properties);
        }
    }

    private static void handleParameterizedType(ParameterizedType type, Map<String, Object> schema,
                                                Set<Type> processedTypes) {
        Type rawType = type.getRawType();
        if (rawType instanceof Class<?>) {
            Class<?> rawClass = (Class<?>) rawType;
            if (Collection.class.isAssignableFrom(rawClass)) {
                schema.put("type", "array");
                Type itemType = type.getActualTypeArguments()[0];
                schema.put("items", processType(itemType, processedTypes));
            } else if (Map.class.isAssignableFrom(rawClass)) {
                schema.put("type", "object");
                Type valueType = type.getActualTypeArguments()[1];
                schema.put("additionalProperties", processType(valueType, processedTypes));
            } else {
                handleClass(rawClass, schema, processedTypes);
            }
        }
    }

    private static void handleTypeVariable(TypeVariable<?> type, Map<String, Object> schema) {
        // For type variables, we use the first bound or default to object
        Type[] bounds = type.getBounds();
        if (bounds.length > 0) {
            schema.put("type", "object");  // Default to object for generic types
        } else {
            schema.put("type", "object");
        }
    }

    private static void handleWildcardType(WildcardType type, Map<String, Object> schema,
                                           Set<Type> processedTypes) {
        Type[] upperBounds = type.getUpperBounds();
        if (upperBounds.length > 0) {
            Map<String, Object> boundSchema = processType(upperBounds[0], processedTypes);
            schema.putAll(boundSchema);
        } else {
            schema.put("type", "object");
        }
    }

    private static String getFieldName(Field field) {
        return field.getName();
    }
}