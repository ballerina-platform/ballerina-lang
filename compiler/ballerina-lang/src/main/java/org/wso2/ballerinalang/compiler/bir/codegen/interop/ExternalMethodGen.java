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
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen.TerminatorGenerator.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.AnnotationProc.getInteropAnnotValue;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JFieldFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JMethodFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.createJInteropFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.desugarInteropFuncs;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.genJFieldForInteropField;

public class ExternalMethodGen {

    public static void genJMethodForBExternalFunc(BIRFunction birFunc,
                                                  ClassWriter cw,
                                                  BIRPackage birModule,
                                                  @Nilable BType attachedType /* = () */) {
        ExternalFunctionWrapper extFuncWrapper = getExternalFunctionWrapper(birModule, birFunc, attachedType);

        if (extFuncWrapper instanceof JFieldFunctionWrapper) {
            genJFieldForInteropField((JFieldFunctionWrapper) extFuncWrapper, cw, birModule);
        } else {
            genJMethodForBFunc(birFunc, cw, birModule, false, "", attachedType);
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
                BIRFunction birFunc = functions.get(count);
                count = count + 1;
                BIRFunctionWrapper extFuncWrapper = lookupBIRFunctionWrapper(module, birFunc, null);
                if (extFuncWrapper instanceof OldStyleExternalFunctionWrapper) {
                    desugarOldExternFuncs(module, (OldStyleExternalFunctionWrapper) extFuncWrapper, birFunc);
                    enrichWithDefaultableParamInits(birFunc);
                } else if (extFuncWrapper instanceof JMethodFunctionWrapper) {
                    desugarInteropFuncs(module, (JMethodFunctionWrapper) extFuncWrapper, birFunc);
                    enrichWithDefaultableParamInits(birFunc);
                } else if (!(extFuncWrapper instanceof JFieldFunctionWrapper)) {
                    enrichWithDefaultableParamInits(birFunc);
                }
            }
        }

    }

    public static void desugarOldExternFuncs(BIRPackage module, OldStyleExternalFunctionWrapper extFuncWrapper,
                                      BIRFunction birFunc) {
        BType retType = birFunc.type.retType;

        @Nilable BIROperand retRef = null;
        if (!(retType.tag == TypeTags.NIL)) {
            BIRVariableDcl localVar = getVariableDcl(birFunc.localVars.get(0));
            BIRVariableDcl variableDcl = new BIRVariableDcl(retType, localVar.name, localVar.scope, localVar.kind);
            retRef = new BIROperand(variableDcl);
            //retRef = new (variableDcl:getVariableDcl(birFunc.localVars.get(0)), type:retType);
        }

        nextId = -1;
        nextVarId = -1;

        BIRBasicBlock beginBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, "wrapperGen");
        BIRBasicBlock retBB = insertAndGetNextBasicBlock(birFunc.basicBlocks, "wrapperGen");

        @Nilable List<BIROperand> args = new ArrayList<>();

        @Nilable BIRVariableDcl receiver = birFunc.receiver;
        if (!(receiver == null)) {

            BIROperand argRef = new BIROperand(receiver);
            args.add(argRef);
        }

        @Nilable Set<BIRNode.BIRFunctionParameter> birFuncParams = birFunc.parameters.keySet();

        for (BIRNode.BIRFunctionParameter birFuncParam : birFuncParams) {
            BIROperand argRef = new BIROperand(birFuncParam);
            args.add(argRef);
        }

