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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.encoding.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.encoding.Constants;
import org.ballerinalang.stdlib.encoding.EncodingUtil;

import java.util.Base64;

/**
 * Extern function ballerina.encoding:decodeBase64.
 *
 * @since 0.991.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "encoding",
        functionName = "decodeBase64",
        args = {
                @Argument(name = "input", type = TypeKind.STRING)
        },
        returnType = {
                @ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @ReturnType(type = TypeKind.RECORD, structType = Constants.ENCODING_ERROR,
                        structPackage = Constants.ENCODING_PACKAGE)
        },
        isPublic = true
)
public class DecodeBase64 extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String input = context.getStringArgument(0);
        try {
            byte[] output = Base64.getDecoder().decode(input);
            context.setReturnValues(new BValueArray(output));
        } catch (IllegalArgumentException e) {
            context.setReturnValues(EncodingUtil.createEncodingError(context, "input is not a valid Base64 value"));
        }
    }
}
