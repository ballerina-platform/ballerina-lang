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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nativeimpl.jvm.ASMUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.model.types.TypeKind.UNION;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.LABEL;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.METHOD_VISITOR;

/**
 * Native class for jvm try catch byte code creation.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "jvm",
        functionName = "visitTryCatchBlock",
        receiver = @Receiver(type = OBJECT, structType = METHOD_VISITOR, structPackage = JVM_PKG_PATH),
        args = {
                @Argument(name = "startLabel", type = OBJECT, structType = LABEL),
                @Argument(name = "endLabel", type = OBJECT, structType = LABEL),
                @Argument(name = "handlerLabel", type = OBJECT, structType = LABEL),
                @Argument(name = "exceptionType", type = UNION)
        }
)
public class VisitTryCatchBlock extends BlockingNativeCallableUnit {

    @Override
    @Deprecated
    public void execute(Context context) {
        throw new UnsupportedOperationException("BVM Unsupported");
    }

    public static void visitTryCatchBlock(Strand strand, ObjectValue oMv, ObjectValue oStartLabel,
                                          ObjectValue oEndLabel, ObjectValue oHandlerLabel, Object exceptionType) {
        MethodVisitor mv = ASMUtil.getRefArgumentNativeData(oMv);
        Label startLabel = ASMUtil.getRefArgumentNativeData(oStartLabel);
        Label endLabel = ASMUtil.getRefArgumentNativeData(oEndLabel);
        Label handlerLabel = ASMUtil.getRefArgumentNativeData(oHandlerLabel);
        mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, (String) exceptionType);
    }
}
