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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.nativeimpl.jvm.ASMUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.LABEL;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.METHOD_VISITOR;

/**
 * Native class for jvm method byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visitLookupSwitchInsn",
        receiver = @Receiver(type = OBJECT, structType = METHOD_VISITOR, structPackage = JVM_PKG_PATH),
        args = {
                @Argument(name = "defaultLabel", type = OBJECT, structType = LABEL),
                @Argument(name = "keys", type = ARRAY, elementType = INT),
                @Argument(name = "labels", type = ARRAY, elementType = OBJECT)
        }
)
public class VisitLookupSwitchInsn extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        MethodVisitor mv = ASMUtil.getRefArgumentNativeData(context, 0);
        Label defaultLabel = ASMUtil.getRefArgumentNativeData(context, 1);
        BValue val = context.getRefArgument(2);
        Label[] labels = getLabels(context.getRefArgument(3));
        mv.visitLookupSwitchInsn(defaultLabel, getKeys(val), labels);
    }

    private int[] getKeys(BValue value) {
        if (!(value instanceof BValueArray)) {
            return null;
        }

        BValueArray valueArray = (BValueArray) value;
        int[] intArray = new int[(int) valueArray.size()];
        for (int i = 0; i < valueArray.size(); i++) {
            intArray[i] = (int) valueArray.getInt(i);
        }
        return intArray;
    }

    private Label[] getLabels(BValue value) {
        if (!(value instanceof BValueArray)) {
            return null;
        }

        BValueArray valueArray = (BValueArray) value;
        Label[] labels = new Label[(int) valueArray.size()];
        for (int i = 0; i < valueArray.size(); i++) {
            value = valueArray.getBValue(i);
            Object obj = ((BMap<String, BValue>) value).getNativeData(ASMUtil.NATIVE_KEY);
            labels[i] = (Label) obj;
        }

        return labels;
    }
}
