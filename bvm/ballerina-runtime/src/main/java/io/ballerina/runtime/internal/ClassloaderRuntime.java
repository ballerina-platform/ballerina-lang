/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.internal.values.ValueCreator;

import java.lang.reflect.InvocationTargetException;

/**
 * This class represents the Ballerina runtime that is created using a classloader for internal purposes.
 *
 * @since 2201.11.0
 */
public class ClassloaderRuntime extends BalRuntime {

    private final ClassLoader classLoader;

    public ClassloaderRuntime(Module module, ClassLoader classLoader) {
        super(module);
        this.classLoader = classLoader;
    }

    @Override
    Class<?> loadClass(String className) throws ClassNotFoundException {
        String name = getFullQualifiedClassName(module, className);
        return Class.forName(name, true, classLoader);
    }

    @Override
    void invokeModuleStop() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        super.invokeModuleStop();
        String lookupKey = ValueCreator.getLookupKey(module.getOrg(), module.getName(), module.getMajorVersion(),
                module.isTestPkg());
        ValueCreator.removeValueCreator(lookupKey);
    }
}