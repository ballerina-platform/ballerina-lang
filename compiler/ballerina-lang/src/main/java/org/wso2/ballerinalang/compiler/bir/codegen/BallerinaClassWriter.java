/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.objectweb.asm.ClassWriter;

/**
 * Class Writer for generating ballerina classes.
 * <p>
 * since 1.2.0
 */
public class BallerinaClassWriter extends ClassWriter {

    private static final String OBJECT_CLASS = "java/lang/Object";

    public BallerinaClassWriter(int flags) {

        super(flags);
    }

    /**
     * Returns the common super type of the two given types. If any of the classes are
     * not found, assume its a generated or to-be generated class and return
     * {@link Object} as the super type.
     */
    @Override
    protected String getCommonSuperClass(String type1, String type2) {

        ClassLoader classLoader = getClassLoader();
        Class<?> class1;

        try {
            class1 = Class.forName(type1.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            return OBJECT_CLASS;
        }

        Class<?> class2;
        try {
            class2 = Class.forName(type2.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            return OBJECT_CLASS;
        }

        if (class1.isAssignableFrom(class2)) {
            return type1;
        } else if (class2.isAssignableFrom(class1)) {
            return type2;
        } else if (class1.isInterface() || class2.isInterface()) {
            return OBJECT_CLASS;
        }

        class1 = class1.getSuperclass();
        while (!class1.isAssignableFrom(class2)) {
            class1 = class1.getSuperclass();
        }

        return class1.getName().replace('.', '/');
    }
}
