/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.split.constants;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmRefTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen.setTypeInitialized;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.generateConstantsClassInit;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm class for the ballerina type reference types as constants for a given module.
 *
 * @since 2201.2.0
 */
public class JvmRefTypeConstantsGen {

    private final JarEntries jarEntries;
    private JvmRefTypeGen jvmRefTypeGen;
    private final Map<BTypeReferenceType, String> typeRefVarMap;
    private final String typeRefVarConstantsPkgName;

    public JvmRefTypeConstantsGen(BIRNode.BIRPackage module, BTypeHashComparator bTypeHashComparator,
                                  JarEntries jarEntries) {
        this.typeRefVarMap = new TreeMap<>(bTypeHashComparator);
        this.jarEntries = jarEntries;
        this.typeRefVarConstantsPkgName = getModuleLevelClassName(module.packageID,
                JvmConstants.TYPE_REF_TYPE_CONSTANT_PACKAGE_NAME);
    }

    public void setJvmRefTypeGen(JvmRefTypeGen jvmRefTypeGen) {
        this.jvmRefTypeGen = jvmRefTypeGen;
    }

    public String add(BIRNode.BIRTypeDefinition typeDef, JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                      AsyncDataCollector asyncDataCollector, LazyLoadingDataCollector lazyLoadingDataCollector) {
        BTypeReferenceType referenceType = (BTypeReferenceType) typeDef.referenceType;
        String varName = typeRefVarMap.get(referenceType);
        if (varName == null) {
            varName = generateTypeRefTypeInitMethod(typeDef, referenceType, jvmPackageGen, jvmCastGen,
                    asyncDataCollector, lazyLoadingDataCollector);
            typeRefVarMap.put(referenceType, varName);
        }
        return varName;
    }

    private String generateTypeRefTypeInitMethod(BIRNode.BIRTypeDefinition typeDef, BTypeReferenceType referenceType,
                                                 JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                                                 AsyncDataCollector asyncDataCollector,
                                                 LazyLoadingDataCollector lazyLoadingDataCollector) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String varName = JvmCodeGenUtil.getRefTypeConstantName(referenceType);
        String typeRefConstantClass = this.typeRefVarConstantsPkgName + varName;
        generateConstantsClassInit(cw, typeRefConstantClass);
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        boolean isAnnotatedType = referenceType.referredType.tag != TypeTags.RECORD;
        setTypeInitialized(mv, ICONST_1, typeRefConstantClass);
        jvmRefTypeGen.createTypeRefType(cw, mv, typeDef, referenceType, typeRefConstantClass, isAnnotatedType,
                jvmPackageGen, jvmCastGen, asyncDataCollector, lazyLoadingDataCollector);
        setTypeInitialized(mv, ICONST_0, typeRefConstantClass);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeRefConstantClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        return varName;
    }
}
