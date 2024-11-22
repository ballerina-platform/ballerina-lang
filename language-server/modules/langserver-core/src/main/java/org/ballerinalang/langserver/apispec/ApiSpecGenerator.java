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

package org.ballerinalang.langserver.apispec;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates LS API specifications for JSON-RPC methods.
 *
 * @since 2201.12.0
 */
public class ApiSpecGenerator {

    /**
     * Generates a JSON object representing the API specification for a given JSON-RPC method.
     *
     * @param method the JSON-RPC method to generate the specification for
     * @return a JSON object representing the API specification
     */
    public static JsonObject generate(JsonRpcMethod method) {
        MethodSchema methodSchema = new MethodSchema(
                method.getMethodName(),
                convertParameters(method.getParameterTypes()),
                method.getReturnType() != null ? convertType(method.getReturnType()) : null
        );

        return new Gson().toJsonTree(methodSchema).getAsJsonObject();
    }

    private static List<ParameterSchema> convertParameters(Type[] parameterTypes) {
        if (parameterTypes == null) {
            return List.of();
        }

        List<ParameterSchema> parameters = new ArrayList<>();
        for (int i = 0; i < parameterTypes.length; i++) {
            parameters.add(new ParameterSchema(
                    "param" + i,
                    convertType(parameterTypes[i])
            ));
        }
        return parameters;
    }

    private static TypeSchema convertType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        return processType(type, new HashSet<>());
    }

    private static TypeSchema processType(Type type, Set<Type> processedTypes) {
        // Handle cyclic references
        if (processedTypes.contains(type)) {
            return TypeSchema.object(null, type.getTypeName());
        }
        Set<Type> newProcessedTypes = new HashSet<>(processedTypes);
        newProcessedTypes.add(type);

        if (type instanceof Class<?>) {
            return handleClass((Class<?>) type, newProcessedTypes);
        } else if (type instanceof ParameterizedType) {
            return handleParameterizedType((ParameterizedType) type, newProcessedTypes);
        } else if (type instanceof TypeVariable) {
            return handleTypeVariable((TypeVariable<?>) type);
        } else if (type instanceof WildcardType) {
            return handleWildcardType((WildcardType) type, newProcessedTypes);
        }

        return TypeSchema.object(null, type.getTypeName());
    }

    private static TypeSchema handleClass(Class<?> clazz, Set<Type> processedTypes) {
        if (clazz.isPrimitive()) {
            return handlePrimitive(clazz);
        } else if (clazz.isEnum()) {
            return handleEnum(clazz);
        } else if (clazz.isArray()) {
            return TypeSchema.array(
                    processType(clazz.getComponentType(), processedTypes),
                    clazz.getTypeName()
            );
        } else if (Collection.class.isAssignableFrom(clazz)) {
            return TypeSchema.array(
                    TypeSchema.object(null, "java.lang.Object"),
                    clazz.getTypeName()
            );
        } else if (Map.class.isAssignableFrom(clazz)) {
            return TypeSchema.map(
                    TypeSchema.object(null, "java.lang.Object"),
                    clazz.getTypeName()
            );
        } else if (clazz.equals(String.class)) {
            return TypeSchema.string(clazz.getName());
        } else if (Number.class.isAssignableFrom(clazz)) {
            return TypeSchema.number(clazz.getTypeName());
        } else if (Boolean.class.equals(clazz)) {
            return TypeSchema.bool(clazz.getTypeName());
        } else {
            return handleObject(clazz, processedTypes);
        }
    }

    private static TypeSchema handlePrimitive(Class<?> clazz) {
        if (clazz == int.class || clazz == long.class || clazz == short.class || clazz == byte.class) {
            return TypeSchema.integer(clazz.getName());
        } else if (clazz == double.class || clazz == float.class) {
            return TypeSchema.number(clazz.getName());
        } else if (clazz == boolean.class) {
            return TypeSchema.bool(clazz.getName());
        } else if (clazz == char.class) {
            return TypeSchema.string(clazz.getName());
        }
        throw new IllegalArgumentException("Unsupported primitive type: " + clazz);
    }

    private static TypeSchema handleEnum(Class<?> clazz) {
        return TypeSchema.enumSchema(
                Arrays.stream(clazz.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.toList()),
                clazz.getTypeName()
        );
    }

    private static TypeSchema handleObject(Class<?> clazz, Set<Type> processedTypes) {
        Map<String, TypeSchema> properties = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            properties.put(field.getName(), processType(field.getGenericType(), processedTypes));
        }

        return TypeSchema.object(properties, clazz.getTypeName());
    }

    private static TypeSchema handleParameterizedType(ParameterizedType type, Set<Type> processedTypes) {
        Type rawType = type.getRawType();
        if (rawType instanceof Class<?>) {
            Class<?> rawClass = (Class<?>) rawType;
            if (Collection.class.isAssignableFrom(rawClass)) {
                return TypeSchema.array(
                        processType(type.getActualTypeArguments()[0], processedTypes),
                        type.getTypeName()
                );
            } else if (Map.class.isAssignableFrom(rawClass)) {
                return TypeSchema.map(
                        processType(type.getActualTypeArguments()[1], processedTypes),
                        type.getTypeName()
                );
            }
            return handleClass(rawClass, processedTypes);
        }
        return TypeSchema.object(null, type.getTypeName());
    }

    private static TypeSchema handleTypeVariable(TypeVariable<?> type) {
        Type[] bounds = type.getBounds();
        return TypeSchema.object(null,
                bounds.length > 0 ? bounds[0].getTypeName() : "java.lang.Object");
    }

    private static TypeSchema handleWildcardType(WildcardType type, Set<Type> processedTypes) {
        Type[] upperBounds = type.getUpperBounds();
        if (upperBounds.length > 0) {
            return processType(upperBounds[0], processedTypes);
        }
        return TypeSchema.object(null, "java.lang.Object");
    }
}
