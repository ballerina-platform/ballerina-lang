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

package org.wso2.ballerinalang.compiler.bir.codegen.split.identifiers;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmErrorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadBirBasicBlock;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GLOBAL_VARIABLES_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.getVarStoreClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.addField;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.genLazyLoadingClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm class for the used ballerina module global vars for given module.
 *
 * @since 2201.12.4
 */
public class JvmGlobalVariablesGen {

    private final BIRNode.BIRPackage module;
    private final LazyLoadingDataCollector lazyLoadingDataCollector;
    private final String globalVarsPkgName;

    public JvmGlobalVariablesGen(BIRNode.BIRPackage module,
                                 LazyLoadingDataCollector lazyLoadingDataCollector) {
        this.module = module;
        this.lazyLoadingDataCollector = lazyLoadingDataCollector;
        this.globalVarsPkgName = getModuleLevelClassName(module.packageID, GLOBAL_VARIABLES_PACKAGE_NAME);
    }

    public void generateGlobalVarsInit(JarEntries jarEntries, JvmPackageGen jvmPackageGen, JvmTypeGen jvmTypeGen,
                                       JvmCastGen jvmCastGen, JvmConstantsGen jvmConstantsGen,
                                       AsyncDataCollector asyncDataCollector) {
        // populate global variable to classes
        generateGlobalVarClasses(jarEntries, jvmPackageGen, jvmTypeGen, jvmCastGen, jvmConstantsGen,
                asyncDataCollector);
    }

    private void generateGlobalVarClasses(JarEntries jarEntries, JvmPackageGen jvmPackageGen, JvmTypeGen jvmTypeGen,
                                          JvmCastGen jvmCastGen, JvmConstantsGen jvmConstantsGen,
                                          AsyncDataCollector asyncDataCollector) {
        if (module.globalVars.isEmpty()) {
            return;
        }
        Map<String, LazyLoadBirBasicBlock> lazyBBMap = lazyLoadingDataCollector.lazyLoadingBBMap;
        Collection<BIRNode.BIRGlobalVariableDcl> globalVariableDcls = module.globalVars;
        ClassWriter allGlobalVarsCW = new BallerinaClassWriter(COMPUTE_FRAMES);
        allGlobalVarsCW.visit(V21, ACC_PUBLIC | ACC_SUPER, jvmConstantsGen.allGlobalVarsClassName, null, OBJECT, null);
        for (BIRNode.BIRGlobalVariableDcl globalVar : globalVariableDcls) {
            String varName = globalVar.name.value;
            // Ignore global variables with '_'
            if (varName.equals("_")) {
                continue;
            }
            ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
            String globalVarClassName = getVarStoreClass(globalVarsPkgName, varName);
            BType bType = JvmCodeGenUtil.getImpliedType(globalVar.type);
            String descriptor = JvmCodeGenUtil.getFieldTypeSignature(bType);
            // Create lazy loading class
            genLazyLoadingClass(cw, globalVarClassName, descriptor);
            addField(allGlobalVarsCW, varName);
            LazyLoadBirBasicBlock lazyBB = lazyBBMap.get(varName);
            if (lazyBB != null) {
                // Initialize global value
                MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
                mv.visitCode();
                BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
                JvmInstructionGen instructionGen = new JvmInstructionGen(mv, indexMap, this.module.packageID,
                        jvmPackageGen, jvmTypeGen, jvmCastGen, jvmConstantsGen, asyncDataCollector);
                List<BIRNonTerminator> instructions = lazyBB.instructions();
                if (instructions != null) {
                    for (BIRInstruction instruction : instructions) {
                        instructionGen.generateInstructions(0, instruction);
                    }
                }
                BIRTerminator.Call call = lazyBB.call();
                if (call != null) {
                    JvmErrorGen errorGen = new JvmErrorGen(mv, indexMap, instructionGen);
                    LabelGenerator labelGen = new LabelGenerator();
                    PackageID packageID = module.packageID;
                    JvmTerminatorGen termGen = new JvmTerminatorGen(mv, indexMap, labelGen, errorGen, packageID,
                            instructionGen, jvmPackageGen, jvmTypeGen, jvmCastGen, asyncDataCollector);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitVarInsn(ASTORE, 1);
                    termGen.genCall(call, call.calleePkg, 1);
                    termGen.storeReturnFromCallIns(call.lhsOp != null ? call.lhsOp.variableDcl : null);
                }
                genMethodReturn(mv);
            }
            cw.visitEnd();
            jarEntries.put(globalVarClassName + CLASS_FILE_SUFFIX, jvmPackageGen.getBytes(cw, this.module));
        }
        allGlobalVarsCW.visitEnd();
        String globalVarsClass = jvmConstantsGen.allGlobalVarsClassName + CLASS_FILE_SUFFIX;
        jarEntries.put(globalVarsClass, jvmPackageGen.getBytes(allGlobalVarsCW, module));
    }
}

