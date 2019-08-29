/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.jvm.methodvisitor;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nativeimpl.jvm.ASMUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.LABEL;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.METHOD_VISITOR;

/**
 * Native class for visiting jvm method line number.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visitLineNumber",
        receiver = @Receiver(type = OBJECT, structType = METHOD_VISITOR, structPackage = JVM_PKG_PATH),
        args = {
                @Argument(name = "line", type = INT),
                @Argument(name = "label", type = OBJECT, structType = LABEL)
        }
)
public class VisitLineNumber {

    public static void visitLineNumber(Strand strand, ObjectValue oMv, long line, ObjectValue oLabel) {
        MethodVisitor mv = ASMUtil.getRefArgumentNativeData(oMv);
        Label label = ASMUtil.getRefArgumentNativeData(oLabel);
        mv.visitLineNumber((int) line, label);
    }
}
