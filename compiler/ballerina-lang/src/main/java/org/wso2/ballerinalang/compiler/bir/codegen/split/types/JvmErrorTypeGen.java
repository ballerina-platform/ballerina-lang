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

import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;

import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ERROR_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_DETAIL_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_TYPEID_SET_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_ID_SET;

/**
 * BIR error type to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmErrorTypeGen {

    public final String errorTypesClass;
    public final ClassWriter errorTypesCw;
    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final  JvmConstantsGen jvmConstantsGen;

    public JvmErrorTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen,
                           PackageID packageID) {
        this.errorTypesClass = getModuleLevelClassName(packageID, MODULE_ERROR_TYPES_CLASS_NAME);
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
        this.errorTypesCw = new BallerinaClassWriter(COMPUTE_FRAMES);
        this.errorTypesCw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, errorTypesClass, null, OBJECT, null);
    }

    public void visitEnd(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module, Map<String, byte[]> jarEntries) {
        errorTypesCw.visitEnd();
        jarEntries.put(errorTypesClass + ".class", jvmPackageGen.getBytes(errorTypesCw, module));
    }


    /**
     * Create a runtime type instance for the error.
     *
     * @param mv        method visitor
     * @param errorType error type
     * @param name      name of the error
     */
    public void createErrorType(MethodVisitor mv, BErrorType errorType, String name) {
        // Create the error type
        mv.visitTypeInsn(NEW, ERROR_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load error type name
        mv.visitLdcInsn(IdentifierUtils.decodeIdentifier(name));

        // Load package
        String varName = jvmConstantsGen.getModuleConstantVar(errorType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(), varName,
                String.format("L%s;", MODULE));
        // initialize the error type
        mv.visitMethodInsn(INVOKESPECIAL, ERROR_TYPE_IMPL, JVM_INIT_METHOD,
                String.format("(L%s;L%s;)V", STRING_VALUE, MODULE), false);
    }

    public  void populateError(MethodVisitor mv, BErrorType bType) {
        mv.visitTypeInsn(CHECKCAST, ERROR_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        jvmTypeGen.loadType(mv, bType.detailType);
        mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_DETAIL_TYPE_METHOD,
                String.format("(L%s;)V", TYPE), false);
        BTypeIdSet typeIdSet = bType.typeIdSet;
        if (!typeIdSet.isEmpty()) {
            mv.visitInsn(DUP);
            jvmCreateTypeGen.loadTypeIdSet(mv, typeIdSet);
            mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_TYPEID_SET_METHOD,
                    String.format("(L%s;)V", TYPE_ID_SET), false);
        }
    }
}
