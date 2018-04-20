/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.nativeimpl.builtin.stringlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.charset.Charset;
import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

/**
 * Native function ballerina.model.string:base16ToBase64Encode.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "string.base16ToBase64Encode",
        args = {@Argument(name = "s", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true)
public class Base16ToBase64Encode extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String value = context.getStringArgument(0);
        byte[] base16DecodedValue = DatatypeConverter.parseHexBinary(value);
        byte[] base64EncodedValue = Base64.getEncoder().encode(base16DecodedValue);
        context.setReturnValues(new BString(new String(base64EncodedValue, Charset.defaultCharset())));
    }
}
