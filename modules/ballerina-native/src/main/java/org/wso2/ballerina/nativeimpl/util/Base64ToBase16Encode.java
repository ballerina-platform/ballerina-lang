/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.nativeimpl.util;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Native function ballerina.util:getHmac.
 *
 * @since 1.0.0
 */

@BallerinaFunction(
        packageName = "ballerina.util",
        functionName = "base64ToBase16Encode",
        args = { @Argument(name = "string", type = TypeEnum.STRING)},
        returnType = { @ReturnType(type = TypeEnum.STRING) },
        isPublic = true) @Component(
        name = "func.util_base64ToBase16Encode",
        immediate = true,
        service = AbstractNativeFunction.class)

public class Base64ToBase16Encode extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Base64ToBase16Encode.class);

    @Override public BValue[] execute(Context context) {
        String stringValue = getArgument(context, 0).stringValue();

        String result = "";
        byte[] keyBytes = Base64.getDecoder().decode(stringValue.getBytes(Charset.defaultCharset()));

        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[keyBytes.length * 2];

        for (int j = 0; j < keyBytes.length; j++) {
            final int byteVal = keyBytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[byteVal >>> 4];
            hexChars[j * 2 + 1] = hexArray[byteVal & 0x0F];
        }

        result = new String(hexChars);

        return getBValues(new BString(result));
    }
}
