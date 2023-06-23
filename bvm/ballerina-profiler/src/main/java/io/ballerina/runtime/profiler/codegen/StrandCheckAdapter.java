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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class StrandCheckAdapter extends AdviceAdapter {
    String profilerOwner = "io/ballerina/runtime/profiler/runtime/Profiler";
    String profilerDescriptor = "()Lio/ballerina/runtime/profiler/runtime/Profiler;";
    Label tryStart = new Label();
    int load;

    /**
     Constructor for MethodWrapperAdapter
     @param access - access flag of the method that is wrapped
     @param mv - MethodVisitor instance to generate the bytecode
     @param methodName - name of the method that is wrapped
     @param description - description of the method that is wrapped
     @param load - the index of the local variable that holds the strand instance
     */

    public StrandCheckAdapter(int access, MethodVisitor mv, String methodName, String description, int load) {
        super(Opcodes.ASM9, mv, access, methodName, description);
        if (load == 0){
            this.load = 1;
        }else {
            this.load = 0;
        }
    }

    /*  This method is called when the visitCode() method is called on the MethodVisitor
    It adds a label to the try block of the wrapped method  */
    public void visitCode() {
        super.visitCode();
        mv.visitLabel(tryStart);
    }

    /*  This method is called when the wrapped method is entered
    It retrieves the profiler instance, gets the strand id and starts the profiling */
    protected void onMethodEnter() {
        mv.visitMethodInsn(INVOKESTATIC, profilerOwner, "getInstance", profilerDescriptor, false);
        mv.visitVarInsn(ALOAD, load);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "getId", "()I", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/profiler/runtime/Profiler", "start", "(I)V", false);
    }

    /**
     This method is called when the wrapped method is exited
     If the exit is not due to an exception, it calls the onFinally method
     @param opcode - the opcode of the instruction that caused the method exit
     */
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            onFinally();
        }
    }

    /**
     This method is called to generate the max stack and max locals for the wrapped method
     It adds a try-catch block to the wrapped method and calls the onFinally method in the catch block
     @param maxStack - the maximum stack size
     @param maxLocals - the maximum number of local variables
     */
    public void visitMaxs(int maxStack, int maxLocals) {
        Label tryEnd = new Label();
        mv.visitTryCatchBlock(tryStart, tryEnd, tryEnd, null);
        mv.visitLabel(tryEnd);
        onFinally();
        mv.visitInsn(ATHROW);
        mv.visitMaxs(-1, -1);
    }

    /*  This method stops the profiling for the wrapped method
    It retrieves the profiler instance, gets the strand state and id, and stops the profiling   */
    private void onFinally() {
        mv.visitMethodInsn(INVOKESTATIC, profilerOwner, "getInstance", profilerDescriptor, false);
        mv.visitVarInsn(ALOAD, load);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "getState", "()Lio/ballerina/runtime/internal/scheduling/State;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/State", "toString", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ALOAD, load);
        mv.visitMethodInsn(INVOKEVIRTUAL, "io/ballerina/runtime/internal/scheduling/Strand", "getId", "()I", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, profilerOwner, "stop", "(Ljava/lang/String;I)V", false);
    }
}