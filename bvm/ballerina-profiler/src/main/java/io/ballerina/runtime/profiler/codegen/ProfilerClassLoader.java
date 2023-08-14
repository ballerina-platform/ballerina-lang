/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.profiler.codegen;

import io.ballerina.runtime.profiler.util.ProfilerException;
import org.objectweb.asm.ClassReader;

/**
 * This class is used as a class loader for the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class ProfilerClassLoader extends ClassLoader {

    public ProfilerClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void loadClass(byte[] code) {
        String name = readClassName(code);
        try {
            if (!name.equals("module-info")) {
                defineClass(name, code, 0, code.length);
            }
        } catch (Throwable e) {
            throw new ProfilerException("error while loading class: " + name, e);
        }
    }

    public String readClassName(final byte[] byteCode) {
        return new ClassReader(byteCode).getClassName().replace("/", ".");
    }
}
