/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.HashSet;

/**
 * This visitor replaces unused functions with null
 */
public class DeadFunctionVisitor extends ClassVisitor {
    private static final String INIT_FUNCTION_IDENTIFIER = "init";
    private final HashSet<String> deadFunctionNames;
    public final HashSet<String> deletedFunctionNames;

    public DeadFunctionVisitor(int api, ClassWriter cw, HashSet<String> deadFunctionNames, HashSet<String> deletedFunctionNames) {
        super(api, cw);
        this.deadFunctionNames = deadFunctionNames;
        this.deletedFunctionNames = deletedFunctionNames;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = null;
        if (name.contains(INIT_FUNCTION_IDENTIFIER) || !deadFunctionNames.contains(name)) {
            methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        }
        deletedFunctionNames.add(name);
        return methodVisitor;
    }
}
