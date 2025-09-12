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

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadBirBasicBlock;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.List;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_CONSTANT_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.lazyLoadAnnotations;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm class for the ballerina function type constants for given module.
 *
 * @since 2.0.0
 */
public class JvmFunctionTypeConstantsGen {

    private final List<BIRNode.BIRFunction> functions;
    private JvmTypeGen jvmTypeGen;
    private final JvmConstantsGen jvmConstantsGen;
    private final String functionTypeConstantsPkgName;


    public JvmFunctionTypeConstantsGen(PackageID module, List<BIRNode.BIRFunction> functions,
                                       JvmConstantsGen jvmConstantsGen) {
        this.functions = functions;
        this.functionTypeConstantsPkgName = getModuleLevelClassName(module, FUNCTION_TYPE_CONSTANT_PACKAGE_NAME);
        this.jvmConstantsGen = jvmConstantsGen;
    }

    public void generateClass(JarEntries jarEntries, JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                              AsyncDataCollector asyncDataCollector,
                              LazyLoadingDataCollector lazyLoadingDataCollector) {
        generateFunctionTypeInits(jarEntries, jvmPackageGen, jvmCastGen, asyncDataCollector, lazyLoadingDataCollector);
    }

    private void generateFunctionTypeInits(JarEntries jarEntries, JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                                           AsyncDataCollector asyncDataCollector,
                                           LazyLoadingDataCollector lazyLoadingDataCollector) {
        for (BIRNode.BIRFunction function : functions) {
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            String functionName = function.name.value;
            String functionTypeConstantClassName = this.functionTypeConstantsPkgName + functionName;
            cw.visit(V21, ACC_PUBLIC | ACC_SUPER, functionTypeConstantClassName, null, OBJECT, null);
            MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
            jvmTypeGen.loadFunctionType(mv, function.type, functionName);
            mv.visitFieldInsn(PUTSTATIC, functionTypeConstantClassName, VALUE_VAR_NAME, GET_FUNCTION_TYPE);
            LazyLoadBirBasicBlock lazyBB = lazyLoadingDataCollector.lazyLoadingAnnotationsBBMap.get(functionName);
            if (lazyBB != null) {
                lazyLoadAnnotations(mv, lazyBB, jvmPackageGen, jvmCastGen, jvmConstantsGen, jvmTypeGen,
                        asyncDataCollector);
            }
            genMethodReturn(mv);
            this.visitFunctionTypeFields(cw);
            cw.visitEnd();
            jarEntries.put(functionTypeConstantClassName + ".class", cw.toByteArray());
        }
    }

    private void visitFunctionTypeFields(ClassWriter cw) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, VALUE_VAR_NAME, GET_FUNCTION_TYPE,
                null, null);
        fv.visitEnd();
    }

    public void setJvmTypeGen(JvmTypeGen jvmTypeGen) {
        this.jvmTypeGen = jvmTypeGen;
    }

    public String getFunctionTypeConstantClass(String functionName) {
        return this.functionTypeConstantsPkgName + Utils.encodeFunctionIdentifier(functionName);
    }
}
