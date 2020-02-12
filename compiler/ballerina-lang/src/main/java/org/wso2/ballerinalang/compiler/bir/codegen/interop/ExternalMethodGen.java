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
import org.objectweb.asm.ClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.Nilable;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ClassUtils.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.addDefaultableBooleanVarsToSignature;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.enrichWithDefaultableParamInits;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.insertAndGetNextBasicBlock;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.genJMethodForBFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getMethodDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.getVariableDcl;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nameOfNonBStringFunc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nextId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.nextVarId;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.BIRFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.birFunctionMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.cleanupPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupExternClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.AnnotationProc.getInteropAnnotValue;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JFieldFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JMethodFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.desugarInteropFuncs;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.genJFieldForInteropField;


//import ballerina/bir;
//import ballerina/jvm;
//import ballerina/io;

public class ExternalMethodGen {

    static void genJMethodForBExternalFunc(BIRFunction birFunc,
                                           ClassWriter cw,
                                           BIRPackage birModule,
                                           @Nilable BType attachedType /* = () */) {
        var extFuncWrapper = getExternalFunctionWrapper(birModule, birFunc, attachedType = attachedType);

        if (extFuncWrapper instanceof JFieldFunctionWrapper) {
            genJFieldForInteropField(extFuncWrapper, cw, birModule);
        } else {
            genJMethodForBFunc(birFunc, cw, birModule, false, "", attachedType = attachedType);
        }
    }

    public static void injectDefaultParamInits(BIRPackage module) {

        // filter out functions.
        @Nilable List<BIRFunction> functions = module.functions;
        if (functions.size() > 0) {
            int funcSize = functions.size();
            int count = 3;

            // Generate classes for other functions.
            while (count < funcSize) {
                BIRFunction birFunc = (BIRFunction) functions.get(count);
                count = count + 1;
                var extFuncWrapper = lookupBIRFunctionWrapper(module, birFunc, attachedType = null);
                if (extFuncWrapper instanceof OldStyleExternalFunctionWrapper) {
                    desugarOldExternFuncs(module, extFuncWrapper, birFunc);
                    enrichWithDefaultableParamInits(birFunc);
                } else if (extFuncWrapper instanceof JMethodFunctionWrapper) {
                    desugarInteropFuncs(module, extFuncWrapper, birFunc);
                    enrichWithDefaultableParamInits(birFunc);
                } else if (!(extFuncWrapper instanceof JMethodFunctionWrapper) && !(extFuncWrapper instanceof JFieldFunctionWrapper)) {
                    enrichWithDefaultableParamInits(birFunc);
                }
            }
        }

    }

    public static void desugarOldExternFuncs(BIRPackage module, OldStyleExternalFunctionWrapper extFuncWrapper,
                                      BIRFunction birFunc) {
        BType retType = (BType) birFunc.type["retType"];

        @Nilable BIRVarRef retRef = null;
        if (!(retType.tag == TypeTags.NIL)) {
            retRef = new (variableDcl:getVariableDcl(birFunc.localVars.get(0)), type:retType);
        }

        nextId = -1;
        nextVarId = -1;

        BIRBasicBlock beginBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, prefix = "wrapperGen");
        BIRBasicBlock retBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, prefix = "wrapperGen");

        @Nilable List<BIRVarRef> args = new ArrayList<>();

        @Nilable BIRVariableDcl receiver = birFunc.receiver;
        if (!(receiver == null)) {
            BIRVarRef argRef = new BIRVarRef(variableDcl:receiver, type:receiver.type);
            args.add(argRef);
        }

        @Nilable List<BIRFunctionParam> birFuncParams = birFunc.params;

        int birFuncParamIndex = 0;
        while (birFuncParamIndex < birFuncParams.size()) {
            BIRFunctionParam birFuncParam = (BIRFunctionParam) birFuncParams.get(birFuncParamIndex);
            BIRVarRef argRef = new BIRVarRef(variableDcl:birFuncParam, type:birFuncParam.type);
            args.add(argRef);
            birFuncParamIndex += 1;
        }

        String jMethodName = birFunc.name.value;
        JavaMethodCall jCall = new JavaMethodCall(pos:birFunc.pos, args:args, kind:BIRTERMINATOR_PLATFORM, lhsOp:retRef,
                jClassName:extFuncWrapper.jClassName, name:jMethodName,
                jMethodVMSigBString:extFuncWrapper.jMethodVMSigBString ?:"<error>",
                jMethodVMSig:extFuncWrapper.jMethodVMSig, thenBB:retBB);
        beginBB.terminator = jCall;

        BIRReturn ret = new BIRReturn(pos:birFunc.pos, kind:BIRTERMINATOR_RETURN);
        retBB.terminator = ret;
    }

