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
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.util.Names;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ENCODED_DOT_CHARACTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INIT_FUNCTION_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LAMBDA_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STOP_FUNCTION_SUFFIX;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * Util methods for the method gen classes.
 *
 * @since 2.0.0
 */
public final class MethodGenUtils {

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
        String funcSuffix = STOP_FUNCTION_SUFFIX;

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

    private MethodGenUtils() {
    }
}
