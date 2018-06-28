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
package org.ballerinalang.nativeimpl.builtin.stringlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;

/**
 * Convert String to Blob.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "string.toBlob",
        args = {@Argument(name = "string", type = TypeKind.STRING),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BLOB)},
        isPublic = true
)
public class ToBlob extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        try {
            String string = ctx.getStringArgument(0);
            String encoding = ctx.getStringArgument(1);
            byte[] arr = string.getBytes(encoding);
            ctx.setReturnValues(new BBlob(arr));
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("Unsupported Encoding", e);
        }
    }
}
