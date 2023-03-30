/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;

import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_REF_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TYPE_REF;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_PARAMETER;

/**
 * BIR Type reference types to JVM byte code generation class.
 *
 * @since 2201.2.0
 */
public class JvmRefTypeGen {

    private final JvmTypeGen jvmTypeGen;
    private final JvmConstantsGen jvmConstantsGen;

    public JvmRefTypeGen(JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen) {
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
    }

    /**
     * Create a runtime type instance for type reference type.
     *
     * @param mv          method visitor
     * @param typeRefType type reference type
     */
    public void createTypeRefType(MethodVisitor mv, BTypeReferenceType typeRefType) {
        mv.visitTypeInsn(NEW, TYPE_REF_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(Utils.decodeIdentifier(typeRefType.tsymbol.name.value));
        String varName = jvmConstantsGen.getModuleConstantVar(typeRefType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(), varName, GET_MODULE);
        mv.visitLdcInsn(jvmTypeGen.typeFlag(typeRefType.referredType));
        jvmTypeGen.loadReadonlyFlag(mv, typeRefType.referredType);
        mv.visitMethodInsn(INVOKESPECIAL, TYPE_REF_TYPE_IMPL, JVM_INIT_METHOD, INIT_TYPE_REF, false);
    }

    public void populateTypeRef(MethodVisitor mv, BTypeReferenceType referenceType) {
        mv.visitTypeInsn(CHECKCAST, TYPE_REF_TYPE_IMPL);
        jvmTypeGen.loadType(mv, referenceType.referredType);
        mv.visitMethodInsn(INVOKEVIRTUAL, TYPE_REF_TYPE_IMPL, "setReferredType", TYPE_PARAMETER, false);
    }
}
