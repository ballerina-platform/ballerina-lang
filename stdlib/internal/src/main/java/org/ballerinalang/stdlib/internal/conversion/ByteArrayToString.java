/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.internal.conversion;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;

/**
 * Convert byte array to string.
 *
 * @since 0.980
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "byteArrayToString",
        args = {@Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ByteArrayToString extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        try {
            byte[] bytes = ((BByteArray) context.getRefArgument(0)).getBytes();
            String encoding = context.getStringArgument(0);
            String value = new String(bytes, encoding);
            context.setReturnValues(new BString(value));
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("Unsupported encoding of byte array", e);
        }
    }
}