//        int birFuncParamIndex = 0;
//        while (birFuncParamIndex < birFuncParams.size()) {
//            BIRNode.BIRFunctionParameter birFuncParam = (BIRFunctionParam) birFuncParams.get(birFuncParamIndex);
//            BIROperand argRef = new BIROperand(variableDcl:birFuncParam, type:birFuncParam.type);
//            args.add(argRef);
//            birFuncParamIndex += 1;
//        }

        String jMethodName = birFunc.name.value;
        beginBB.terminator = new JavaMethodCall(birFunc.pos, InstructionKind.PLATFORM, args, retRef,
                extFuncWrapper.jClassName,
                extFuncWrapper.jMethodVMSigBString != null ? extFuncWrapper.jMethodVMSigBString :"<error>",
                extFuncWrapper.jMethodVMSig, jMethodName, retBB);

        retBB.terminator = new BIRTerminator.Return(birFunc.pos);
    }

    private static ExternalFunctionWrapper getExternalFunctionWrapper(BIRPackage birModule, BIRFunction birFunc,
                                                                      @Nilable BType attachedType /* = () */) {

        BIRFunctionWrapper birFuncWrapper = lookupBIRFunctionWrapper(birModule, birFunc, attachedType);
        if (birFuncWrapper instanceof ExternalFunctionWrapper) {
            return (ExternalFunctionWrapper) birFuncWrapper;
        } else {
            throw new BLangCompilerException("cannot find function definition for : " + birFunc.name.value);// TODO improve
        }
    }

    public static BIRFunctionWrapper lookupBIRFunctionWrapper(BIRPackage birModule, BIRFunction birFunc,
                                                       @Nilable BType attachedType /* = () */) {
        String lookupKey;
        String currentPackageName = getPackageName(birModule.org.value, birModule.name.value);
        String birFuncName = nameOfNonBStringFunc(birFunc.name.value);

        if (attachedType == null) {
            lookupKey = currentPackageName + birFuncName;
        } else if (attachedType.tag == TypeTags.OBJECT) {
            lookupKey = currentPackageName + toNameString(attachedType) + "." + birFuncName;
        } else {
            throw new BLangCompilerException(String.format("Java method generation for the receiver type %s " +
                    "is not supported: ", attachedType));
        }

        BIRFunctionWrapper birFuncWrapper = birFunctionMap.get(lookupKey);
        if (birFuncWrapper != null) {
            return birFuncWrapper;
        } else {
            throw new BLangCompilerException("cannot find function definition for : " + lookupKey);
        }
    }

    public static OldStyleExternalFunctionWrapper createOldStyleExternalFunctionWrapper(BIRFunction birFunc,
                                                                                        String orgName,
                                                                                        String moduleName,
                                                                                        String version,
                                                                                        String birModuleClassName,
                                                                                        String jClassName) {

        @Nilable List<BType> jMethodPramTypes = new ArrayList<>(birFunc.type.paramTypes);/*birFunc.type.paramTypes.clone();*/
        addDefaultableBooleanVarsToSignature(birFunc);
        BInvokableType functionTypeDesc = birFunc.type;

        @Nilable BType restType = functionTypeDesc.restType;

        if (!(restType == null)) {
            jMethodPramTypes.add(restType);
        }

        @Nilable BIRVariableDcl receiver = birFunc.receiver;
        @Nilable BType attachedType = receiver != null ? receiver.type : null;
        String jvmMethodDescription = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc.retType, attachedType,
                false, false);
        String jvmMethodDescriptionBString = getMethodDesc(functionTypeDesc.paramTypes, functionTypeDesc.retType,
                attachedType, false, true);
        String jMethodVMSig = getMethodDesc(jMethodPramTypes, functionTypeDesc.retType,
                attachedType, true, false);
        String jMethodVMSigBString = getMethodDesc(jMethodPramTypes, functionTypeDesc.retType, attachedType, true, true);

        return new OldStyleExternalFunctionWrapper(orgName, moduleName, version, birFunc, birModuleClassName,
                jvmMethodDescription, jvmMethodDescriptionBString, jClassName, jMethodPramTypes, jMethodVMSigBString,
                jMethodVMSig);
    }

    public static boolean isBallerinaBuiltinModule(String orgName, String moduleName) {
        return orgName.equals("ballerina") && moduleName.equals("builtin");
    }

    public static BIRFunctionWrapper createExternalFunctionWrapper(InteropValidator interopValidator,
                                                                   BIRFunction birFunc, String orgName,
                                                                   String moduleName, String version,
                                                                   String birModuleClassName) {
        BIRFunctionWrapper birFuncWrapper;
        @Nilable InteropValidationRequest jInteropValidationReq = getInteropAnnotValue(birFunc);
        if (jInteropValidationReq == null) {
            // This is a old-style external Java interop function
            String pkgName = getPackageName(orgName, moduleName);
            String jClassName = lookupExternClassName(cleanupPackageName(pkgName), birFunc.name.value);
            if (jClassName != null) {
                if (isBallerinaBuiltinModule(orgName, moduleName)) {
                    birFuncWrapper = getFunctionWrapper(birFunc, orgName, moduleName, version, jClassName);
                } else{
                    birFuncWrapper = createOldStyleExternalFunctionWrapper(birFunc, orgName, moduleName, version,
                            birModuleClassName, jClassName);
                }
            } else {
                throw new BLangCompilerException("cannot find full qualified class name for extern function : " +
                        pkgName + birFunc.name.value);
            }
        } else {
            birFuncWrapper = createJInteropFunctionWrapper(interopValidator, jInteropValidationReq, birFunc, orgName,
                    moduleName, version, birModuleClassName);
        }

        return birFuncWrapper;
    }

    public static class OldStyleExternalFunctionWrapper extends BIRFunctionWrapper implements ExternalFunctionWrapper {
        String jClassName;
        @Nilable
        List<BType> jMethodPramTypes;
        String jMethodVMSig;
        @Nilable
        String jMethodVMSigBString = null;

        public OldStyleExternalFunctionWrapper(String orgName, String moduleName, String version, BIRFunction func,
                                               String fullQualifiedClassName, String jvmMethodDescription,
                                               String jvmMethodDescriptionBString, String jClassName, List<BType>
                                                       jMethodPramTypes, String jMethodVMSig,
                                               String jMethodVMSigBString) {

            super(orgName, moduleName, version, func, fullQualifiedClassName, jvmMethodDescription,
                    jvmMethodDescriptionBString);

            this.jClassName = jClassName;
            this.jMethodPramTypes = jMethodPramTypes;
            this.jMethodVMSig = jMethodVMSig;
            this.jMethodVMSigBString = jMethodVMSigBString;
        }
    }

    public static class JavaMethodCall extends BIRTerminator {
        DiagnosticPos pos;
        @Nilable
        public
        List<BIROperand> args;
        InstructionKind kind;
        @Nilable
        public
        BIROperand lhsOp;
        public String jClassName;
        public String jMethodVMSig;
        public String jMethodVMSigBString;
        public String name;
        BIRBasicBlock thenBB;

        public JavaMethodCall(DiagnosticPos pos, InstructionKind kind, List<BIROperand> args,
                              BIROperand lhsOp, String jClassName, String jMethodVMSig,
                              String jMethodVMSigBString, String name, BIRBasicBlock thenBB) {

            super(pos, kind);
            this.pos = pos;
            this.args = args;
            this.kind = kind;
            this.lhsOp = lhsOp;
            this.jClassName = jClassName;
            this.jMethodVMSig = jMethodVMSig;
            this.jMethodVMSigBString = jMethodVMSigBString;
            this.name = name;
            this.thenBB = thenBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {

        }
    }
}