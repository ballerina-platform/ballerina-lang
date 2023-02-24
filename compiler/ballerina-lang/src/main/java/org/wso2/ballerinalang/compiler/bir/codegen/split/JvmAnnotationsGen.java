/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.split;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATIONS_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_MAP_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANNOTATIONS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;

/**
 * Generates Jvm class for the ballerina annotation processing.
 *
 * @since 2.0.0
 */
public class JvmAnnotationsGen {

    private final String annotationsClass;
    private final JvmPackageGen jvmPackageGen;
    private final JvmTypeGen jvmTypeGen;
    private final JvmConstantsGen jvmConstantsGen;
    private final BIRNode.BIRPackage module;

    public JvmAnnotationsGen(BIRNode.BIRPackage module, JvmPackageGen jvmPackageGen, JvmTypeGen jvmTypeGen,
                             JvmConstantsGen jvmConstantsGen) {
        this.annotationsClass = getModuleLevelClassName(module.packageID, MODULE_ANNOTATIONS_CLASS_NAME);
        this.jvmPackageGen = jvmPackageGen;
        this.jvmTypeGen = jvmTypeGen;
        this.module = module;
        this.jvmConstantsGen = jvmConstantsGen;
    }

    public void generateAnnotationsClass(Map<String, byte[]> jarEntries) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, annotationsClass, null, OBJECT, null);
        generateProcessAnnotationsMethod(cw, module.typeDefs, module.packageID);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(annotationsClass + ".class", bytes);
    }

    private void generateProcessAnnotationsMethod(ClassWriter cw, List<BIRNode.BIRTypeDefinition> typeDefs,
                                                  PackageID packageID) {
        int annotationsCount = generateAnnotationsLoad(cw, typeDefs, packageID, jvmTypeGen);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, ANNOTATIONS_METHOD_PREFIX, "()V", null, null);
        mv.visitCode();
        for (int i = 0; i < annotationsCount; i++) {
            mv.visitMethodInsn(INVOKESTATIC, annotationsClass, ANNOTATIONS_METHOD_PREFIX + i, "()V", false);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private int generateAnnotationsLoad(ClassWriter cw, List<BIRNode.BIRTypeDefinition> typeDefs,
                                        PackageID packageID, JvmTypeGen jvmTypeGen) {
        int methodCount = 0;
        MethodVisitor mv;
        String typePkgName = JvmCodeGenUtil.getPackageName(packageID);

        mv = cw.visitMethod(ACC_STATIC, ANNOTATIONS_METHOD_PREFIX + methodCount++,
                            "()V", null, null);
        mv.visitCode();
        for (BIRNode.BIRTypeDefinition optionalTypeDef : typeDefs) {
            if (optionalTypeDef.isBuiltin) {
                continue;
            }
            BType bType = optionalTypeDef.type;
            // Annotations for object constructors are populated at object init site.
            boolean constructorsPopulated = (bType.flags & Flags.OBJECT_CTOR) == Flags.OBJECT_CTOR;
            if (!constructorsPopulated) {
                loadAnnotations(mv, typePkgName, optionalTypeDef, jvmTypeGen);
            }
        }
        // Visit the previously started string init method if not ended.
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return methodCount;
    }

    private void loadAnnotations(MethodVisitor mv, String pkgName, BIRNode.BIRTypeDefinition typeDef,
                                 JvmTypeGen jvmTypeGen) {
        String pkgClassName = pkgName.equals(".") || pkgName.equals("") ? MODULE_INIT_CLASS_NAME :
                jvmPackageGen.lookupGlobalVarClassName(pkgName, ANNOTATION_MAP_NAME);
        mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, JvmSignatures.GET_MAP_VALUE);
        BType type = typeDef.type;
        BType refType = typeDef.referenceType == null || type.tag == TypeTags.RECORD
                ? type : typeDef.referenceType;
        jvmTypeGen.loadType(mv, refType);
        mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "processAnnotations",
                JvmSignatures.PROCESS_ANNOTATIONS, false);

    }

}
