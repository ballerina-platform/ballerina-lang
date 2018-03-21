/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.builtin.bloblib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;

/**
 * Convert BLOB to String.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "blob.toString",
        args = {@Argument(name = "b", type = TypeKind.BLOB),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ToString extends BlockingNativeCallableUnit {

    public void execute(Context context) {
        try {
            String encoding = context.getStringArgument(0);
            byte[] arr = context.getBlobArgument(0);
            String s = new String(arr, encoding);
            context.setReturnValues(new BString(s));
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("Unsupported Encoding of Blob", e);
        }
    }
}
