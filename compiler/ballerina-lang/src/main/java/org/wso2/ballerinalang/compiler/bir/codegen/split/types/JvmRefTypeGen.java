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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_REF_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE_REF_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TYPE_REF;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_PARAMETER;

/**
 * BIR Type reference types to JVM byte code generation class.
 *
 * @since 2201.2.0
 */
public class JvmRefTypeGen {

    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final JvmConstantsGen jvmConstantsGen;

    public JvmRefTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen) {
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
    }

    public void createTypeRefType(ClassWriter cw, MethodVisitor mv, BIRNode.BIRTypeDefinition typeDef,
                                  BTypeReferenceType typeRefType, String typeRefConstantClass,
                                  JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                                  AsyncDataCollector asyncDataCollector,
                                  LazyLoadingDataCollector lazyLoadingDataCollector) {
        // Create field for type ref type var
        FieldVisitor fv = cw.visitField(ACC_STATIC + ACC_PUBLIC, TYPE_VAR_NAME, GET_TYPE_REF_TYPE_IMPL, null, null);
        fv.visitEnd();
        mv.visitTypeInsn(NEW, TYPE_REF_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(Utils.decodeIdentifier(typeRefType.tsymbol.name.value));
        String moduleVar = jvmConstantsGen.getModuleConstantVar(typeRefType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
        mv.visitLdcInsn(jvmTypeGen.typeFlag(typeRefType.referredType));
        jvmTypeGen.loadReadonlyFlag(mv, typeRefType.referredType);
        mv.visitMethodInsn(INVOKESPECIAL, TYPE_REF_TYPE_IMPL, JVM_INIT_METHOD, INIT_TYPE_REF, false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, typeRefConstantClass, TYPE_VAR_NAME, GET_TYPE_REF_TYPE_IMPL);
        populateTypeRef(mv, typeRefType, typeRefConstantClass);
        if (typeRefType.referredType.tag != TypeTags.RECORD) {
            jvmCreateTypeGen.loadAnnotations(mv, typeRefType, typeDef.originalName.value, jvmPackageGen, jvmCastGen,
                    asyncDataCollector, lazyLoadingDataCollector);
        }
    }

    public void populateTypeRef(MethodVisitor mv, BTypeReferenceType referenceType, String typeRefConstantClass) {
        mv.visitFieldInsn(GETSTATIC, typeRefConstantClass, TYPE_VAR_NAME, GET_TYPE_REF_TYPE_IMPL);
        jvmTypeGen.loadType(mv, referenceType.referredType);
        mv.visitMethodInsn(INVOKEVIRTUAL, TYPE_REF_TYPE_IMPL, "setReferredType", TYPE_PARAMETER, false);
    }
}
