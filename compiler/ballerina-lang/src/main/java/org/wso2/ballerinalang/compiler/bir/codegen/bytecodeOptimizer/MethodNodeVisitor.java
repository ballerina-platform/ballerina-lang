/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.ballerinalang.compiler.bir.codegen.bytecodeOptimizer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

import static org.objectweb.asm.Opcodes.ASM9;

/**
 * A visitor class used to visit methods collect used class types.
 */
public class MethodNodeVisitor extends MethodVisitor {

    private DependencyCollector collector;

    public MethodNodeVisitor(DependencyCollector collector) {
        super(ASM9);
        this.collector = collector;
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationNodeVisitor(collector);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return getAnnotationNodeVisitor(collector, desc);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        return getAnnotationNodeVisitor(collector, desc);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return getAnnotationNodeVisitor(collector, desc);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        collector.addType(Type.getObjectType(type));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        collectOwnerAndDesc(collector, owner, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        collectOwnerAndDesc(collector, owner, desc);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        collector.addMethodDesc(desc);
        collector.addConstant(bsm);
        for (int i = 0; i < bsmArgs.length; i++) {
            collector.addConstant(bsmArgs[i]);
        }
    }

    @Override
    public void visitLdcInsn(Object constant) {
        collector.addConstant(constant);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        collector.addDesc(desc);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        collector.addTypeSignature(signature);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        if (type != null) {
            collector.addInternalName(type);
        }
    }

    private static AnnotationNodeVisitor getAnnotationNodeVisitor(DependencyCollector collector, String desc) {
        collector.addDesc(desc);
        return new AnnotationNodeVisitor(collector);
    }

    private static void collectOwnerAndDesc(DependencyCollector collector, String owner, String desc) {
        collector.addInternalName(owner);
        collector.addDesc(desc);
    }
}

