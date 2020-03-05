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

    final string serviceIdentifier = "$$service$";
    if (stringutils:contains(serviceName, serviceIdentifier)) {
        if (stringutils:contains(serviceName, "$anonService$")) {
            finalString = stringutils:replace(serviceName, serviceIdentifier, "_");
        } else {
            int lastIndex = stringutils:lastIndexOf(finalString, serviceIdentifier);
            finalString = finalString.substring(0, lastIndex);
        }
    }

    return finalString;
}

function getFullQualifiedRemoteFunctionName(string moduleOrg, string moduleName, string funcName) returns string {
    if moduleName == "" {
        return funcName;
    }
    return moduleOrg + "/" + moduleName + "/" + funcName;
}

function genObserveStartWithTryBlockStart(jvm:MethodVisitor mv, LabelGenerator labelGen, bir:Function func,
                                          int localVarOffset, bir:BType? attachedType,
                                          string attachedConstructName = "") returns jvm:Label? {
    string funcName = cleanupFunctionName(<@untainted> func.name.value);
    jvm:Label tryStart = labelGen.getLabel(funcName + ":observe-try-start");
    mv.visitLabel(<jvm:Label>tryStart);
    string connectorName = attachedConstructName;
    if attachedType is bir:BObjectType {
        // Add module org and module name to remote spans.
        connectorName = getFullQualifiedRemoteFunctionName(
                        attachedType.moduleId.org, attachedType.moduleId.name, attachedType.name.value);
    }
    emitStartObservationInvocation(mv, localVarOffset, connectorName, funcName,
        "startCallableObservation");
    return tryStart;
}

function genObserveEndWithTryBlockEnd(jvm:MethodVisitor mv, LabelGenerator labelGen, BalToJVMIndexMap indexMap,
                                      bir:Function func, jvm:Label? tryStart, int localVarOffset) {
    string funcName = cleanupFunctionName(<@untainted> func.name.value);
    jvm:Label tryEnd = labelGen.getLabel(funcName + ":observe-try-end");
    jvm:Label tryCatch = labelGen.getLabel(funcName + ":observe-try-handler");
    jvm:Label tryCatchFinally = labelGen.getLabel(funcName + ":observe-try-catch-finally");
    jvm:Label tryFinally = labelGen.getLabel(funcName + ":observe-try-finally");
    jvm:Label tryBlockEndLabel = labelGen.getLabel(funcName + ":observe-try-block-end");

    // visitTryCatchBlock visited at the end since order of the error table matters.
    mv.visitTryCatchBlock(<jvm:Label>tryStart, tryEnd, tryCatch, ERROR_VALUE);
    mv.visitTryCatchBlock(<jvm:Label>tryStart, tryEnd, tryFinally, ());
    mv.visitTryCatchBlock(tryCatch, tryCatchFinally, tryFinally, ());

    bir:VariableDcl catchVarDcl = { typeValue: "any", name: { value: "$_catch_$" } };
    int catchVarIndex = indexMap.getIndex(catchVarDcl);
    bir:VariableDcl throwableVarDcl = { typeValue: "any", name: { value: "$_throwable_$" } };
    int throwableVarIndex = indexMap.getIndex(throwableVarDcl);

    // Try-To-Finally
    mv.visitLabel(tryEnd);
    mv.visitJumpInsn(GOTO, tryBlockEndLabel);

    // Catch Block
    mv.visitLabel(tryCatch);
    mv.visitVarInsn(ASTORE, catchVarIndex);
    emitReportErrorInvocation(mv, localVarOffset, catchVarIndex);

    mv.visitLabel(tryCatchFinally);
    emitStopObservationInvocation(mv, localVarOffset);
    // Re-throw caught error value
    mv.visitVarInsn(ALOAD, catchVarIndex);
    mv.visitInsn(ATHROW);

    // Finally Block
    mv.visitLabel(tryFinally);
    mv.visitVarInsn(ASTORE, throwableVarIndex);
    emitStopObservationInvocation(mv, localVarOffset);
    mv.visitVarInsn(ALOAD, throwableVarIndex);
    mv.visitInsn(ATHROW);

    mv.visitLabel(tryBlockEndLabel);
    emitStopObservationInvocation(mv, localVarOffset);
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
