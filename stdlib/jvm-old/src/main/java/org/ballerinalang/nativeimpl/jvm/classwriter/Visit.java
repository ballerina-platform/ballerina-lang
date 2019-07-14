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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.nativeimpl.jvm.ASMUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.CLASS_WRITER;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;

/**
 * Native class for jvm java class byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visit",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLASS_WRITER, structPackage = JVM_PKG_PATH),
        args = {
                @Argument(name = "versionNumber", type = INT),
                @Argument(name = "access", type = INT),
                @Argument(name = "name", type = STRING),
                @Argument(name = "signature", type = STRING),
                @Argument(name = "superName", type = STRING),
                @Argument(name = "interfaces", type = ARRAY, elementType = STRING)
        }
)
public class Visit extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        BallerinaClassWriter cw = ASMUtil.getRefArgumentNativeData(context, 0);
        int versionNumber = (int) context.getIntArgument(0);
        int access = (int) context.getIntArgument(1);
        String name = context.getStringArgument(0);
        String superName = context.getStringArgument(1);
        String[] interfaces = getInterfaces(context.getNullableRefArgument(2));
        cw.visitClass(versionNumber, access, name, null, superName, interfaces);
    }

    private String[] getInterfaces(BValue value) {
        if (!(value instanceof BValueArray)) {
            return null;
        }

        BValueArray valueArray = (BValueArray) value;
        String[] stringArray = new String[(int) valueArray.size()];
        for (int i = 0; i < valueArray.size(); i++) {
            stringArray[i] = valueArray.getString(i);
        }
        return stringArray;
    }
}
