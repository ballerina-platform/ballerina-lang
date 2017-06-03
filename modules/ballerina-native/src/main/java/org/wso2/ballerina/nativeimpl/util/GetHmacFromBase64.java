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

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Native function ballerina.util:getHmacFromBase64.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.util",
        functionName = "getHmacFromBase64",
        args = { @Argument(name = "baseString", type = TypeEnum.STRING),
                 @Argument(name = "keyString", type = TypeEnum.STRING),
                 @Argument(name = "algorithm", type = TypeEnum.STRING) },
        returnType = { @ReturnType(type = TypeEnum.STRING) },
        isPublic = true)

public class GetHmacFromBase64 extends AbstractNativeFunction {


    @Override public BValue[] execute(Context context) {
        String baseString = getArgument(context, 0).stringValue();
        String keyString = getArgument(context, 1).stringValue();
        String algorithm = getArgument(context, 2).stringValue();
        String hmacAlgorithm;

        //todo document the supported algorithm
        switch (algorithm) {
            case "SHA1":
                hmacAlgorithm = "HmacSHA1";
                break;
            case "SHA256":
                hmacAlgorithm = "HmacSHA256";
                break;
            case "MD5":
                hmacAlgorithm = "HmacMD5";
                break;
            default:
                throw new BallerinaException(
                        "Unsupported algorithm " + algorithm + " for HMAC calculation");
        }

        String result = "";
        try {
            byte[] keyBytes =
                    Base64.getDecoder().decode(keyString.getBytes(Charset.defaultCharset()));
            SecretKey secretKey = new SecretKeySpec(keyBytes, hmacAlgorithm);
            Mac mac = Mac.getInstance(hmacAlgorithm);
            mac.init(secretKey);
            byte[] baseStringBytes = baseString.getBytes(Charset.defaultCharset());
            result = new String(Base64.getEncoder().encode(mac.doFinal(baseStringBytes)),
                                Charset.defaultCharset());
        } catch (IllegalArgumentException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new BallerinaException(
                    "Error while calculating HMAC for " + hmacAlgorithm + ": " + e.getMessage(),
                    context);
        }

        return getBValues(new BString(result));
    }
}
