/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
 */
package org.wso2.ballerinalang.compiler.bir.codegen.split.types;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_MEMBERS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_ORIGINAL_MEMBERS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_UNION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_UNION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_TYPE_ARRAY;

/**
 * BIR union types to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmUnionTypeGen {

    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final JvmConstantsGen jvmConstantsGen;
    public int methodCount = 0;

    public JvmUnionTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen) {
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
    }

    public void createUnionType(ClassWriter cw, MethodVisitor mv, String unionTypeClass, String varName,
                                BUnionType unionType, SymbolTable symbolTable) {
        // Create field for union type var
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, TYPE_VAR_NAME, GET_UNION_TYPE_IMPL, null, null);
        fv.visitEnd();
        // Create union type instance
        mv.visitTypeInsn(NEW, UNION_TYPE_IMPL);
        mv.visitInsn(DUP);
        boolean nameLoaded = jvmTypeGen.loadUnionName(mv, unionType);
        if (nameLoaded) {
            BTypeSymbol symbol = unionType.tsymbol;
            if (symbol == null) {
                mv.visitInsn(ACONST_NULL);
            } else {
                String moduleVar = jvmConstantsGen.getModuleConstantVar(unionType.tsymbol.pkgID);
                mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
            }
        }
        mv.visitLdcInsn(jvmTypeGen.typeFlag(unionType));
        jvmTypeGen.loadCyclicFlag(mv, unionType);
        mv.visitLdcInsn(unionType.getFlags());
        // initialize the union type without the members array
        if (nameLoaded) {
            mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE_IMPL, JVM_INIT_METHOD, INIT_UNION_TYPE_IMPL, false);
        } else {
            mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE_IMPL, JVM_INIT_METHOD, "(IZJ)V", false);
        }
        mv.visitFieldInsn(PUTSTATIC, unionTypeClass, TYPE_VAR_NAME, GET_UNION_TYPE_IMPL);
        populateUnion(cw, mv, unionType, unionTypeClass, varName, symbolTable);
    }

    public void populateUnion(ClassWriter cw, MethodVisitor mv, BUnionType bType, String unionTypeClass, String name,
                              SymbolTable symbolTable) {
        mv.visitFieldInsn(GETSTATIC, unionTypeClass, TYPE_VAR_NAME, GET_UNION_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        // populate member fields
        addUnionMembers(cw, mv, bType, unionTypeClass, name);
        jvmCreateTypeGen.addImmutableType(mv, bType, symbolTable);
    }

    private void addUnionMembers(ClassWriter cw, MethodVisitor mv, BUnionType unionType, String className,
                                 String name) {
        jvmTypeGen.createUnionMembersArray(cw, mv, unionType.getMemberTypes(), className, name);
        mv.visitMethodInsn(INVOKEVIRTUAL, UNION_TYPE_IMPL, SET_MEMBERS_METHOD, SET_TYPE_ARRAY, false);
        jvmTypeGen.createUnionMembersArray(cw, mv, unionType.getOriginalMemberTypes(), className, "original" + name);
        mv.visitMethodInsn(INVOKEVIRTUAL, UNION_TYPE_IMPL, SET_ORIGINAL_MEMBERS_METHOD, SET_TYPE_ARRAY, false);
    }
}
