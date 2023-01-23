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
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP_X1;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_RECORD_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RECORD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_TYPE_IMPL_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_DESC_CONSTRUCTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeDescClassName;

/**
 * BIR record type to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmRecordTypeGen {

    public final String recordTypesClass;
    public final ClassWriter recordTypesCw;
    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final  JvmConstantsGen jvmConstantsGen;

    public JvmRecordTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen
            , PackageID packageID) {
        this.recordTypesClass = getModuleLevelClassName(packageID, MODULE_RECORD_TYPES_CLASS_NAME);
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
        this.recordTypesCw = new BallerinaClassWriter(COMPUTE_FRAMES);
        this.recordTypesCw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, recordTypesClass, null, OBJECT, null);
    }

    public void visitEnd(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module, Map<String, byte[]> jarEntries) {
        recordTypesCw.visitEnd();
        jarEntries.put(recordTypesClass + ".class", jvmPackageGen.getBytes(recordTypesCw, module));
    }

    public void populateRecord(MethodVisitor mv, String methodName, BRecordType bType, SymbolTable symbolTable) {
        mv.visitTypeInsn(CHECKCAST, RECORD_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        addRecordFields(mv, methodName, bType.fields);
        addRecordRestField(mv, bType.restFieldType);
        jvmCreateTypeGen.addImmutableType(mv, bType, symbolTable);
    }

    /**
     * Add the field type information of a record type. The record type is assumed
     * to be at the top of the stack.
     *  @param mv     method visitor
     * @param fields record fields to be added
     */
    private void addRecordFields(MethodVisitor mv, String methodName, Map<String, BField> fields) {
        // Create the fields map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, JVM_INIT_METHOD, "()V", false);
        if (!fields.isEmpty()) {
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESTATIC, recordTypesClass, methodName + "$addField$", SET_LINKED_HASH_MAP, false);
            jvmCreateTypeGen.splitAddFields(recordTypesCw, recordTypesClass, methodName, fields);
        }
        // Set the fields of the record
        mv.visitMethodInsn(INVOKEVIRTUAL, RECORD_TYPE_IMPL, "setFields", SET_MAP, false);
    }

    /**
     * Add the rest field to a record type. The record type is assumed
     * to be at the top of the stack.
     *
     * @param mv            method visitor
     * @param restFieldType type of the rest field
     */
    private void addRecordRestField(MethodVisitor mv, BType restFieldType) {
        // Load the rest field type
        jvmTypeGen.loadType(mv, restFieldType);
        mv.visitFieldInsn(PUTFIELD, RECORD_TYPE_IMPL, "restFieldType", GET_TYPE);
    }

    /**
     * Create a runtime type instance for the record.
     *
     * @param mv             method visitor
     * @param recordType     record type
     * @param typeOwnerClass record type owner class
     * @param internalName   record type internal name
     */
    public void createRecordType(MethodVisitor mv, BRecordType recordType, String typeOwnerClass, String internalName) {
        // Create the record type
        mv.visitTypeInsn(NEW, RECORD_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load type name
        String name = getFullName(recordType);
        mv.visitLdcInsn(Utils.decodeIdentifier(name));

        // Load internal name
        mv.visitLdcInsn(Utils.decodeIdentifier(internalName));

        // Load package path
        // TODO: get it from the type
        String varName = jvmConstantsGen.getModuleConstantVar(recordType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(), varName,
                GET_MODULE);
        // Load flags
        mv.visitLdcInsn(recordType.tsymbol.flags);

        // Load 'sealed' flag
        mv.visitLdcInsn(recordType.sealed);

        // Load type flags
        mv.visitLdcInsn(jvmTypeGen.typeFlag(recordType));

        // initialize the record type
        mv.visitMethodInsn(INVOKESPECIAL, RECORD_TYPE_IMPL, JVM_INIT_METHOD, RECORD_TYPE_IMPL_INIT, false);

        mv.visitInsn(DUP);
        String packageName = JvmCodeGenUtil.getPackageName(recordType.tsymbol.pkgID);
        String className = getTypeDescClassName(packageName, toNameString(recordType));
        mv.visitTypeInsn(NEW, className);
        mv.visitInsn(DUP_X1);
        mv.visitInsn(SWAP);
        mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, TYPE_DESC_CONSTRUCTOR, false);
        mv.visitFieldInsn(PUTSTATIC, typeOwnerClass, jvmTypeGen.getTypedescFieldName(internalName), GET_TYPEDESC);
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
