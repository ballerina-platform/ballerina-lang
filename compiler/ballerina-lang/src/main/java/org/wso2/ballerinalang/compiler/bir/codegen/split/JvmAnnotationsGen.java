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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JVMModuleUtils.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATIONS_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_MAP_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GLOBAL_VARIABLES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANNOTATIONS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.getVarStoreClass;

/**
 * Generates Jvm class for the ballerina annotation processing.
 *
 * @since 2.0.0
 */
public class JvmAnnotationsGen {

    private final String annotationsClass;
    private final JvmPackageGen jvmPackageGen;
    private final JvmTypeGen jvmTypeGen;
    private final BIRNode.BIRPackage module;
    private final String annotationVarClassName;

    public JvmAnnotationsGen(BIRNode.BIRPackage module, JvmPackageGen jvmPackageGen, JvmTypeGen jvmTypeGen) {
        this.jvmPackageGen = jvmPackageGen;
        this.jvmTypeGen = jvmTypeGen;
        this.module = module;
        this.annotationsClass = getModuleLevelClassName(module.packageID, MODULE_ANNOTATIONS_CLASS_NAME);
        this.annotationVarClassName = getVarStoreClass(getModuleLevelClassName(module.packageID,
                        GLOBAL_VARIABLES_CLASS_NAME) , ANNOTATION_MAP_NAME);
    }

    public void generateAnnotationsClass(JarEntries jarEntries) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V21, ACC_PUBLIC + ACC_SUPER, annotationsClass, null, OBJECT, null);
        generateProcessAnnotationsMethod(cw, module.typeDefs);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(annotationsClass + CLASS_FILE_SUFFIX, bytes);
    }

    private void generateProcessAnnotationsMethod(ClassWriter cw, List<BIRNode.BIRTypeDefinition> typeDefs) {
        int annotationsCount = generateAnnotationsLoad(cw, typeDefs, jvmTypeGen);
        MethodVisitor mv =
                cw.visitMethod(ACC_PUBLIC + ACC_STATIC, ANNOTATIONS_METHOD_PREFIX, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        for (int i = 0; i < annotationsCount; i++) {
            mv.visitMethodInsn(INVOKESTATIC, annotationsClass, ANNOTATIONS_METHOD_PREFIX + i, VOID_METHOD_DESC, false);
        }
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, ANNOTATIONS_METHOD_PREFIX, annotationsClass);
        mv.visitEnd();
    }

    private int generateAnnotationsLoad(ClassWriter cw, List<BIRNode.BIRTypeDefinition> typeDefs,
                                        JvmTypeGen jvmTypeGen) {
        int methodCount = 0;
        MethodVisitor mv;

        String annotationMethodName = ANNOTATIONS_METHOD_PREFIX + methodCount++;
        mv = cw.visitMethod(ACC_STATIC, annotationMethodName, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        for (BIRNode.BIRTypeDefinition optionalTypeDef : typeDefs) {
            if (optionalTypeDef.isBuiltin) {
                continue;
            }
            BType bType = optionalTypeDef.type;
            // Annotations for object constructors are populated at object init site.
            boolean constructorsPopulated = Symbols.isFlagOn(bType.getFlags(), Flags.OBJECT_CTOR);
            if (!constructorsPopulated) {
                loadAnnotations(mv, optionalTypeDef, jvmTypeGen);
            }
        }
        // Visit the previously started string init method if not ended.
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, annotationMethodName, annotationsClass);
        mv.visitEnd();
        return methodCount;
    }

    private void loadAnnotations(MethodVisitor mv, BIRNode.BIRTypeDefinition typeDef, JvmTypeGen jvmTypeGen) {
        mv.visitFieldInsn(GETSTATIC, this.annotationVarClassName, ANNOTATION_MAP_NAME, JvmSignatures.GET_MAP_VALUE);
        BType type = typeDef.type;
        BType refType = typeDef.referenceType == null || type.tag == TypeTags.RECORD ? type : typeDef.referenceType;
        jvmTypeGen.loadType(mv, refType);
        mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "processAnnotations", JvmSignatures.PROCESS_ANNOTATIONS, false);
    }

}
