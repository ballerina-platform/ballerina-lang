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
package org.ballerinalang.nativeimpl.jvm.fieldvisitor;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.nativeimpl.jvm.ASMUtil;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.objectweb.asm.FieldVisitor;

import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.FIELD_VISITOR;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;

/**
 * Native class for jvm method byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visitEnd",
        receiver = @Receiver(type = OBJECT, structType = FIELD_VISITOR, structPackage = JVM_PKG_PATH)
)
public class VisitEnd extends BlockingNativeCallableUnit {

    @Override
    @Deprecated
    public void execute(Context context) {
        throw new UnsupportedOperationException("BVM Unsupported");
    }

    public static void visitEnd(Strand strand, ObjectValue oFv) {
        FieldVisitor fv = ASMUtil.getRefArgumentNativeData(oFv);
        fv.visitEnd();
    }
}
