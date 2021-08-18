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
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANNOTATIONS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;

/**
 * Generates Jvm class for the ballerina annotation processing.
 *
 * @since 2.0.0
 */
public class JvmAnnotationsGen {

    private final String annotationsClass;
    private final JvmPackageGen jvmPackageGen;
    private final JvmTypeGen jvmTypeGen;
    private static final int MAX_ANNOTATIONS_PER_METHOD = 100;
    private final BIRNode.BIRPackage module;

    public JvmAnnotationsGen(BIRNode.BIRPackage module, JvmPackageGen jvmPackageGen, JvmTypeGen jvmTypeGen) {
        this.annotationsClass = getModuleLevelClassName(module.packageID, MODULE_ANNOTATIONS_CLASS_NAME);
        this.jvmPackageGen = jvmPackageGen;
        this.jvmTypeGen = jvmTypeGen;
        this.module = module;
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
        String typePkgName = ".";
        int annCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        if (!"".equals(packageID)) {
            typePkgName = JvmCodeGenUtil.getPackageName(packageID);
        }

        for (BIRNode.BIRTypeDefinition optionalTypeDef : typeDefs) {
            if (optionalTypeDef.isBuiltin) {
                continue;
            }
            BType bType = optionalTypeDef.type;
            // Annotations for object constructors are populated at object init site.
            boolean constructorsPopulated = (bType.flags & Flags.OBJECT_CTOR) == Flags.OBJECT_CTOR;
            if (!constructorsPopulated && bType.tag != TypeTags.FINITE) {
                if (annCount % MAX_ANNOTATIONS_PER_METHOD == 0) {
                    mv = cw.visitMethod(ACC_STATIC, ANNOTATIONS_METHOD_PREFIX + methodCount++,
                            "()V", null, null);
                    mv.visitCode();
                }
                annCount = loadAnnotationss(mv, typePkgName, optionalTypeDef, jvmTypeGen, annCount);
                if (annCount % MAX_ANNOTATIONS_PER_METHOD == 0) {
                    mv.visitInsn(RETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
            }
        }
        // Visit the previously started string init method if not ended.
        if (annCount % MAX_ANNOTATIONS_PER_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        return methodCount;
    }

    private int loadAnnotationss(MethodVisitor mv, String pkgName, BIRNode.BIRTypeDefinition typeDef,
                                 JvmTypeGen jvmTypeGen, int annCount) {
        String pkgClassName = pkgName.equals(".") || pkgName.equals("") ? MODULE_INIT_CLASS_NAME :
                jvmPackageGen.lookupGlobalVarClassName(pkgName, ANNOTATION_MAP_NAME);
        mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, String.format("L%s;", MAP_VALUE));
        loadLocalType(mv, typeDef, jvmTypeGen);
        mv.visitMethodInsn(INVOKESTATIC, String.format("%s", ANNOTATION_UTILS), "processAnnotations",
                String.format("(L%s;L%s;)V", MAP_VALUE, TYPE), false);
        if (typeDef.type.tag == TypeTags.UNION) {
            annCount = annCount + ((BUnionType) typeDef.type).getMemberTypes().size();
        }
        if (annCount >= MAX_ANNOTATIONS_PER_METHOD) {
            return 0;
        }
        return annCount;
    }

    void loadLocalType(MethodVisitor mv, BIRNode.BIRTypeDefinition typeDefinition, JvmTypeGen jvmTypeGen) {
        jvmTypeGen.loadType(mv, typeDefinition.type);
    }

}
