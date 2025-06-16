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
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmRefTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JVMModuleUtils;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;

import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.generateConstantsClassInit;

/**
 * Generates Jvm class for the ballerina type reference types as constants for a given module.
 *
 * @since 2201.2.0
 */
public class JvmRefTypeConstantsGen {

    private final JarEntries jarEntries;
    private JvmRefTypeGen jvmRefTypeGen;
    private final Map<BTypeReferenceType, String> typeRefVarMap;
    private final String typeRefVarConstantsPkgName;

    public JvmRefTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator, JarEntries jarEntries) {
        this.typeRefVarMap = new TreeMap<>(bTypeHashComparator);
        this.jarEntries = jarEntries;
        this.typeRefVarConstantsPkgName = JVMModuleUtils.getModuleLevelClassName(packageID, JvmConstants.
                TYPE_REF_TYPE_CONSTANT_PACKAGE_NAME);
    }

    public void setJvmRefTypeGen(JvmRefTypeGen jvmRefTypeGen) {
        this.jvmRefTypeGen = jvmRefTypeGen;
    }

    public String add(BTypeReferenceType type) {
        String varName = typeRefVarMap.get(type);
        if (varName == null) {
            varName = generateTypeRefTypeInitMethod(type);
            typeRefVarMap.put(type, varName);
        }
        return varName;
    }

    private String generateTypeRefTypeInitMethod(BTypeReferenceType type) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String varName = JvmCodeGenUtil.getRefTypeConstantName(type);
        String typeRefConstantClass = this.typeRefVarConstantsPkgName + varName;
        generateConstantsClassInit(cw, typeRefConstantClass);
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        jvmRefTypeGen.createTypeRefType(cw, mv, type, typeRefConstantClass);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeRefConstantClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        return varName;
    }
}
