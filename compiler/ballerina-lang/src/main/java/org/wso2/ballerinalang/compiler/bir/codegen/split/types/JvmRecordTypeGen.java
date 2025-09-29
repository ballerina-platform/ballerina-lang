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

import io.ballerina.identifier.Utils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.Map;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RECORD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RECORD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RECORD_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_TYPE_IMPL_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;

/**
 * BIR record type to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmRecordTypeGen {

    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final JvmConstantsGen jvmConstantsGen;

    public JvmRecordTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen) {
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
    }

    public void createRecordType(ClassWriter cw, MethodVisitor mv, BIRNode.BIRPackage module, String recordTypeClass,
                                 BRecordType recordType, String varName, boolean isAnnotatedType,
                                 SymbolTable symbolTable) {
        FieldVisitor fv = cw.visitField(ACC_STATIC + ACC_PUBLIC, TYPE_VAR_NAME, GET_RECORD_TYPE_IMPL, null, null);
        fv.visitEnd();
        // Create the record type
        mv.visitTypeInsn(NEW, RECORD_TYPE_IMPL);
        mv.visitInsn(DUP);
        // Load type name
        String name = getFullName(recordType);
        mv.visitLdcInsn(Utils.decodeIdentifier(name));
        // Load internal name
        mv.visitLdcInsn(Utils.decodeIdentifier(varName));
        // Load package path
        String moduleVar = jvmConstantsGen.getModuleConstantVar(recordType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
        // Load flags
        mv.visitLdcInsn(recordType.tsymbol.flags);
        // Load 'sealed' flag
        mv.visitLdcInsn(recordType.sealed);
        // Load type flags
        mv.visitLdcInsn(jvmTypeGen.typeFlag(recordType));
        // initialize the record type
        mv.visitMethodInsn(INVOKESPECIAL, RECORD_TYPE_IMPL, JVM_INIT_METHOD, RECORD_TYPE_IMPL_INIT, false);
        mv.visitFieldInsn(PUTSTATIC, recordTypeClass, TYPE_VAR_NAME, GET_RECORD_TYPE_IMPL);
        populateRecord(cw, mv, module, recordTypeClass, recordType, symbolTable);
        jvmCreateTypeGen.genGetTypeMethod(cw, recordTypeClass, GET_RECORD_TYPE_METHOD, GET_RECORD_TYPE_IMPL,
                isAnnotatedType, null);
    }

    public void populateRecord(ClassWriter cw, MethodVisitor mv, BIRNode.BIRPackage module, String recordTypeClass,
                               BRecordType bType, SymbolTable symbolTable) {
        mv.visitFieldInsn(GETSTATIC, recordTypeClass, TYPE_VAR_NAME, GET_RECORD_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        Map<String, String> fieldNameFPNameMap = module.recordDefaultValueMap.get(bType.tsymbol.name.value);
        if (fieldNameFPNameMap != null) {
            mv.visitInsn(DUP);
        }
        addRecordFields(cw, mv, recordTypeClass, bType);
        if (fieldNameFPNameMap != null) {
            addRecordDefaultValues(cw, mv, recordTypeClass, bType, fieldNameFPNameMap);
        }
        addRecordRestField(mv, bType.restFieldType);
        jvmCreateTypeGen.addImmutableType(mv, bType, symbolTable);
    }

    private void addRecordFields(ClassWriter cw, MethodVisitor mv, String typeClass, BRecordType bType) {
        // Create the fields map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        if (!bType.fields.isEmpty()) {
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESTATIC, typeClass, "addFields", SET_LINKED_HASH_MAP, false);
            jvmCreateTypeGen.splitAddFields(cw, bType, typeClass);
        }
        // Set the fields of the record
        mv.visitMethodInsn(INVOKEVIRTUAL, RECORD_TYPE_IMPL, "setFields", SET_MAP, false);
    }

    private void addRecordDefaultValues(ClassWriter cw, MethodVisitor mv, String typeClass, BRecordType recordType,
                                        Map<String, String> fieldNameFPNameMap) {
        // Create the default values map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESTATIC, typeClass, "addDefaultValues", SET_LINKED_HASH_MAP, false);
        jvmCreateTypeGen.splitAddDefaultValues(cw, typeClass, recordType, fieldNameFPNameMap);
        // Set the fields of the record
        mv.visitMethodInsn(INVOKEVIRTUAL, RECORD_TYPE_IMPL, "setDefaultValues", SET_MAP, false);
    }

    private void addRecordRestField(MethodVisitor mv, BType restFieldType) {
        // Load the rest field type
        jvmTypeGen.loadType(mv, restFieldType);
        mv.visitFieldInsn(PUTFIELD, RECORD_TYPE_IMPL, "restFieldType", GET_TYPE);
    }

    private String getFullName(BRecordType recordType) {
        String fullName;
        if (recordType.shouldPrintShape()) {
            fullName = recordType.toString();
        } else {
            // for non-shape values toString gives the org name + name, we only need the name
            fullName = recordType.tsymbol.name.value;
        }
        return fullName;
    }
}
