/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.runtime.profiler.codegen;

import org.objectweb.asm.ClassReader;

public class CustomClassLoader extends ClassLoader {

    public CustomClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> loadClass(byte[] code) {
        Class<?> classOut = null;

        String name = readClassName(code);

        try {
            classOut = defineClass(name, code, 0, code.length);
        } catch (Error e) {
            System.out.println(name);
        }
        return classOut;
    }

    public String readClassName(final byte[] byteCode) {
        String className;
        className = new ClassReader(byteCode).getClassName().replace("/", ".");
        return className;
    }
}