// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/bir;
import ballerina/io;
import ballerina/jvm;
import ballerina/stringutils;

function emitStopObservationInvocation(jvm:MethodVisitor mv, int strandIndex) {
    mv.visitVarInsn(ALOAD, strandIndex);
    mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, "stopObservation",
        io:sprintf("(L%s;)V", STRAND), false);
}

function emitReportErrorInvocation(jvm:MethodVisitor mv, int strandIndex, int errorIndex) {
    mv.visitVarInsn(ALOAD, strandIndex);
    mv.visitVarInsn(ALOAD, errorIndex);
    mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, "reportError",
        io:sprintf("(L%s;L%s;)V", STRAND, ERROR_VALUE), false);
}

function emitStartObservationInvocation(jvm:MethodVisitor mv, int strandIndex, string serviceOrConnectorName,
                                        string resourceOrActionName, string observationStartMethod,
                                        map<string> tags = {}) {
    mv.visitVarInsn(ALOAD, strandIndex);
    mv.visitLdcInsn(cleanUpServiceName(serviceOrConnectorName));
    mv.visitLdcInsn(resourceOrActionName);

    if (tags.length() > 0) {
        mv.visitTypeInsn(NEW, "java/util/HashMap");
        mv.visitInsn(DUP);
        mv.visitLdcInsn(tags.length());
        mv.visitInsn(L2I);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "(I)V", false);
        foreach var [key, value] in tags.entries() {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(key);
            mv.visitLdcInsn(value);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
                io:sprintf("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT), true);
            mv.visitInsn(POP);
        }
    } else {
        mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "emptyMap",
            io:sprintf("()L%s;", MAP), false);
    }
    mv.visitMethodInsn(INVOKESTATIC, OBSERVE_UTILS, observationStartMethod,
        io:sprintf("(L%s;L%s;L%s;L%s;)V", STRAND, STRING_VALUE, STRING_VALUE, MAP), false);
}

function cleanUpServiceName(string serviceName) returns string {
    string finalString = serviceName;
    if (stringutils:contains(serviceName, "$$service$")) {
        finalString = stringutils:replace(serviceName, "$$service$", "_");
    }
    return finalString;
}

function getFullQualifiedRemoteFunctionName(string moduleOrg, string moduleName, string funcName) returns string {
    if moduleName == "" {
        return funcName;
    }
    return moduleOrg + "/" + moduleName + "/" + funcName;
}

function isFunctionObserved(bir:Function func) returns boolean {
    boolean isObserved = false;
    string funcName = cleanupFunctionName(<@untainted> func.name.value);
    if (funcName != "__init" && funcName != "$__init$") {
        boolean isRemote = (func.flags & bir:REMOTE) == bir:REMOTE;
        if (isRemote) {
            isObserved = true;
        } else {
            foreach var attachment in func.annotAttachments {
                if (attachment is bir:AnnotationAttachment) {
                    string annotationFQN = attachment.moduleId.org + "/" + attachment.moduleId.name + "/"
                        + attachment.annotTagRef.value;
                    if (annotationFQN == OBSERVABLE_ANOTATION) {
                        isObserved = true;
                        break;
                    }
                }
            }
        }
    }
    return isObserved;
}
