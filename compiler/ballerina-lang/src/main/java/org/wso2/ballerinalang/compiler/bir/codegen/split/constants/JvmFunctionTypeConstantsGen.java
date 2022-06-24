/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.split.constants;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_FUNCTION_TYPE_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_CONSTANT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_FIELDS_PER_SPLIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;

/**
 * Generates Jvm class for the ballerina function type constants for given module.
 *
 * @since 2.0.0
 */
public class JvmFunctionTypeConstantsGen {

    private final String functionTypeConstantClass;

    private final List<BIRNode.BIRFunction> functions;

    private JvmTypeGen jvmTypeGen;

    public JvmFunctionTypeConstantsGen(PackageID module, List<BIRNode.BIRFunction> functions) {
        this.functionTypeConstantClass = JvmCodeGenUtil.getModuleLevelClassName(module,
                FUNCTION_TYPE_CONSTANT_CLASS_NAME);
        this.functions = functions;
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, functionTypeConstantClass, null, OBJECT, null);

        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, JVM_INIT_METHOD, "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT, JVM_INIT_METHOD, "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        functions.forEach(func -> visitFunctionTypeFields(cw, func.name.value));
        generateFunctionTypeInits(cw);
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(functionTypeConstantClass + ".class", cw.toByteArray());
    }

    private void visitFunctionTypeFields(ClassWriter cw, String functionName) {
        FieldVisitor fv;
        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, getFunctionTypeVar(functionName),
                JvmSignatures.GET_FUNCTION_TYPE, null, null);
        fv.visitEnd();
    }

    private void generateFunctionTypeInits(ClassWriter cw) {
        MethodVisitor mv = null;
        int functionTypeCount = 0;
        int methodCount = 0;

        for (BIRNode.BIRFunction function : functions) {
            if (functionTypeCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC, B_FUNCTION_TYPE_INIT_METHOD_PREFIX + methodCount++, "()V", null, null);
            }
            jvmTypeGen.loadInvokableType(mv, function.type);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, functionTypeConstantClass, getFunctionTypeVar(function.name.value),
                    JvmSignatures.GET_FUNCTION_TYPE);
            functionTypeCount++;
            if (functionTypeCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        // Visit the previously started function init method if not ended.
        if (functionTypeCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, "()V", null, null);
        int methodIndex = (functions.size() - 1) / MAX_FIELDS_PER_SPLIT_METHOD;
        for (int i = 0; i <= methodIndex; i++) {
            mv.visitMethodInsn(INVOKESTATIC, functionTypeConstantClass, B_FUNCTION_TYPE_INIT_METHOD_PREFIX + i, "()V"
                    , false);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public String getFunctionTypeVar(String functionName) {
        return functionName + FUNCTION_TYPE_VAR_PREFIX;
    }

    public void setJvmTypeGen(JvmTypeGen jvmTypeGen) {
        this.jvmTypeGen = jvmTypeGen;
    }

    public String getFunctionTypeConstantClass() {
        return this.functionTypeConstantClass;
    }
}
