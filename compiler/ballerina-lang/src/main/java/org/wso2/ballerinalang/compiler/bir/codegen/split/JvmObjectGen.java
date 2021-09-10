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

import org.ballerinalang.compiler.BLangCompilerException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.NameHashComparator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.NamedNode;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2I;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;

/**
 * Class for generate {@link io.ballerina.runtime.api.values.BObject} related methods.
 *
 * @since 1.2.0
 */
public class JvmObjectGen {

    public static final NameHashComparator NAME_HASH_COMPARATOR = new NameHashComparator();

    private static final int MAX_CALLS_PER_CLIENT_METHOD = 100;

    public void createCallMethod(ClassWriter cw, List<BIRNode.BIRFunction> functions, String objClassName,
                                  JvmCastGen jvmCastGen) {

        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int funcNameRegIndex = 2;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        functions.sort(NAME_HASH_COMPARATOR);

        // case body
        int i = 0;
        List<Label> targetLabels = new ArrayList<>();
        String callMethodName = "call";
        for (BIRNode.BIRFunction optionalFunc : functions) {
            if (bTypesCount % MAX_CALLS_PER_CLIENT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC, callMethodName, String.format("(L%s;L%s;[L%s;)L%s;",
                        STRAND_CLASS, STRING_VALUE, OBJECT, OBJECT), null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = functions.size() - bTypesCount;
                if (remainingCases > MAX_CALLS_PER_CLIENT_METHOD) {
                    remainingCases = MAX_CALLS_PER_CLIENT_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, funcNameRegIndex, functions,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = createLabelsForEqualCheck(mv, funcNameRegIndex, functions,
                        bTypesCount, remainingCases, labels, defaultCaseLabel);
                i = 0;
                callMethodName = "call" + ++methodCount;
            }
            BIRNode.BIRFunction func = getFunction(optionalFunc);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            List<BType> paramTypes = func.type.paramTypes;
            BType retType = func.type.retType;

            String methodSig;

            // use index access, since retType can be nil.
            methodSig = JvmCodeGenUtil.getMethodDesc(paramTypes, retType);

            // load self
            mv.visitVarInsn(ALOAD, 0);

            // load strand
            mv.visitVarInsn(ALOAD, 1);
            int j = 0;
            for (BType paramType : paramTypes) {
                // load parameters
                mv.visitVarInsn(ALOAD, 3);

                // load j'th parameter
                mv.visitLdcInsn((long) j);
                mv.visitInsn(L2I);
                mv.visitInsn(AALOAD);
                jvmCastGen.addUnboxInsn(mv, paramType);
                j += 1;
            }

            mv.visitMethodInsn(INVOKEVIRTUAL, objClassName, func.name.value,
                    methodSig, false);
            if (retType == null || retType.tag == TypeTags.NIL || retType.tag == TypeTags.NEVER) {
                mv.visitInsn(ACONST_NULL);
            } else {
                jvmCastGen.addBoxInsn(mv, retType);
            }
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_CALLS_PER_CLIENT_METHOD == 0) {
                if (bTypesCount == functions.size()) {
                    createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex, "No such method: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, objClassName, "call" + methodCount, String.format(
                            "(L%s;L%s;[L%s;)L%s;", STRAND_CLASS, STRING_VALUE, OBJECT, OBJECT), false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }

        if (methodCount != 0 && bTypesCount % MAX_CALLS_PER_CLIENT_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex, "No such method: ");
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }

    static List<Label> createLabelsForEqualCheck(MethodVisitor mv, int nameRegIndex,
                                                 List<? extends NamedNode> nodes, int start, int length,
                                                 List<Label> labels, Label defaultCaseLabel) {
        List<Label> targetLabels = new ArrayList<>();
        int i = 0;
        for (int j = start; j < start + length; j++) {
            NamedNode node = nodes.get(j);
            if (node == null) {
                continue;
            }
            mv.visitLabel(labels.get(i));
            mv.visitVarInsn(ALOAD, nameRegIndex);
            mv.visitLdcInsn(node.getName().value);
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "equals",
                    String.format("(L%s;)Z", OBJECT), false);
            Label targetLabel = new Label();
            mv.visitJumpInsn(IFNE, targetLabel);
            mv.visitJumpInsn(GOTO, defaultCaseLabel);
            targetLabels.add(i, targetLabel);
            i += 1;
        }
        return targetLabels;
    }

    private static BIRNode.BIRFunction getFunction(BIRNode.BIRFunction func) {

        if (func == null) {
            throw new BLangCompilerException("Invalid function");
        }

        return func;
    }
}
