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
package org.ballerinalang.model.util.serializer;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Dynamically generate {@link TypeInstanceProvider} for given class.
 */
public class TypeInstanceProviderFactory {
    /**
     * Given fully qualified class name get {@link TypeInstanceProvider} dynamically implemented.
     *
     * @param fullClassName class name of requested {@link TypeInstanceProvider}
     * @return
     */
    public TypeInstanceProvider createProvider(String fullClassName) {
        try {
            Class<?> clazz = Class.forName(fullClassName);
            assertInstantiable(clazz);
            try {
                Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
                if (declaredConstructor == null) {
                    return null;
                }
                TypeInstanceProvider implement = implementUsingNoParamConstructor(clazz, declaredConstructor);
                if (implement != null) {
                    return implement;
                }
            } catch (NoSuchMethodException e) {
                // When no-param constructor is not found, try unsafe allocator.
                return implementUnsafe(clazz);
            }
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private void assertInstantiable(Class<?> clazz) {
        if (!ObjectHelper.isInstantiable(clazz)) {
            throw new BallerinaException("Can not generate instance provider for un-instantiable class: " +
                    clazz.getName());
        }
    }

    private TypeInstanceProvider implementUsingNoParamConstructor(Class<?> clazz, Constructor<?> constructor) {
        return new TypeInstanceProvider() {
            @Override
            public String getTypeName() {
                return clazz.getSimpleName();
            }

            @Override
            public Object newInstance() {
                try {
                    constructor.setAccessible(true);
                    return constructor.newInstance(null);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    return null;
                }
            }

            @Override
            public Class getTypeClass() {
                return clazz;
            }
        };
    }

    private TypeInstanceProvider implementUnsafe(Class<?> clazz) {
        return new TypeInstanceProvider() {
            @Override
            public String getTypeName() {
                return clazz.getSimpleName();
            }

            @Override
            public Object newInstance() {
                try {
                    return UnsafeObjectAllocator.allocateFor(clazz);
                } catch (ClassNotFoundException
                        | NoSuchFieldException
                        | NoSuchMethodException
                        | IllegalAccessException
                        | InvocationTargetException e) {
                    // Not even sun.misc.Unsafe can create a instance of this class
                    // only option is to provide a SerializationBValueProvider or
                    // TypeInstanceProvider for this type.
                    throw new BallerinaException(
                            String.format("%s cannot instantiate object of %s, maybe add a %s",
                                    JsonSerializer.class.getSimpleName(),
                                    clazz.getName(),
                                    TypeInstanceProvider.class.getSimpleName()));
                }
            }

            @Override
            public Class getTypeClass() {
                return clazz;
            }
        };
    }
}
