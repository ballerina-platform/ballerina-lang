/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;

import java.util.List;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CHANNEL_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;

import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.isExternFunc;

import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.birFunctionMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getBIRFunctionWrapper;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.ASYNC_CALL;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.CALL;
import static org.wso2.ballerinalang.compiler.bir.model.InstructionKind.FP_LOAD;

/**
 * BIR termination instructions to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmTerminatorGen {

    public static void genYieldCheckForLock(MethodVisitor mv, LabelGenerator labelGen, String funcName,
                                            int localVarOffset) {

        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND, "isYielded", "()Z", false);
        Label yieldLabel = labelGen.getLabel(funcName + "yield");
        mv.visitJumpInsn(IFNE, yieldLabel);
    }

    public static void loadChannelDetails(MethodVisitor mv, List<BIRNode.ChannelDetails> channels) {

        mv.visitIntInsn(BIPUSH, channels.size());
        mv.visitTypeInsn(ANEWARRAY, CHANNEL_DETAILS);
        int index = 0;
        for (BIRNode.ChannelDetails ch : channels) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, index);
            index += 1;

            mv.visitTypeInsn(NEW, CHANNEL_DETAILS);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(ch.name);

            if (ch.channelInSameStrand) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }

            if (ch.send) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }

            mv.visitMethodInsn(INVOKESPECIAL, CHANNEL_DETAILS, "<init>", String.format("(L%s;ZZ)V", STRING_VALUE),
                    false);
            mv.visitInsn(AASTORE);
        }
    }

    public static String cleanupObjectTypeName(String typeName) {

        int index = typeName.lastIndexOf(".");
        if (index > 0) {
            return typeName.substring(index + 1);
        } else {
            return typeName;
        }
    }

    static boolean isExternStaticFunctionCall(BIRInstruction callIns) {

        String methodName;
        InstructionKind kind = callIns.getKind();

        PackageID packageID;

        if (kind == CALL) {
            BIRTerminator.Call call = (BIRTerminator.Call) callIns;
            if (call.isVirtual) {
                return false;
            }
            methodName = call.name.value;
            packageID = call.calleePkg;
        } else if (kind == ASYNC_CALL) {
            BIRTerminator.AsyncCall asyncCall = (BIRTerminator.AsyncCall) callIns;
            methodName = asyncCall.name.value;
            packageID = asyncCall.calleePkg;
        } else if (kind == FP_LOAD) {
            BIRNonTerminator.FPLoad fpLoad = (BIRNonTerminator.FPLoad) callIns;
            methodName = fpLoad.funcName.value;
            packageID = fpLoad.pkgId;
        } else {
            throw new BLangCompilerException("JVM static function call generation is not supported for instruction " +
                    String.format("%s", callIns));
        }

        String key = getPackageName(packageID.orgName.value, packageID.name.value) + methodName;

        if (birFunctionMap.containsKey(key)) {
            BIRFunctionWrapper functionWrapper = getBIRFunctionWrapper(birFunctionMap.get(key));
            return isExternFunc(functionWrapper.func);
        }

        return false;
    }
}
