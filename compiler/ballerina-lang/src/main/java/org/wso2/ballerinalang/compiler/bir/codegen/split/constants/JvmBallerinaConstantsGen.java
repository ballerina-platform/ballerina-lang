/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONSTANTS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONSTANT_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_CONSTANTS_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.generateConstantsClassInit;

/**
 * Generates Jvm class for the used ballerina module constants for given module.
 *
 * @since 2.0.0
 */
public class JvmBallerinaConstantsGen {

    private final String constantClass;
    private final String moduleInitClass;
    private final JvmConstantsGen jvmConstantsGen;
    private final BIRNode.BIRPackage module;

    public JvmBallerinaConstantsGen(BIRNode.BIRPackage module, String moduleInitClass,
                                    JvmConstantsGen jvmConstantsGen) {
        this.moduleInitClass = moduleInitClass;
        this.constantClass = getModuleLevelClassName(module.packageID, CONSTANTS_CLASS_NAME);
        this.jvmConstantsGen = jvmConstantsGen;
        this.module = module;
    }

    public void generateConstantInit(Map<String, byte[]> jarEntries) {

        if (module.constants.isEmpty()) {
            return;
        }

        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        generateConstantsClassInit(cw, constantClass);
        // Create multiple module constant init methods based on module count.
        generateConstantsInits(cw);
        // Create static initializer which will call previously generated module init methods.
        generateConstantInitPublicMethod(cw);
        cw.visitEnd();
        jarEntries.put(constantClass + ".class", cw.toByteArray());
    }

    private void generateConstantsInits(ClassWriter cw) {
        MethodVisitor mv = null;
        int moduleCount = 0;
        int methodCount = 0;
        for (BIRNode.BIRConstant constant : module.constants) {
            if (moduleCount % MAX_CONSTANTS_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC, CONSTANT_INIT_METHOD_PREFIX + methodCount++, "()V", null, null);
            }
            setConstantField(mv, constant, moduleInitClass, jvmConstantsGen);
            moduleCount++;
            if (moduleCount % MAX_CONSTANTS_PER_METHOD == 0) {
                if (moduleCount != module.constants.size()) {
                    mv.visitMethodInsn(INVOKESTATIC, constantClass, CONSTANT_INIT_METHOD_PREFIX + methodCount,
                                       "()V", false);
                }
                genMethodReturn(mv);
            }
        }
        // Visit the previously started module init method if not ended.
        if (moduleCount % MAX_CONSTANTS_PER_METHOD != 0) {
            genMethodReturn(mv);
        }
    }

    private void generateConstantInitPublicMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC + ACC_PUBLIC, CONSTANT_INIT_METHOD_PREFIX, "()V", null, null);
        mv.visitMethodInsn(INVOKESTATIC, constantClass, CONSTANT_INIT_METHOD_PREFIX + 0, "()V", false);
        genMethodReturn(mv);
    }

    private static void setConstantField(MethodVisitor mv, BIRNode.BIRConstant constant, String className,
                                         JvmConstantsGen jvmConstantsGen) {
        BIRNode.ConstValue constValue = constant.constValue;
        if (JvmCodeGenUtil.isSimpleBasicType(constValue.type)) {
            String descriptor = JvmCodeGenUtil.getFieldTypeSignature(constValue.type);
            JvmCodeGenUtil.loadConstantValue(constValue.type, constValue.value, mv, jvmConstantsGen);
            mv.visitFieldInsn(PUTSTATIC, className, constant.name.value, descriptor);
        }
    }

    public String getConstantClass() {
        return constantClass;
    }
}

