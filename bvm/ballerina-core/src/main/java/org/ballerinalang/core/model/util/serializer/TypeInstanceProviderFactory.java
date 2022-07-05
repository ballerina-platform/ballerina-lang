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

import org.ballerinalang.core.util.exceptions.BallerinaException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Dynamically generate {@link TypeInstanceProvider} for given class.
 *
 * @since 0.982.0
 */
public class TypeInstanceProviderFactory {
    /**
     * Given fully qualified class name get {@link TypeInstanceProvider} dynamically implemented.
     *
     * @param fullClassName class name of requested {@link TypeInstanceProvider}
     * @return dynamically implemented {@link TypeInstanceProvider}
     */
    public TypeInstanceProvider from(String fullClassName) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullClassName);
            assertInstantiable(clazz);
            Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
            return createInstanceProvider(declaredConstructor, clazz);
        } catch (NoSuchMethodException e) {
            // If no-param constructor is not found, try unsafe allocator.
            return createInstanceProvider(clazz);
        } catch (ClassNotFoundException e) {
            // return null to indicate TypeInstanceProviderFactory cannot create a TypeInstanceProvider for this type.
            return null;
        }
    }

    private void assertInstantiable(Class<?> clazz) {
        if (!ObjectHelper.isInstantiable(clazz)) {
            throw new BallerinaException("Can not generate instance provider for un-instantiable class: " +
                    clazz.getName());
        }
    }

    private TypeInstanceProvider createInstanceProvider(Constructor<?> constructor, Class<?> clazz) {
        return new TypeInstanceProvider() {
            @Override
            public String getTypeName() {
                return clazz.getSimpleName();
            }

            @Override
            public Object newInstance() {
                try {
                    constructor.setAccessible(true);
                    return constructor.newInstance();
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

    private TypeInstanceProvider createInstanceProvider(Class<?> clazz) {
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
                    // Control reaching this point means all attempts at creating an instance of this class has failed.
                    // Only option is to provide a SerializationBValueProvider or TypeInstanceProvider for this type.
                    throw new BallerinaException(String.format("%s cannot instantiate object of %s, maybe add a %s",
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
