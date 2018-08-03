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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class UnsafeObjectAllocator {
    static Object allocateFor(Class<?> clazz) {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Object unsafe = f.get(null);
            Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
            if (isInstantiable(clazz)) {
                return clazz.cast(allocateInstance.invoke(unsafe, clazz));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static boolean isInstantiable(Class<?> clazz) {
        int modifiers = clazz.getModifiers();
        return !(Modifier.isInterface(modifiers) && Modifier.isAbstract(modifiers));
    }
}
