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
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmTupleTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;

import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE_CONSTANT_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TUPLE_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.generateConstantsClassInit;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm class for the ballerina tuple types as constants for a given module.
 *
 * @since 2.0.0
 */
public class JvmTupleTypeConstantsGen {

    private final JarEntries jarEntries;
    private final Map<BTupleType, String> tupleTypeVarMap;
    private JvmTupleTypeGen jvmTupleTypeGen;
    private int constantIndex = 0;
    private final String tupleVarConstantsPkgName;

    public JvmTupleTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator,
                                    JarEntries jarEntries) {
        this.jarEntries = jarEntries;
        this.tupleTypeVarMap = new TreeMap<>(bTypeHashComparator);
        this.tupleVarConstantsPkgName = getModuleLevelClassName(packageID,
                TUPLE_TYPE_CONSTANT_PACKAGE_NAME);
    }

    public void setJvmTupleTypeGen(JvmTupleTypeGen jvmTupleTypeGen) {
        this.jvmTupleTypeGen = jvmTupleTypeGen;
    }

    public String add(BTupleType type, SymbolTable symbolTable) {
        String varName = tupleTypeVarMap.get(type);
        if (varName == null) {
            varName = JvmConstants.TUPLE_TYPE_VAR_PREFIX + constantIndex++;
            tupleTypeVarMap.put(type, varName);
            generateBTupleInits(type, varName, symbolTable);

        }
        return varName;
    }

    private void generateBTupleInits(BTupleType type, String varName, SymbolTable symbolTable) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String tupleTypeClass = this.tupleVarConstantsPkgName + varName;
        generateConstantsClassInit(cw, tupleTypeClass);
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        jvmTupleTypeGen.createTupleType(cw, mv, tupleTypeClass, type, false, symbolTable, ACC_PUBLIC);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(tupleTypeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
    }

    public void generateGetBTupleType(MethodVisitor mv, String varName) {
        String typeClass = this.tupleVarConstantsPkgName + varName;
        mv.visitMethodInsn(INVOKESTATIC, typeClass, GET_TYPE_METHOD, GET_TUPLE_TYPE_METHOD, false);

    }
}
