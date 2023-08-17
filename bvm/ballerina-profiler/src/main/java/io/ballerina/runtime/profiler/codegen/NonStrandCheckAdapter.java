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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import static io.ballerina.runtime.profiler.util.Constants.GET_INSTANCE_DESCRIPTOR;
import static io.ballerina.runtime.profiler.util.Constants.PROFILE_ANALYZER;

/**
 * This class is used as the advice adapter for the Ballerina profiler.
 * This class only manages the functions that doesn't contain the strand parameter.
 *
 * @since 2201.8.0
 */
public class NonStrandCheckAdapter extends AdviceAdapter {

    /**
     * Constructor for MethodWrapperAdapter.
     *
     * @param access      access flag of the method that is wrapped
     * @param mv          MethodVisitor instance to generate the bytecode
     * @param methodName  name of the method that is wrapped
     * @param description description of the method that is wrapped
     */
    public NonStrandCheckAdapter(int access, MethodVisitor mv, String methodName, String description) {
        super(Opcodes.ASM9, mv, access, methodName, description);
    }

    @Override
    protected void onMethodEnter() {
        mv.visitMethodInsn(INVOKESTATIC, PROFILE_ANALYZER, "getInstance", GET_INSTANCE_DESCRIPTOR, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, PROFILE_ANALYZER, "start", "()V", false);
    }

    @Override
    protected void onMethodExit(int opcode) {
        mv.visitMethodInsn(INVOKESTATIC, PROFILE_ANALYZER, "getInstance", GET_INSTANCE_DESCRIPTOR, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, PROFILE_ANALYZER, "stop", "()V", false);
    }
}