//   type ExternalFunctionWrapper JInteropFunctionWrapper | OldStyleExternalFunctionWrapper;

    static ExternalFunctionWrapper getExternalFunctionWrapper(BIRPackage birModule, BIRFunction birFunc,
                                                              @Nilable BType attachedType /* = () */) {

        var birFuncWrapper = lookupBIRFunctionWrapper(birModule, birFunc, attachedType = attachedType);
        if (birFuncWrapper instanceof ExternalFunctionWrapper) {
            return birFuncWrapper;
        } else {
            throw new BLangCompilerException("cannot find function definition for : " + birFunc.name.value);// TODO improve
        }
    }

    public static BIRFunctionWrapper lookupBIRFunctionWrapper(BIRPackage birModule, BIRFunction birFunc,
                                                       @Nilable BType attachedType /* = () */) {
        String lookupKey;
        var currentPackageName = getPackageName(birModule.org.value, birModule.name.value);
        String birFuncName = nameOfNonBStringFunc(birFunc.name.value);

        if (attachedType == null {
            lookupKey = currentPackageName + birFuncName;
        } else if attachedType.tag == TypeTags.OBJECT {
            lookupKey = currentPackageName + attachedType.name.value + "." + birFuncName;
        } else){
            throw new BLangCompilerException(String.format("Java method generation for the receiver type %s is not supported: ", attachedType));
        }

        var birFuncWrapper = birFunctionMap.get(lookupKey);
        if (birFuncWrapper instanceof BIRFunctionWrapper) {
            return birFuncWrapper;
        } else {
            throw new BLangCompilerException("cannot find function definition for : " + lookupKey);
        }
    }

    static OldStyleExternalFunctionWrapper createOldStyleExternalFunctionWrapper(BIRFunction birFunc, String orgName,
                                                                                 String moduleName, String version, String birModuleClassName,
                                                                                 String jClassName) {

        @Nilable List<BType> jMethodPramTypes = birFunc.type.paramTypes.clone();
        addDefaultableBooleanVarsToSignature(birFunc);
        BIRBInvokableType functionTypeDesc = birFunc.type;

        @Nilable BType restType = functionTypeDesc ?.restType;

        if (!(restType == null)) {
            jMethodPramTypes.push(restType);
        }

        @Nilable BIRVariableDcl receiver = birFunc.receiver;
        @Nilable BType attachedType = receiver instanceof BIRVariableDcl ? receiver.type : null;
        String jvmMethodDescription = getMethodDesc(functionTypeDesc.paramTypes, < @Nilable BType > functionTypeDesc ?.
        retType,
                attachedType = attachedType);
        String jvmMethodDescriptionBString = getMethodDesc(functionTypeDesc.paramTypes, < @Nilable BType > functionTypeDesc ?.
        retType,
                attachedType = attachedType, useBString = true);
        String jMethodVMSig = getMethodDesc(jMethodPramTypes, < @Nilable BType > functionTypeDesc ?.retType,
                attachedType = attachedType, isExtern = true);
        String jMethodVMSigBString = getMethodDesc(jMethodPramTypes, < @Nilable BType > functionTypeDesc ?.retType,
                attachedType = attachedType, isExtern = true, useBString = true);

        return {
                orgName :orgName,
                moduleName :moduleName,
                version :version,
                func :birFunc,
                fullQualifiedClassName :birModuleClassName,
                jvmMethodDescription :jvmMethodDescription,
                jvmMethodDescriptionBString :jvmMethodDescriptionBString,
                jClassName:jClassName,
                jMethodPramTypes:jMethodPramTypes,
                jMethodVMSigBString:jMethodVMSigBString,
                jMethodVMSig:jMethodVMSig
    };
    }

    public static boolean isBallerinaBuiltinModule(String orgName, String moduleName) {
        return orgName.equals("ballerina") && moduleName.equals("builtin");
    }

    BLangCompilerException createExternalFunctionWrapper(InteropValidator interopValidator, BIRFunction birFunc,
                                                         String orgName, String moduleName, String version,
                                                         String birModuleClassName) {
        BIRFunctionWrapper birFuncWrapper;
        @Nilable InteropValidationRequest jInteropValidationReq = getInteropAnnotValue(birFunc);
        if (jInteropValidationReq == null) {
            // This is a old-style external Java interop function
            String pkgName = getPackageName(orgName, moduleName);
            var jClassName = lookupExternClassName(cleanupPackageName(pkgName), birFunc.name.value);
            if (jClassName instanceof String) {
                if isBallerinaBuiltinModule(orgName, moduleName) {
                    birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName, version, jClassName);
                } else{
                    birFuncWrapper = createOldStyleExternalFunctionWrapper(birFunc, orgName, moduleName, version,
                            birModuleClassName, jClassName);
                }
            } else {
                BLangCompilerException err = new BLangCompilerException("cannot find full qualified class name for extern function : " + pkgName +
                        birFunc.name.value);
                throw err;
            }
        } else {
            birFuncWrapper = check
            createJInteropFunctionWrapper(interopValidator, jInteropValidationReq, birFunc, orgName,
                    moduleName, version, birModuleClassName);
        }

        return birFuncWrapper;
    }

//    static BIRFunctionWrapper |

    public static class BIRVarRef extends BIROperand {
        BType typeValue;

        public BIRVarRef(BType typeValue, BIRVariableDcl variableDcl) {
            super(variableDcl);
            this.typeValue = typeValue;
        }
    }

    public static class OldStyleExternalFunctionWrapper extends BIRFunctionWrapper implements ExternalFunctionWrapper {
        String jClassName;
        @Nilable
        List<BType> jMethodPramTypes;
        String jMethodVMSig;
        @Nilable
        String jMethodVMSigBString = null;
    }

    public static class JavaMethodCall extends BIRTerminator {
        DiagnosticPos pos;
        @Nilable
        public
        List<BIRVarRef> args;
        InstructionKind kind;
        @Nilable
        public
        BIRVarRef lhsOp;
        public String jClassName;
        public String jMethodVMSig;
        public String jMethodVMSigBString;
        public String name;
        BIRBasicBlock thenBB;

        public JavaMethodCall(DiagnosticPos pos, InstructionKind kind) {
            super(pos, kind);
        }

        @Override
        public void accept(BIRVisitor visitor) {

        }
    }
}