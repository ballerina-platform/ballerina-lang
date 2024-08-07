/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.InitMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.MethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.model.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JFieldBIRFunction;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JMethodBIRFunction;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.List;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.desugarInteropFuncs;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.genJFieldForInteropField;

/**
 * Extern method generation class for JVM byte code generation.
 *
 * @since 1.2.0
 */
public class ExternalMethodGen {

    public static void genJMethodForBExternalFunc(BIRFunction birFunc, ClassWriter cw, BIRPackage birModule,
                                                  BType attachedType, MethodGen methodGen, JvmPackageGen jvmPackageGen,
                                                  JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                                                  JvmConstantsGen jvmConstantsGen, String moduleClassName,
                                                  AsyncDataCollector lambdaGenMetadata, Types types) {
        if (birFunc instanceof JFieldBIRFunction jFieldBIRFunction) {
            genJFieldForInteropField(jFieldBIRFunction, cw, birModule.packageID, jvmPackageGen, jvmTypeGen,
                    jvmCastGen, jvmConstantsGen, lambdaGenMetadata, types);
        } else {
            methodGen.genJMethodForBFunc(birFunc, cw, birModule, jvmTypeGen, jvmCastGen, jvmConstantsGen,
                    moduleClassName, attachedType, lambdaGenMetadata, false);
        }
    }

    public static void injectDefaultParamInits(BIRPackage module, InitMethodGen initMethodGen) {
        // filter out functions.
        List<BIRFunction> functions = module.functions;
        if (!functions.isEmpty()) {
            int funcSize = functions.size();
            int count = 3;
            // Generate classes for other functions.
            while (count < funcSize) {
                BIRFunction birFunc = functions.get(count);
                count = count + 1;
                if (birFunc instanceof JMethodBIRFunction jMethodBIRFunction) {
                    desugarInteropFuncs(jMethodBIRFunction, initMethodGen);
                    initMethodGen.resetIds();
                } else if (!(birFunc instanceof JFieldBIRFunction)) {
                    initMethodGen.resetIds();
                }
            }
        }
    }

    public static BIRFunctionWrapper createExternalFunctionWrapper(boolean isEntry, BIRFunction birFunc,
                                                                   PackageID packageID, String birModuleClassName) {
        if (isEntry) {
            addDefaultableBooleanVarsToSignature(birFunc);
        }
        return getFunctionWrapper(birFunc, packageID, birModuleClassName);
    }

    private ExternalMethodGen() {
    }
}
