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
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationAttachment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.util.Flags;

import java.util.Map;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVABLE_ANNOTATION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBSERVE_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;

/**
 * BIR observability model to JVM byte code generation class.
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
        emitStartObservationInvocation(mv, strandIndex, serviceOrConnectorName, resourceOrActionName,
                observationStartMethod, null);
    }

    static void emitStartObservationInvocation(MethodVisitor mv, int strandIndex, String serviceOrConnectorName,
                                               String resourceOrActionName, String observationStartMethod,
                                               Map<String, String> tags) {
        mv.visitVarInsn(ALOAD, strandIndex);
        mv.visitLdcInsn(cleanUpServiceName(serviceOrConnectorName));
        mv.visitLdcInsn(resourceOrActionName);

        if (tags != null) {
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitLdcInsn(tags.size());
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "(I)V", false);
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                mv.visitInsn(DUP);
                mv.visitLdcInsn(entry.getKey());
                mv.visitLdcInsn(entry.getValue());
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
                        String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), true);
                mv.visitInsn(POP);
            }
        } else {
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "emptyMap",
                    String.format("()L%s;", MAP), false);
        }

        mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, observationStartMethod,
                String.format("(L%s;L%s;L%s;L%s;)V", STRAND, STRING_VALUE, STRING_VALUE, MAP), false);
    }

    private static String cleanUpServiceName(String serviceName) {
        final String serviceIdentifier = "$$service$";
        if (serviceName.contains(serviceIdentifier)) {
            if (serviceName.contains("$anonService$")) {
                return serviceName.replace(serviceIdentifier, "_");
            } else {
                return serviceName.substring(0, serviceName.lastIndexOf(serviceIdentifier));
            }
        }
        return serviceName;
    }

    static String getFullQualifiedRemoteFunctionName(String moduleOrg, String moduleName, String funcName) {

        if (moduleName.equals("")) {
            return funcName;
        }
        return moduleOrg + "/" + moduleName + "/" + funcName;
    }

    static boolean isFunctionObserved(BIRFunction func) {
        boolean isObserved = false;
        String funcName = JvmMethodGen.cleanupFunctionName(func.name.value);
        if (!Objects.equals(funcName, "__init") && !Objects.equals(funcName, "$__init$")) {
            boolean isRemote = (func.flags & Flags.REMOTE) == Flags.REMOTE;
            if (isRemote) {
                isObserved = true;
            } else {
                for (BIRAnnotationAttachment attachment : func.annotAttachments) {
                    String annotationFQN = attachment.packageID.orgName + "/" + attachment.packageID.name + "/"
                            + attachment.annotTagRef.value;
                    if (Objects.equals(annotationFQN, OBSERVABLE_ANNOTATION)) {
                        isObserved = true;
                        break;
                    }
                }
            }
        }
        return isObserved;
    }
}
