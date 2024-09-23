/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.methodgen;

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ENCODED_DOT_CHARACTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FRAME_CLASS_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LAMBDA_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAIN_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULE_FUNCTION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SCHEDULE_LOCAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_STRAND;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * Util methods for the method gen classes.
 *
 * @since 2.0.0
 */
public final class MethodGenUtils {

    public static final String FRAMES = "frames";
    static final String INIT_FUNCTION_SUFFIX = ".<init>";
    static final String STOP_FUNCTION_SUFFIX = ".<stop>";
    static final String START_FUNCTION_SUFFIX = ".<start>";

    static boolean hasInitFunction(BIRNode.BIRPackage pkg) {
        for (BIRNode.BIRFunction func : pkg.functions) {
            if (func != null && isModuleInitFunction(func)) {
                return true;
            }
        }
        return false;
    }

    static boolean isModuleInitFunction(BIRNode.BIRFunction func) {
        return func.name.value.equals(encodeModuleSpecialFuncName(INIT_FUNCTION_SUFFIX));
    }

    public static void submitToScheduler(MethodVisitor mv, String strandMetadataClass, String workerName,
                                  AsyncDataCollector asyncDataCollector) {
        String metaDataVarName = JvmCodeGenUtil.setAndGetStrandMetadataVarName(MAIN_METHOD, asyncDataCollector);
        mv.visitLdcInsn(workerName);
        mv.visitFieldInsn(GETSTATIC, strandMetadataClass, metaDataVarName, GET_STRAND_METADATA);
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD, SCHEDULE_LOCAL, false);
    }

    public static void visitReturn(MethodVisitor mv, String funcName, String className) {
        mv.visitInsn(ARETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, funcName, className);
        mv.visitEnd();
    }

    public static String encodeModuleSpecialFuncName(String funcSuffix) {
        return Utils.encodeFunctionIdentifier(funcSuffix);
    }

    static String calculateLambdaStopFuncName(PackageID id) {
        String orgName = id.orgName.value;
        String moduleName;
        if (id.isTestPkg) {
            moduleName = id.name.value + Names.TEST_PACKAGE;
        } else {
            moduleName = id.name.value;
        }
        String version = getMajorVersion(id.version.value);
        String funcSuffix = MethodGenUtils.STOP_FUNCTION_SUFFIX;

        String funcName;
        if (moduleName.equals(ENCODED_DOT_CHARACTER)) {
            funcName = ".." + funcSuffix;
        } else if (version.isEmpty()) {
            funcName = moduleName + "." + funcSuffix;
        } else {
            funcName = moduleName + ":" + version + "." + funcSuffix;
        }

        if (!orgName.equalsIgnoreCase("$anon")) {
            funcName = orgName + "/" + funcName;
        }

        return LAMBDA_PREFIX + Utils.encodeFunctionIdentifier(funcName);
    }

    public static void callSetDaemonStrand(MethodVisitor mv) {
        // set daemon strand
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);
        mv.visitMethodInsn(INVOKESTATIC, SCHEDULER, "setDaemonStrand", SET_STRAND, false);
    }

    private MethodGenUtils() {
    }

    static String getFrameClassName(String pkgName, String funcName, BType attachedType) {
        String frameClassName = pkgName + FRAME_CLASS_PREFIX;
        if (isValidType(attachedType)) {
            frameClassName += JvmCodeGenUtil.toNameString(attachedType) + "_";
        }

        return frameClassName + funcName;
    }

    private static boolean isValidType(BType attachedType) {
        BType referredAttachedType = JvmCodeGenUtil.getImpliedType(attachedType);
        return attachedType != null && (referredAttachedType.tag == TypeTags.OBJECT
                || referredAttachedType.tag == TypeTags.RECORD);
    }
}
