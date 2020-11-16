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

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.ScheduleFunctionInfo;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULE_FUNCTION_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;

/**
 * Util methods for the method gen classes.
 *
 * @since 2.0.0
 */
public class MethodGenUtils {
    static final String FRAMES = "frames";
    static final String INIT_FUNCTION_SUFFIX = "<init>";
    static final String STOP_FUNCTION_SUFFIX = "<stop>";
    static final String START_FUNCTION_SUFFIX = "<start>";

    static boolean hasInitFunction(BIRNode.BIRPackage pkg) {
        for (BIRNode.BIRFunction func : pkg.functions) {
            if (func != null && isModuleInitFunction(pkg, func)) {
                return true;
            }
        }
        return false;
    }

    static boolean isModuleInitFunction(BIRNode.BIRPackage module, BIRNode.BIRFunction func) {
        return func.name.value.equals(calculateModuleInitFuncName(packageToModuleId(module)));
    }

    private static String calculateModuleInitFuncName(PackageID id) {
        return calculateModuleSpecialFuncName(id, INIT_FUNCTION_SUFFIX);
    }

    static PackageID packageToModuleId(BIRNode.BIRPackage mod) {
        return new PackageID(mod.org, mod.name, mod.version);
    }

    static void genArgs(MethodVisitor mv, int schedulerVarIndex) {
        mv.visitVarInsn(ALOAD, schedulerVarIndex);
        mv.visitIntInsn(BIPUSH, 1);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
    }

    static void submitToScheduler(MethodVisitor mv, String moduleClassName,
                                   String workerName, AsyncDataCollector asyncDataCollector) {
        String metaDataVarName = JvmCodeGenUtil.getStrandMetadataVarName("main");
        asyncDataCollector.getStrandMetadata().putIfAbsent(metaDataVarName, new ScheduleFunctionInfo("main"));
        mv.visitLdcInsn(workerName);
        mv.visitFieldInsn(GETSTATIC, moduleClassName, metaDataVarName, String.format("L%s;", STRAND_METADATA));
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULE_FUNCTION_METHOD,
                           String.format("([L%s;L%s;L%s;L%s;L%s;L%s;)L%s;", OBJECT, B_FUNCTION_POINTER, STRAND_CLASS,
                                         TYPE, STRING_VALUE, STRAND_METADATA, FUTURE_VALUE), false);
    }

    static void visitReturn(MethodVisitor mv) {
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    static String calculateModuleSpecialFuncName(PackageID id, String funcSuffix) {
        String orgName = id.orgName.value;
        String moduleName = id.name.value;
        String version = id.version.value;

        String funcName;
        if (moduleName.equals(".")) {
            funcName = ".." + funcSuffix;
        } else if (version.equals("")) {
            funcName = moduleName + "." + funcSuffix;
        } else {
            funcName = moduleName + ":" + version + "." + funcSuffix;
        }

        if (!orgName.equalsIgnoreCase("$anon")) {
            funcName = orgName + "/" + funcName;
        }

        return funcName;
    }

    private MethodGenUtils() {
    }

    static String getFrameClassName(String pkgName, String funcName, BType attachedType) {
        String frameClassName = pkgName;
        if (isValidType(attachedType)) {
            frameClassName += JvmCodeGenUtil.cleanupReadOnlyTypeName(JvmCodeGenUtil.toNameString(attachedType)) + "_";
        }

        return frameClassName + JvmCodeGenUtil.cleanupFunctionName(funcName) + "Frame";
    }

    private static boolean isValidType(BType attachedType) {
        return attachedType != null && (attachedType.tag == TypeTags.OBJECT || attachedType.tag == TypeTags.RECORD);
    }
}
