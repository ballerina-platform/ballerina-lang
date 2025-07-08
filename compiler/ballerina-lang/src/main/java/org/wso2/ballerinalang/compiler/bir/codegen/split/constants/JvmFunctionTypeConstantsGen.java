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
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.List;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_CONSTANT_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.skipRecordDefaultValueFunctions;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm class for the ballerina function type constants for given module.
 *
 * @since 2.0.0
 */
public class JvmFunctionTypeConstantsGen {

    private final List<BIRNode.BIRFunction> functions;
    private JvmTypeGen jvmTypeGen;
    private final String functionTypeConstantsPkgName;

    public JvmFunctionTypeConstantsGen(PackageID module, List<BIRNode.BIRFunction> functions) {
        // Skip function types for record default value functions since they can be called directly from function
        // pointers instead of the function name
        this.functions = skipRecordDefaultValueFunctions(functions);
        this.functionTypeConstantsPkgName = getModuleLevelClassName(module,
                FUNCTION_TYPE_CONSTANT_PACKAGE_NAME);
    }

    public void generateClass(JarEntries jarEntries) {
        generateFunctionTypeInits(jarEntries);
    }

    private void generateFunctionTypeInits(JarEntries jarEntries) {
        for (BIRNode.BIRFunction function : functions) {
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            String varName = getFunctionTypeVar(function.name.value);
            String functionTypeConstantClassName = this.functionTypeConstantsPkgName + varName;
            cw.visit(V21, ACC_PUBLIC | ACC_SUPER, functionTypeConstantClassName, null, OBJECT, null);
            MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
            jvmTypeGen.loadInvokableType(mv, function.type);
            mv.visitFieldInsn(PUTSTATIC, functionTypeConstantClassName, getFunctionTypeVar(function.name.value),
                    JvmSignatures.GET_FUNCTION_TYPE);
            genMethodReturn(mv);
            this.visitFunctionTypeFields(cw, varName);
            cw.visitEnd();
            jarEntries.put(functionTypeConstantClassName + ".class", cw.toByteArray());
        }
    }

    private void visitFunctionTypeFields(ClassWriter cw, String functionName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, functionName,
                JvmSignatures.GET_FUNCTION_TYPE, null, null);
        fv.visitEnd();
    }

    public String getFunctionTypeVar(String functionName) {
        return functionName + FUNCTION_TYPE_VAR_PREFIX;
    }

    public void setJvmTypeGen(JvmTypeGen jvmTypeGen) {
        this.jvmTypeGen = jvmTypeGen;
    }

    public String getFunctionTypeConstantClass(String varName) {
        return this.functionTypeConstantsPkgName + varName;
    }
}
