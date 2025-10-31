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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.model.DoubleCheckLabelsRecord;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_REFERRED_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_REF_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_REF_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_REF_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TYPE_REF;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_PARAMETER;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen.endDoubleCheckGetEnd;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen.genDoubleCheckGetStart;

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
                                  boolean isAnnotatedType, JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                                  AsyncDataCollector asyncDataCollector,
                                  LazyLoadingDataCollector lazyLoadingDataCollector) {
        // Create field for type ref type var
        cw.visitField(ACC_STATIC + ACC_PRIVATE, TYPE_VAR_FIELD, GET_REF_TYPE_IMPL, null, null).visitEnd();
        mv.visitTypeInsn(NEW, TYPE_REF_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(Utils.decodeIdentifier(typeRefType.tsymbol.name.value));
        String moduleVar = jvmConstantsGen.getModuleConstantVar(typeRefType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);
        mv.visitLdcInsn(jvmTypeGen.typeFlag(typeRefType.referredType));
        jvmTypeGen.loadReadonlyFlag(mv, typeRefType.referredType);
        mv.visitMethodInsn(INVOKESPECIAL, TYPE_REF_TYPE_IMPL, JVM_INIT_METHOD, INIT_TYPE_REF, false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, typeRefConstantClass, TYPE_VAR_FIELD, GET_REF_TYPE_IMPL);
        genGetTypeMethod(cw, typeRefType, typeRefConstantClass, isAnnotatedType);
        if (isAnnotatedType) {
            jvmCreateTypeGen.loadAnnotations(cw, typeDef.internalName.value, typeRefConstantClass,
                    GET_REF_TYPE_IMPL, jvmPackageGen, jvmCastGen, asyncDataCollector,
                    lazyLoadingDataCollector);
        }
    }

    private void genGetTypeMethod(ClassWriter cw, BTypeReferenceType typeRefType, String typeRefClass,
                                  boolean isAnnotatedType) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, GET_TYPE_METHOD, GET_REF_TYPE_METHOD, null, null);
        mv.visitCode();
        DoubleCheckLabelsRecord checkLabelsRecord = genDoubleCheckGetStart(mv, typeRefClass, GET_REF_TYPE_IMPL);
        populateTypeRef(mv, typeRefType, typeRefClass);
        endDoubleCheckGetEnd(mv, typeRefClass, GET_REF_TYPE_IMPL, checkLabelsRecord, isAnnotatedType);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void populateTypeRef(MethodVisitor mv, BTypeReferenceType referenceType, String typeRefConstantClass) {
        mv.visitFieldInsn(GETSTATIC, typeRefConstantClass, TYPE_VAR_FIELD, GET_REF_TYPE_IMPL);
        jvmTypeGen.loadType(mv, referenceType.referredType);
        mv.visitMethodInsn(INVOKEVIRTUAL, TYPE_REF_TYPE_IMPL, SET_REFERRED_TYPE_METHOD, TYPE_PARAMETER, false);
    }
}
