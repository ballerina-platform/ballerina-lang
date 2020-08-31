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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.nativeimpl.jvm.ASMUtil;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.objectweb.asm.ClassTooLargeException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodTooLargeException;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.BYTE;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.CLASS_WRITER;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;

/**
 * Native class for jvm java class byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "toByteArray",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLASS_WRITER, structPackage = JVM_PKG_PATH),
        returnType = {
                @ReturnType(type = ARRAY, elementType = BYTE)
        }
)
public class ToByteArray {

    private static final String NAME = "name";

    public static Object toByteArray(Strand strand, ObjectValue oCw) {
        ClassWriter cw = ASMUtil.getRefArgumentNativeData(oCw);
        try {
            return new ArrayValueImpl(cw.toByteArray());
        } catch (MethodTooLargeException e) {
            MapValue<String, String> details = new MapValueImpl<>(BTypes.typeErrorDetail);
            details.put(NAME, e.getMethodName());
            return BallerinaErrors.createError(ErrorReasons.METHOD_TOO_LARGE, details);
        } catch (ClassTooLargeException e) {
            MapValue<String, String> details = new MapValueImpl<>(BTypes.typeErrorDetail);
            details.put(NAME, e.getClassName());
            return BallerinaErrors.createError(ErrorReasons.CLASS_TOO_LARGE, details);
        } catch (Exception e) {
            return BallerinaErrors.createError(e.getMessage());
        }
    }
}
