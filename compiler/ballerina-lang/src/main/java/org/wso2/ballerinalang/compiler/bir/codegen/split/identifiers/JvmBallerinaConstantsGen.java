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

package org.wso2.ballerinalang.compiler.bir.codegen.split.identifiers;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STRING_CONSTANT_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.getVarStoreClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.addDebugField;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.genLazyLoadingClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm classes for the used ballerina module constants for given module.
 *
 * @since 2.0.0
 */
public class JvmBallerinaConstantsGen {

    private final JvmConstantsGen jvmConstantsGen;
    private final BIRNode.BIRPackage module;
    private final String stringConstantsPkgName;

    public JvmBallerinaConstantsGen(BIRNode.BIRPackage module, JvmConstantsGen jvmConstantsGen) {
        this.jvmConstantsGen = jvmConstantsGen;
        this.module = module;
        this.stringConstantsPkgName = getModuleLevelClassName(module.packageID, MODULE_STRING_CONSTANT_PACKAGE_NAME);
    }

    public void generateConstantInit(JvmPackageGen jvmPackageGen, JarEntries jarEntries) {
        // populate constants to classes
        generateConstantsClasses(jvmPackageGen, jarEntries);
    }

    private void generateConstantsClasses(JvmPackageGen jvmPackageGen, JarEntries jarEntries) {
        if (module.constants.isEmpty()) {
            return;
        }
        ClassWriter allConstantsCW = new BallerinaClassWriter(COMPUTE_FRAMES);
        allConstantsCW.visit(V21, ACC_PUBLIC | ACC_SUPER, jvmConstantsGen.allConstantsClassName, null, OBJECT, null);
        for (BIRNode.BIRConstant constant : module.constants) {
            BIRNode.ConstValue constValue = constant.constValue;
            BType bType = JvmCodeGenUtil.getImpliedType(constValue.type);
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            String varName = constant.name.value;
            String constantVarClassName = getVarStoreClass(jvmConstantsGen.constantsPkgName, varName);
            String descriptor = JvmCodeGenUtil.getFieldTypeSignature(bType);
            // Create lazy loading class
            genLazyLoadingClass(cw, constantVarClassName, descriptor);
            addDebugField(allConstantsCW, varName);
            if (JvmCodeGenUtil.isSimpleBasicType(bType)) {
                // load basic constant value
                MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
                mv.visitCode();
                JvmCodeGenUtil.loadConstantValue(constValue.type, constValue.value, mv, this.jvmConstantsGen,
                        this.stringConstantsPkgName);
                mv.visitFieldInsn(PUTSTATIC, constantVarClassName, VALUE_VAR_NAME, descriptor);
                genMethodReturn(mv);
            }
            cw.visitEnd();
            jarEntries.put(constantVarClassName + CLASS_FILE_SUFFIX, jvmPackageGen.getBytes(cw, module));
        }
        allConstantsCW.visitEnd();
        String constantsClass = jvmConstantsGen.allConstantsClassName + CLASS_FILE_SUFFIX;
        jarEntries.put(constantsClass, jvmPackageGen.getBytes(allConstantsCW, module));
    }
}

