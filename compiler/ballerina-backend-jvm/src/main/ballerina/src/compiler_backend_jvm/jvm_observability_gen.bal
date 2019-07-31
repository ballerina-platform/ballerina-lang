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

function emitStopObservationInvocation(jvm:MethodVisitor mv, int strandIndex) {
    mv.visitVarInsn(ALOAD, strandIndex);
    mv.visitMethodInsn(INVOKESTATIC, "org/ballerinalang/jvm/observability/ObserveUtils", "stopObservation",
        io:sprintf("(L%s;)V", STRAND), false);
}

function emitStartObservationInvocation(jvm:MethodVisitor mv, int strandIndex, string serviceOrConnectorName,
                                        string resourceOrActionName, string observationStartMethod) {
    mv.visitVarInsn(ALOAD, strandIndex);
    mv.visitLdcInsn(serviceOrConnectorName);
    mv.visitLdcInsn(resourceOrActionName);
    mv.visitMethodInsn(INVOKESTATIC, "org/ballerinalang/jvm/observability/ObserveUtils", observationStartMethod,
        io:sprintf("(L%s;L%s;L%s;)V", STRAND, STRING_VALUE, STRING_VALUE), false);
}
