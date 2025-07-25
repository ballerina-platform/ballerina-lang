/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.split.constants;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmErrorTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;

import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.generateConstantsClassInit;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates the JVM class for the ballerina error types as constants for a given module.
 * Anonymous distinct error types are generated in this class.
 *
 * @since 2201.4.0
 */
public class JvmErrorTypeConstantsGen {

    private final JarEntries jarEntries;
    private JvmErrorTypeGen jvmErrorTypeGen;
    private final Map<BErrorType, String> errorTypeVarMap;
    private int constantIndex = 0;
    private final String errorVarConstantsPkgName;

    public JvmErrorTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator,
                                    JarEntries jarEntries) {
        this.errorTypeVarMap = new TreeMap<>(bTypeHashComparator);
        this.jarEntries = jarEntries;
        this.errorVarConstantsPkgName = getModuleLevelClassName(packageID,
                JvmConstants.ERROR_TYPE_CONSTANT_PACKAGE_NAME);
    }

    public void setJvmErrorTypeGen(JvmErrorTypeGen jvmErrorTypeGen) {
        this.jvmErrorTypeGen = jvmErrorTypeGen;
    }

    public String add(BErrorType type) {
        String varName = errorTypeVarMap.get(type);
        if (varName == null) {
            varName = generateBErrorInits(type);
            errorTypeVarMap.put(type, varName);
        }
        return varName;
    }

    private String generateBErrorInits(BErrorType errorType) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String varName = ERROR_TYPE_VAR_PREFIX + constantIndex++;
        String errorTypeClass = this.errorVarConstantsPkgName + varName;
        generateConstantsClassInit(cw, errorTypeClass);
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        jvmErrorTypeGen.createErrorType(cw, mv, errorType, errorTypeClass, false);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(errorTypeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        return varName;
    }

    public void generateGetBErrorType(MethodVisitor mv, String varName) {
        String typeClass = this.errorVarConstantsPkgName + varName;
        mv.visitMethodInsn(INVOKESTATIC, typeClass, GET_TYPE_METHOD, GET_ERROR_TYPE_METHOD, false);
    }
}
