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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.InitMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.MethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WRAPPER_GEN_BB_ID_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.enrichWithDefaultableParamInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.insertAndGetNextBasicBlock;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.cleanupPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INITIAL_METHOD_DESC;
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
        if (birFunc instanceof JFieldBIRFunction) {
            genJFieldForInteropField((JFieldBIRFunction) birFunc, cw, birModule.packageID, jvmPackageGen, jvmTypeGen,
                    jvmCastGen, jvmConstantsGen, lambdaGenMetadata, types);
        } else {
            methodGen.genJMethodForBFunc(birFunc, cw, birModule, jvmTypeGen, jvmCastGen, jvmConstantsGen,
                    moduleClassName, attachedType, lambdaGenMetadata);
        }
    }

    public static void injectDefaultParamInits(BIRPackage module, InitMethodGen initMethodGen,
                                               JvmPackageGen jvmPackageGen) {

        // filter out functions.
        List<BIRFunction> functions = module.functions;
        if (!functions.isEmpty()) {
            int funcSize = functions.size();
            int count = 3;

            // Generate classes for other functions.
            while (count < funcSize) {
                BIRFunction birFunc = functions.get(count);
                count = count + 1;
                BIRFunctionWrapper extFuncWrapper = lookupBIRFunctionWrapper(module.packageID, birFunc, null,
                                                                             jvmPackageGen);
                if (extFuncWrapper instanceof OldStyleExternalFunctionWrapper) {
                    desugarOldExternFuncs((OldStyleExternalFunctionWrapper) extFuncWrapper, birFunc, initMethodGen);
                    enrichWithDefaultableParamInits(birFunc, initMethodGen);
                } else if (birFunc instanceof JMethodBIRFunction) {
                    desugarInteropFuncs((JMethodBIRFunction) birFunc, initMethodGen);
                    enrichWithDefaultableParamInits(birFunc, initMethodGen);
                } else if (!(birFunc instanceof JFieldBIRFunction)) {
                    enrichWithDefaultableParamInits(birFunc, initMethodGen);
                }
            }
        }

    }

    public static void desugarOldExternFuncs(OldStyleExternalFunctionWrapper extFuncWrapper, BIRFunction birFunc,
                                             InitMethodGen initMethodGen) {
        BType retType = birFunc.type.retType;

        BIROperand retRef = null;
        if (retType.tag != TypeTags.NIL) {
            BIRVariableDcl localVar = birFunc.localVars.get(0);
            BIRVariableDcl variableDcl = new BIRVariableDcl(retType, localVar.name, localVar.scope, localVar.kind);
            retRef = new BIROperand(variableDcl);
        }

        initMethodGen.resetIds();

        BIRBasicBlock beginBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, WRAPPER_GEN_BB_ID_NAME, initMethodGen);
        BIRBasicBlock retBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, WRAPPER_GEN_BB_ID_NAME, initMethodGen);

        List<BIROperand> args = new ArrayList<>();

        BIRVariableDcl receiver = birFunc.receiver;
        if (receiver != null) {

            BIROperand argRef = new BIROperand(receiver);
            args.add(argRef);
        }

        for (BIRNode.BIRFunctionParameter birFuncParam : birFunc.parameters) {
            BIROperand argRef = new BIROperand(birFuncParam);
            args.add(argRef);
        }

        String jMethodName = birFunc.name.value;
        beginBB.terminator = new JavaMethodCall(birFunc.pos, args, retRef, extFuncWrapper.jClassName,
                extFuncWrapper.jMethodVMSig, jMethodName, retBB);

        retBB.terminator = new BIRTerminator.Return(birFunc.pos);
    }

    public static BIRFunctionWrapper lookupBIRFunctionWrapper(PackageID packageID, BIRFunction birFunc,
                                                              BType attachedType, JvmPackageGen jvmPackageGen) {

        String lookupKey;
        String currentPackageName = JvmCodeGenUtil.getPackageName(packageID);

        String birFuncName = birFunc.name.value;

        if (attachedType == null) {
            lookupKey = currentPackageName + birFuncName;
        } else if (attachedType.tag == TypeTags.OBJECT) {
            lookupKey = currentPackageName + toNameString(attachedType) + "." + birFuncName;
        } else {
            throw new BLangCompilerException("Java method generation for the receiver type " + attachedType + " " +
                    "is not supported: ");
        }

        BIRFunctionWrapper birFuncWrapper = jvmPackageGen.lookupBIRFunctionWrapper(lookupKey);
        if (birFuncWrapper != null) {
            return birFuncWrapper;
        } else {
            throw new BLangCompilerException("cannot find function definition for : " + lookupKey);
        }
    }

    public static OldStyleExternalFunctionWrapper createOldStyleExternalFunctionWrapper(
            BIRFunction birFunc, PackageID packageID, String birModuleClassName, String jClassName,
            boolean isEntryModule, SymbolTable symbolTable) {
        List<BType> jMethodPramTypes = new ArrayList<>(birFunc.type.paramTypes);
        if (isEntryModule) {
            addDefaultableBooleanVarsToSignature(birFunc, symbolTable.booleanType);
        }
        BInvokableType functionTypeDesc = birFunc.type;

        BType restType = functionTypeDesc.restType;

        if (restType != null) {
            jMethodPramTypes.add(restType);
        }

        BIRVariableDcl receiver = birFunc.receiver;

        String jvmMethodDescription;
        String jMethodVMSig;
        if (receiver == null) {
            jvmMethodDescription = JvmCodeGenUtil.getMethodDesc(functionTypeDesc.paramTypes,
                                                                functionTypeDesc.retType);
            jMethodVMSig = getExternMethodDesc(jMethodPramTypes, functionTypeDesc.retType);
        } else {
            jvmMethodDescription = JvmCodeGenUtil.getMethodDesc(functionTypeDesc.paramTypes,
                                                                functionTypeDesc.retType, receiver.type);
            jMethodVMSig = getExternMethodDesc(jMethodPramTypes, functionTypeDesc.retType, receiver.type);
        }
        return new OldStyleExternalFunctionWrapper(packageID, birFunc, birModuleClassName,
                                                   jvmMethodDescription, jClassName, jMethodPramTypes, jMethodVMSig);
    }

    public static String getExternMethodDesc(List<BType> paramTypes, BType retType) {
        return INITIAL_METHOD_DESC + JvmCodeGenUtil.populateMethodDesc(paramTypes) +
                generateExternReturnType(retType);
    }

    public static String getExternMethodDesc(List<BType> paramTypes, BType retType, BType attachedType) {
        return INITIAL_METHOD_DESC + JvmCodeGenUtil.getArgTypeSignature(attachedType) +
                JvmCodeGenUtil.populateMethodDesc(paramTypes) + generateExternReturnType(retType);
    }

    static String generateExternReturnType(BType bType) {
        bType = JvmCodeGenUtil.UNIFIER.build(bType);
        if (bType == null || bType.tag == TypeTags.NIL || bType.tag == TypeTags.NEVER) {
            return ")V";
        }
        return JvmCodeGenUtil.generateReturnType(bType);
    }

    public static BIRFunctionWrapper createExternalFunctionWrapper(boolean isEntry, BIRFunction birFunc,
                                                                   PackageID packageID, String birModuleClassName,
                                                                   String lookupKey, JvmPackageGen jvmPackageGen) {

        BIRFunctionWrapper birFuncWrapper;
        String pkgName = JvmCodeGenUtil.getPackageName(packageID);
        String jClassName = jvmPackageGen.lookupExternClassName(cleanupPackageName(pkgName), lookupKey);
        if (birFunc instanceof JBIRFunction) {
            if (isEntry) {
                addDefaultableBooleanVarsToSignature(birFunc, jvmPackageGen.symbolTable.booleanType);
            }
            birFuncWrapper = getFunctionWrapper(birFunc, packageID, birModuleClassName);
        } else {
            // This is a old-style external Java interop function
            if (jClassName != null) {
                if (JvmCodeGenUtil.isBallerinaBuiltinModule(packageID.orgName.value, packageID.name.value)) {
                    birFuncWrapper = getFunctionWrapper(birFunc, packageID, jClassName);
                } else {
                    birFuncWrapper = createOldStyleExternalFunctionWrapper(birFunc, packageID,
                                                                           birModuleClassName, jClassName,
                                                                           isEntry, jvmPackageGen.symbolTable);
                }
            } else {
                throw new BLangCompilerException("cannot find full qualified class name for extern function : " +
                                                         pkgName + birFunc.name.value);
            }
        }
        return birFuncWrapper;
    }

    private ExternalMethodGen() {
    }
}
