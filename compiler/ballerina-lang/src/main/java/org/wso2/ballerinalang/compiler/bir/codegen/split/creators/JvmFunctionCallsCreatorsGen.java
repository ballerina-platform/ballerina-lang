/*
 *  Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.ballerinalang.compiler.bir.codegen.split.creators;

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.BIRFunctionWrapper;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.V17;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CALL_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_CALLS_PER_FUNCTION_CALL_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_FUNCTION_CALLS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VISIT_MAX_SAFE_MARGIN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.FUNCTION_CALL;

/**
 * Ballerina function calls creation related JVM byte code generation class. This is required to call function by its
 * name dynamically at runtime.
 *
 * @since 2201.3.0
 */
public class JvmFunctionCallsCreatorsGen {

    private final String functionCallsClass;

    public JvmFunctionCallsCreatorsGen(PackageID packageID) {
        this.functionCallsClass = getModuleLevelClassName(packageID, MODULE_FUNCTION_CALLS_CLASS_NAME);
    }

    public void generateFunctionCallsClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                           Map<String, byte[]> jarEntries, JvmCastGen jvmCastGen,
                                           List<BIRNode.BIRFunction> sortedFunctions) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V17, ACC_PUBLIC + ACC_SUPER, functionCallsClass, null, OBJECT, null);
        createAndSplitFunctionCallMethod(cw, module.packageID, sortedFunctions, jvmPackageGen, jvmCastGen);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(functionCallsClass + CLASS_FILE_SUFFIX, bytes);
    }

    public void createAndSplitFunctionCallMethod(ClassWriter cw, PackageID packageID,
                                                 List<BIRNode.BIRFunction> functions, JvmPackageGen jvmPackageGen,
                                                 JvmCastGen jvmCastGen) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int funcNameRegIndex = 1;
        Label defaultCaseLabel = new Label();

        // case body
        int i = 0;
        List<Label> targetLabels = new ArrayList<>();
        String callMethod = CALL_FUNCTION;
        for (BIRNode.BIRFunction func : functions) {
            String encodedMethodName = Utils.encodeFunctionIdentifier(func.name.value);
            String packageName = JvmCodeGenUtil.getPackageName(packageID);
            BIRFunctionWrapper functionWrapper =
                    jvmPackageGen.lookupBIRFunctionWrapper(packageName + encodedMethodName);
            if (bTypesCount % MAX_CALLS_PER_FUNCTION_CALL_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, callMethod, FUNCTION_CALL, null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = functions.size() - bTypesCount;
                if (remainingCases > MAX_CALLS_PER_FUNCTION_CALL_METHOD) {
                    remainingCases = MAX_CALLS_PER_FUNCTION_CALL_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, funcNameRegIndex, functions,
                        bTypesCount, remainingCases, defaultCaseLabel, false);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, funcNameRegIndex, functions,
                        bTypesCount, remainingCases, labels, defaultCaseLabel, false);
                i = 0;
                callMethod = CALL_FUNCTION + ++methodCount;
            }
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            List<BType> paramTypes = func.type.paramTypes;
            BType retType = func.type.retType;

            // load strand
            mv.visitVarInsn(ALOAD, 0);
            int j = 0;
            for (BType paramType : paramTypes) {
                // load parameters
                mv.visitVarInsn(ALOAD, 2);

                // load j parameter
                mv.visitLdcInsn((long) j);
                mv.visitInsn(L2I);
                mv.visitInsn(AALOAD);
                jvmCastGen.addUnboxInsn(mv, paramType);
                j += 1;
            }
            mv.visitMethodInsn(INVOKESTATIC, functionWrapper.fullQualifiedClassName, func.name.value,
                    functionWrapper.jvmMethodDescription, false);
            int retTypeTag = JvmCodeGenUtil.getImpliedType(retType).tag;
            if (retType == null || retTypeTag == TypeTags.NIL || retTypeTag == TypeTags.NEVER) {
                mv.visitInsn(ACONST_NULL);
            } else {
                jvmCastGen.addBoxInsn(mv, retType);
            }
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_CALLS_PER_FUNCTION_CALL_METHOD == 0) {
                if (bTypesCount == functions.size()) {
                    createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex, "No such function: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKESTATIC, functionCallsClass, CALL_FUNCTION + methodCount, FUNCTION_CALL,
                            false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
                mv.visitEnd();
            }
        }

        if (methodCount != 0 && (bTypesCount % MAX_CALLS_PER_FUNCTION_CALL_METHOD != 0)) {
            createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex, "No such function: ");
            mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
            mv.visitEnd();
        }
    }
}
