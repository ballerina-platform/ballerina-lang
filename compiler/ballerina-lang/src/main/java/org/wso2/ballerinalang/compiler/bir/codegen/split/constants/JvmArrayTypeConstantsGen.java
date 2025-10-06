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
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmArrayTypeGen;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;

import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_FIELD_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ARRAY_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.generateConstantsClassInit;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm class for the ballerina array types as constants for a given module.
 *
 * @since 2.0.0
 */
public class JvmArrayTypeConstantsGen {

    private final JarEntries jarEntries;
    private final Map<BArrayType, String> arrayTypeVarMap;
    private JvmArrayTypeGen jvmArrayTypeGen;
    private final Types types;
    private int constantIndex = 0;
    private final String arrayConstantsPkgName;

    public JvmArrayTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator, Types types,
                                    JarEntries jarEntries) {
        this.arrayTypeVarMap = new TreeMap<>(bTypeHashComparator);
        this.types = types;
        this.jarEntries = jarEntries;
        this.arrayConstantsPkgName = getModuleLevelClassName(packageID, JvmConstants.ARRAY_TYPE_CONSTANT_PACKAGE_NAME);
    }

    public void setJvmArrayTypeGen(JvmArrayTypeGen jvmArrayTypeGen) {
        this.jvmArrayTypeGen = jvmArrayTypeGen;
    }

    public String add(BArrayType arrayType) {
        String varName = arrayTypeVarMap.get(arrayType);
        if (varName == null) {
            varName = generateBArrayInits(arrayType);
            arrayTypeVarMap.put(arrayType, varName);
        }
        return varName;
    }

    private String generateBArrayInits(BArrayType arrayType) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String varName = TYPE_VAR_PREFIX + constantIndex++;
        String arrayConstantClass = this.arrayConstantsPkgName + varName;
        generateConstantsClassInit(cw, arrayConstantClass);
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        jvmArrayTypeGen.createArrayType(cw, mv, arrayType, types, arrayConstantClass);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(arrayConstantClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        return varName;
    }

    public void generateGetBArrayType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, arrayConstantsPkgName + varName, TYPE_VAR_FIELD_NAME, GET_ARRAY_TYPE_IMPL);
    }
}
