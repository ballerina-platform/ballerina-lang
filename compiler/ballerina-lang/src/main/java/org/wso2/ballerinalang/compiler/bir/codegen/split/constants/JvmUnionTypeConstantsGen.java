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

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmUnionTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE_CONSTANT_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_UNION_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen.genFieldsForInitFlags;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.generateConstantsClassInit;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm class for the ballerina union types as constants for a given module.
 *
 * @since 2.0.0
 */
public class JvmUnionTypeConstantsGen {

    private final JarEntries jarEntries;
    private final Map<BUnionType, String> unionTypeVarMap;
    private JvmUnionTypeGen jvmUnionTypeGen;
    private int constantIndex = 0;
    private final String unionVarConstantsPkgName;

    /**
     * Stack keeps track of recursion in union types. The method creation is performed only if recursion is completed.
     */
    public JvmUnionTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator,
                                    JarEntries jarEntries) {
        this.jarEntries = jarEntries;
        this.unionTypeVarMap = new TreeMap<>(bTypeHashComparator);
        this.unionVarConstantsPkgName = getModuleLevelClassName(packageID, UNION_TYPE_CONSTANT_PACKAGE_NAME);
    }

    public void setJvmUnionTypeGen(JvmUnionTypeGen jvmUnionTypeGen) {
        this.jvmUnionTypeGen = jvmUnionTypeGen;
    }

    public String add(BUnionType type, SymbolTable symbolTable) {
        String varName = unionTypeVarMap.get(type);
        if (varName == null) {
            varName = TYPE_VAR_PREFIX + constantIndex++;
            unionTypeVarMap.put(type, varName);
            generateBUnionInits(type, varName, symbolTable);
        }
        return varName;
    }

    private void generateBUnionInits(BUnionType type, String varName, SymbolTable symbolTable) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        genFieldsForInitFlags(cw);
        String unionTypeClass = this.unionVarConstantsPkgName + varName;
        generateConstantsClassInit(cw, unionTypeClass);
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        jvmUnionTypeGen.createUnionType(cw, mv, unionTypeClass, varName, type, false, symbolTable);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(unionTypeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
    }

    public void generateGetBUnionType(MethodVisitor mv, String varName) {
        String typeClass = this.unionVarConstantsPkgName + varName;
        mv.visitMethodInsn(INVOKESTATIC, typeClass, GET_TYPE_METHOD, GET_UNION_TYPE_METHOD, false);
    }
}
