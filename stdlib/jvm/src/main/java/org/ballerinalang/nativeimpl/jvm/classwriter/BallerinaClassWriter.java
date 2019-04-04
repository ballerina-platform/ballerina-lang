/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.jvm.classwriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * Class Writer for generating ballerina classes.
 * 
 * since 0.995.0
 */
public class BallerinaClassWriter extends ClassWriter {

    // A map containing already generated classes, and their super class.
    private static Map<String, String> generatedClasses = new HashMap<>();

    public BallerinaClassWriter(int flags) {
        super(flags);
    }

    public BallerinaClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
    }

    public final void visitClass(int version, int access, String className, String signature, String superName,
                                 String[] interfaces) {
        generatedClasses.put(className, superName);
        super.visit(version, access, className, signature, superName, interfaces);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        if (generatedClasses.containsKey(type1)) {
            type1 = generatedClasses.get(type1);
        }

        if (generatedClasses.containsKey(type2)) {
            type1 = generatedClasses.get(type2);
        }

        return super.getCommonSuperClass(type1, type2);
    }
}
