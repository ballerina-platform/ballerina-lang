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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.List;
import java.util.Optional;

import static io.ballerina.identifier.Utils.decodeIdentifier;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ADD_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOAD_ANNOTATIONS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_CYCLIC_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_IMMUTABLE_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_MEMBERS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_INIT_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_FIELD_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ANY_TO_JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_JBOOLEAN_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TUPLE_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_IMMUTABLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TUPLE_SET_MEMBERS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen.setTypeInitialized;

/**
 * BIR tuple types to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmTupleTypeGen {

    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final  JvmConstantsGen jvmConstantsGen;
    public int methodCount = 0;

    public JvmTupleTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen) {
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
    }

    public void createTupleType(ClassWriter cw, MethodVisitor mv, String tupleTypeClass, BTupleType tupleType,
                                boolean isAnnotatedType, SymbolTable symbolTable, int access) {
        // Create field for tuple type var
        FieldVisitor fv = cw.visitField(ACC_STATIC + access, TYPE_VAR_FIELD_NAME, GET_TUPLE_TYPE_IMPL, null, null);
        fv.visitEnd();
        mv.visitTypeInsn(NEW, TUPLE_TYPE_IMPL);
        mv.visitInsn(DUP);
        // Load type name
        BTypeSymbol typeSymbol = tupleType.tsymbol;
        if (typeSymbol == null) {
            mv.visitLdcInsn(decodeIdentifier(tupleType.name.getValue()));
            mv.visitInsn(ACONST_NULL);
        } else {
            mv.visitLdcInsn(decodeIdentifier(typeSymbol.name.getValue()));
            String moduleVar = jvmConstantsGen.getModuleConstantVar(typeSymbol.pkgID);
            mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
        }
        mv.visitLdcInsn(jvmTypeGen.typeFlag(tupleType));
        jvmTypeGen.loadCyclicFlag(mv, tupleType);
        jvmTypeGen.loadReadonlyFlag(mv, tupleType);
        // initialize the tuple type without the members array
        mv.visitMethodInsn(INVOKESPECIAL, TUPLE_TYPE_IMPL, JVM_INIT_METHOD, INIT_TUPLE_TYPE_IMPL, false);
        mv.visitFieldInsn(PUTSTATIC, tupleTypeClass, TYPE_VAR_FIELD_NAME, GET_TUPLE_TYPE_IMPL);
        genGetTypeMethod(cw, tupleType, tupleTypeClass, isAnnotatedType, symbolTable);
    }

    private void genGetTypeMethod(ClassWriter cw, BTupleType tupleType, String tupleTypeClass, boolean isAnnotatedType,
                                  SymbolTable symbolTable) {
        FieldVisitor f = cw.visitField(ACC_STATIC + ACC_PRIVATE, TYPE_INIT_VAR_NAME, GET_JBOOLEAN_TYPE, null, null);
        f.visitEnd();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, GET_TYPE_METHOD, GET_TUPLE_TYPE_METHOD, null, null);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, tupleTypeClass, TYPE_INIT_VAR_NAME, GET_JBOOLEAN_TYPE);
        Label ifLabel = new Label();
        mv.visitJumpInsn(IFNE, ifLabel);
        setTypeInitialized(mv, ICONST_1, tupleTypeClass);
        populateTuple(mv, tupleType, tupleTypeClass, symbolTable);
        if (isAnnotatedType) {
            mv.visitMethodInsn(INVOKESTATIC, tupleTypeClass, LOAD_ANNOTATIONS_METHOD, VOID_METHOD_DESC, false);
        }
        mv.visitLabel(ifLabel);
        mv.visitFieldInsn(GETSTATIC, tupleTypeClass, TYPE_VAR_FIELD_NAME, GET_TUPLE_TYPE_IMPL);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public void populateTuple(MethodVisitor mv, BTupleType bType, String tupleTypeClass, SymbolTable symbolTable) {
        Optional<BIntersectionType> immutableType = jvmCreateTypeGen.getImmutableType(bType, symbolTable);
        mv.visitFieldInsn(GETSTATIC, tupleTypeClass, TYPE_VAR_FIELD_NAME, GET_TUPLE_TYPE_IMPL);
        mv.visitInsn(DUP);
        if (immutableType.isPresent()) {
            mv.visitInsn(DUP);
        }
        addCyclicFlag(mv, bType);
        addTupleMembers(mv, bType);
        if (immutableType.isPresent()) {
            jvmTypeGen.loadType(mv, immutableType.get());
            mv.visitMethodInsn(INVOKEINTERFACE, TYPE, SET_IMMUTABLE_TYPE_METHOD, SET_IMMUTABLE_TYPE, true);
        }
    }

    private void addTupleMembers(MethodVisitor mv, BTupleType tupleType) {
        createTupleMembersList(mv, tupleType.getMembers());
        BType restType = tupleType.restType;
        if (restType == null) {
            mv.visitInsn(ACONST_NULL);
        } else {
            jvmTypeGen.loadType(mv, restType);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, TUPLE_TYPE_IMPL, SET_MEMBERS_METHOD, TUPLE_SET_MEMBERS_METHOD, false);
    }

    private void addCyclicFlag(MethodVisitor mv, BType userDefinedType) {
        jvmTypeGen.loadCyclicFlag(mv, userDefinedType);
        mv.visitMethodInsn(INVOKEVIRTUAL, TUPLE_TYPE_IMPL, SET_CYCLIC_METHOD, "(Z)V", false);
    }

    private void createTupleMembersList(MethodVisitor mv, List<BTupleMember> members) {
        mv.visitTypeInsn(NEW, ARRAY_LIST);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        for (BTupleMember tupleType : members) {
            mv.visitInsn(DUP);
            jvmTypeGen.loadType(mv, tupleType.type);
            mv.visitMethodInsn(INVOKEINTERFACE, LIST, ADD_METHOD, ANY_TO_JBOOLEAN, true);
            mv.visitInsn(POP);
        }
    }
}
