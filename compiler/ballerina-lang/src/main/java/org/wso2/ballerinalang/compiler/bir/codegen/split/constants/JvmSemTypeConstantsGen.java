/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.ballerinalang.compiler.bir.codegen.split.constants;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BINARY_TYPE_OPERATION_DESCRIPTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BINARY_TYPE_OPERATION_WITH_IDENTIFIER_DESCRIPTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_SEMTYPE_TYPE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_CONSTANTS_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.hasIdentifier;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadTypeBuilderIdentifier;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.generateConstantsClassInit;

public class JvmSemTypeConstantsGen {

    private final String semTypeConstantsClass;
    private final Map<BType, String> typeVarMap;
    private final ClassWriter cw;
    private MethodVisitor mv;
    private int constantIndex = 0;
    private int methodCount = 1;
    private JvmTypeGen jvmTypeGen;

    public JvmSemTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator) {
        semTypeConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                JvmConstants.SEMTYPE_TYPE_CONSTANT_CLASS_NAME);
        cw = new BallerinaClassWriter(ClassWriter.COMPUTE_FRAMES);
        generateConstantsClassInit(cw, semTypeConstantsClass);
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, B_SEMTYPE_TYPE_INIT_METHOD, VOID_METHOD_DESC, null, null);
        typeVarMap = new TreeMap<>(bTypeHashComparator);
    }

    public void setJvmTypeGen(JvmTypeGen jvmTypeGen) {
        this.jvmTypeGen = jvmTypeGen;
    }

    public String add(BType type) {
        String typeVarName = typeVarMap.get(type);
        if (typeVarName == null) {
            typeVarName = generateBSemTypeInitMethod(type);
            typeVarMap.put(type, typeVarName);
        }
        return typeVarName;
    }

    private String generateBSemTypeInitMethod(BType type) {
        String varName = JvmConstants.SEMTYPE_TYPE_VAR_PREFIX + constantIndex++;
        if (constantIndex % MAX_CONSTANTS_PER_METHOD == 0 && constantIndex != 0) {
            mv.visitMethodInsn(INVOKESTATIC, semTypeConstantsClass, B_SEMTYPE_TYPE_INIT_METHOD + methodCount,
                    VOID_METHOD_DESC, false);
            genMethodReturn(mv);
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, B_SEMTYPE_TYPE_INIT_METHOD + methodCount++, VOID_METHOD_DESC,
                    null, null);
        }
        // TODO: if the number of semtypes is too large create a new method
        createSemTypeField(varName);
        if (type instanceof BUnionType unionType) {
            loadBUnionType(unionType);
        } else {
            throw new UnsupportedOperationException("Unsupported BType " + type);
        }
        mv.visitFieldInsn(PUTSTATIC, semTypeConstantsClass, varName, GET_TYPE);
        return varName;
    }

    private void loadBUnionType(BUnionType unionType) {
        List<BType> memberTypes = new ArrayList<>(unionType.getMemberTypes());
        jvmTypeGen.loadTypeUsingTypeBuilder(mv, memberTypes.get(0));
        jvmTypeGen.loadTypeUsingTypeBuilder(mv, memberTypes.get(1));
        // TODO: refactor this to make things clean: load all the types to the stack and
        // repeatedly call union
        // operation until just a single type remains on the stack
        if (memberTypes.size() == 2 && hasIdentifier(unionType)) {
            loadTypeBuilderIdentifier(mv, unionType);
            mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "union", BINARY_TYPE_OPERATION_WITH_IDENTIFIER_DESCRIPTOR,
                    false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "union", BINARY_TYPE_OPERATION_DESCRIPTOR, false);
        }
        for (int i = 2; i < memberTypes.size(); i++) {
            jvmTypeGen.loadTypeUsingTypeBuilder(mv, memberTypes.get(i));
            if (i == memberTypes.size() - 1 && hasIdentifier(unionType)) {
                loadTypeBuilderIdentifier(mv, unionType);
                mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "union",
                        BINARY_TYPE_OPERATION_WITH_IDENTIFIER_DESCRIPTOR, false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "union", BINARY_TYPE_OPERATION_DESCRIPTOR, false);
            }
        }
    }

    private void createSemTypeField(String typeVarName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, typeVarName, GET_TYPE, null, null);
        fv.visitEnd();
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(semTypeConstantsClass + JvmConstants.CLASS_FILE_SUFFIX, cw.toByteArray());
    }

    public void generateGetSemType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, semTypeConstantsClass, varName, GET_TYPE);
    }

    public String getSemTypeConstantsClass() {
        return this.semTypeConstantsClass;
    }
}
