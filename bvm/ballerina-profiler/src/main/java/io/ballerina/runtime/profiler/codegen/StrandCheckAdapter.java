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

import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.profiler.runtime.Data;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import static io.ballerina.runtime.profiler.util.Constants.DATA_CLASS;
import static io.ballerina.runtime.profiler.util.Constants.GET_INSTANCE_DESCRIPTOR;
import static io.ballerina.runtime.profiler.util.Constants.PROFILE_ANALYZER;
import static io.ballerina.runtime.profiler.util.Constants.START_PROFILE_DESCRIPTOR;
import static io.ballerina.runtime.profiler.util.Constants.STOP_PROFILE_DESCRIPTOR;

/**
 * This class is used as the advice adapter for the Ballerina profiler.
 * This will wrap the ballerina methods with
 * {@link io.ballerina.runtime.profiler.runtime.ProfileAnalyzer#start(Strand, String, String)} and
 * {@link io.ballerina.runtime.profiler.runtime.ProfileAnalyzer#stop(Strand, Data)} methods.
 *
 * @since 2201.8.0
 */
public class StrandCheckAdapter extends AdviceAdapter {

    private final String className;
    private final String methodName;
    private final Type dataType;
    Label tryStart = new Label();
    int load;
    int stackKeyIndex;

    public StrandCheckAdapter(String className, int access, MethodVisitor mv, String methodName,
                              String description, int load) {
        super(Opcodes.ASM9, mv, access, methodName, description);
        this.className = className;
        this.methodName = methodName;
        if (load == 0) {
            this.load = 1;
        } else {
            this.load = 0;
        }
        this.dataType = Type.getObjectType(DATA_CLASS);
    }

    // It adds a label to the try block of the wrapped method.
    @Override
    public void visitCode() {
        super.visitCode();
        mv.visitLabel(tryStart);
    }

    // It retrieves the profiler instance, gets the strand id and starts the profiling.
    @Override
    protected void onMethodEnter() {
        this.stackKeyIndex = this.newLocal(dataType);
        mv.visitMethodInsn(INVOKESTATIC, PROFILE_ANALYZER, "getInstance", GET_INSTANCE_DESCRIPTOR, false);
        mv.visitVarInsn(ALOAD, load);
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitMethodInsn(INVOKEVIRTUAL, PROFILE_ANALYZER, "start", START_PROFILE_DESCRIPTOR, false);
        mv.visitVarInsn(ASTORE, this.stackKeyIndex);
    }

    // If the exit is not due to an exception, it calls the onFinally method.
    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            onFinally();
        }
    }

    // This method is called to generate the max stack and max locals for the wrapped method.
    // It adds a try-catch block to the wrapped method and calls the onFinally method in the catch block.
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        Label tryEnd = new Label();
        mv.visitTryCatchBlock(tryStart, tryEnd, tryEnd, null);
        mv.visitLabel(tryEnd);
        onFinally();
        mv.visitInsn(ATHROW);
        mv.visitMaxs(-1, -1);
    }

    // This method stops the profiling for the wrapped method.
    // It retrieves the profiler instance, gets the strand state and id, and stops the profiling.
    private void onFinally() {
        mv.visitMethodInsn(INVOKESTATIC, PROFILE_ANALYZER, "getInstance", GET_INSTANCE_DESCRIPTOR, false);
        mv.visitVarInsn(ALOAD, load);
        mv.visitVarInsn(ALOAD, this.stackKeyIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, PROFILE_ANALYZER, "stop", STOP_PROFILE_DESCRIPTOR, false);
    }
}
