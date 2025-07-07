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
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_DETAIL_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_TYPEID_SET_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_TYPE_ID_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_PARAMETER;

/**
 * BIR error type to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmErrorTypeGen {

    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final JvmConstantsGen jvmConstantsGen;
    public int methodCount = 0;

    public JvmErrorTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen) {
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
    }

    public void createErrorType(ClassWriter cw, MethodVisitor mv, BErrorType errorType, String errorTypeClass,
                                boolean isAnnotatedType) {
        // Create field for error type var
        FieldVisitor fv = cw.visitField(ACC_STATIC + ACC_PUBLIC, TYPE_VAR_NAME, GET_ERROR_TYPE_IMPL, null, null);
        fv.visitEnd();
        String name  = errorType.tsymbol.name.value;
        // Create the error type
        mv.visitTypeInsn(NEW, ERROR_TYPE_IMPL);
        mv.visitInsn(DUP);
        // Load error type name
        mv.visitLdcInsn(Utils.decodeIdentifier(name));
        // Load package
        String moduleVar = jvmConstantsGen.getModuleConstantVar(errorType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
        // initialize the error type
        mv.visitMethodInsn(INVOKESPECIAL, ERROR_TYPE_IMPL, JVM_INIT_METHOD, INIT_ERROR_TYPE_IMPL, false);
        mv.visitFieldInsn(PUTSTATIC, errorTypeClass, TYPE_VAR_NAME, GET_ERROR_TYPE_IMPL);
        populateError(mv, errorType, errorTypeClass);
        jvmCreateTypeGen.genGetTypeMethod(cw, errorTypeClass, GET_ERROR_TYPE_METHOD, GET_ERROR_TYPE_IMPL,
                isAnnotatedType);
    }

    public  void populateError(MethodVisitor mv, BErrorType errorType, String errorTypeClass) {
        mv.visitFieldInsn(GETSTATIC, errorTypeClass, TYPE_VAR_NAME, GET_ERROR_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        jvmTypeGen.loadType(mv, errorType.detailType);
        mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_DETAIL_TYPE_METHOD, TYPE_PARAMETER, false);
        BTypeIdSet typeIdSet = errorType.typeIdSet;
        if (!typeIdSet.isEmpty()) {
            mv.visitInsn(DUP);
            jvmCreateTypeGen.loadTypeIdSet(mv, typeIdSet);
            mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_TYPEID_SET_METHOD, SET_TYPE_ID_SET, false);
        }
    }
}
