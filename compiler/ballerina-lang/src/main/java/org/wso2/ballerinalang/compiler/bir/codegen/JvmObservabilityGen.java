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

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVE_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;

/**
 * BIR observability model to JMV byte code generation class.
 *
 * @since 1.2.0
 */
class JvmObservabilityGen {
    static void emitStopObservationInvocation(MethodVisitor mv, int strandIndex) {
        mv.visitVarInsn(ALOAD, strandIndex);
        mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, "stopObservation",
                String.format("(L%s;)V", STRAND), false);
    }

    static void emitReportErrorInvocation(MethodVisitor mv, int strandIndex, int errorIndex) {
        mv.visitVarInsn(ALOAD, strandIndex);
        mv.visitVarInsn(ALOAD, errorIndex);
        mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, "reportError",
                String.format("(L%s;L%s;)V", STRAND, ERROR_VALUE), false);
    }

    static void emitStartObservationInvocation(MethodVisitor mv, int strandIndex, String serviceOrConnectorName,
                                               String resourceOrActionName, String observationStartMethod) {
        mv.visitVarInsn(ALOAD, strandIndex);
        mv.visitLdcInsn(cleanUpServiceName(serviceOrConnectorName));
        mv.visitLdcInsn(resourceOrActionName);
        mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, observationStartMethod,
                String.format("(L%s;L%s;L%s;)V", STRAND, STRING_VALUE, STRING_VALUE), false);
    }

    private static String cleanUpServiceName(String serviceName) {
        String finalString = serviceName;
        if (serviceName.contains("$$service$")) {
            finalString = serviceName.replace("$$service$", "_");
        }
        return finalString;
    }

    static String getFullQualifiedRemoteFunctionName(String moduleOrg, String moduleName, String funcName) {
        if (moduleName.equals("")) {
            return funcName;
        }
        return moduleOrg + "/" + moduleName + "/" + funcName;
    }
}