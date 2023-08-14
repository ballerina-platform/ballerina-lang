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

import io.ballerina.runtime.profiler.Main;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static io.ballerina.runtime.profiler.util.Constants.STRAND_ARG;

/**
 * This class is used as a class visitor for the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class ProfilerClassVisitor extends ClassVisitor {

    public ProfilerClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (!name.startsWith("$") && desc.startsWith(STRAND_ARG)) {
            Main.incrementBalFunctionCount();
        }
        if (desc.startsWith(STRAND_ARG)) {
            return new StrandCheckAdapter(access, methodVisitor, name, desc, (access & Opcodes.ACC_STATIC));
        }
        return new NonStrandCheckAdapter(access, methodVisitor, name, desc);
    }
}
