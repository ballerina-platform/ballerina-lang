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
package org.ballerinalang.nativeimpl.jvm.classwriter;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.nativeimpl.jvm.ASMUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.CLASS_WRITER;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.METHOD_VISITOR;

/**
 * Native class for jvm java class byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visitMethod",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLASS_WRITER, structPackage = JVM_PKG_PATH),
        args = {
                @Argument(name = "access", type = INT),
                @Argument(name = "name", type = STRING),
                @Argument(name = "descriptor", type = STRING),
                @Argument(name = "signature", type = STRING),
                @Argument(name = "exceptions", type = ARRAY, elementType = STRING)
        },
        returnType = {
                @ReturnType(type = OBJECT, structType = METHOD_VISITOR, structPackage = JVM_PKG_PATH),
        }
)
public class VisitMethod {

    public static ObjectValue visitMethod(Strand strand, ObjectValue oCw, long access, String name, String descriptor,
                                          Object signature, Object exceptions) {
        ClassWriter cw = ASMUtil.getRefArgumentNativeData(oCw);
        MethodVisitor mv = cw.visitMethod((int) access, name, descriptor, null, null);
        ObjectValue rerunWrapperObject = ASMUtil.newObject(METHOD_VISITOR);
        ASMUtil.addNativeDataToObject(mv, rerunWrapperObject);
        return rerunWrapperObject;
    }
}
