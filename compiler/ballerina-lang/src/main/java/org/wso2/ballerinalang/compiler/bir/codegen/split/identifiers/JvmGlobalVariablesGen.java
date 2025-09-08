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

import org.objectweb.asm.ClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadBirBasicBlock;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.Collection;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GLOBAL_VARIABLES_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.getVarStoreClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.LazyLoadingCodeGenUtils.addDebugField;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.LazyLoadingCodeGenUtils.genLazyLoadingClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.LazyLoadingCodeGenUtils.genLoadDebugVariablesMethod;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.LazyLoadingCodeGenUtils.loadIdentifierValue;

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

    public void generateGlobalVarClasses(JvmPackageGen jvmPackageGen, JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                                         JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector,
                                         JarEntries jarEntries) {
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
            if (globalVar.pos != null) {
                cw.visitSource(globalVar.pos.lineRange().fileName(), null);
            }
            BType bType = JvmCodeGenUtil.getImpliedType(globalVar.type);
            String descriptor = JvmCodeGenUtil.getFieldTypeSignature(bType);
            // Create lazy loading class
            genLazyLoadingClass(cw, globalVarClassName, descriptor);
            addDebugField(allGlobalVarsCW, varName);
            loadIdentifierValue(cw, varName, module, lazyBBMap, jvmPackageGen, jvmTypeGen, jvmCastGen,
                    jvmConstantsGen, asyncDataCollector);
            cw.visitEnd();
            jarEntries.put(globalVarClassName + CLASS_FILE_SUFFIX, jvmPackageGen.getBytes(cw, this.module));
        }
        genLoadDebugVariablesMethod(allGlobalVarsCW);
        allGlobalVarsCW.visitEnd();
        String globalVarsClass = jvmConstantsGen.allGlobalVarsClassName + CLASS_FILE_SUFFIX;
        jarEntries.put(globalVarsClass, jvmPackageGen.getBytes(allGlobalVarsCW, module));
    }
}

