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

package org.ballerinalang.nativeimpl.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Native function ballerina.util:getHmac.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.util",
        functionName = "getHmac",
        args = {
                @Argument(name = "baseString", type = TypeKind.STRING),
                @Argument(name = "keyString", type = TypeKind.STRING),
                @Argument(name = "algorithm", type = TypeKind.STRING)
        },
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetHmac extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String baseString = getStringArgument(context, 0);
        String keyString = getStringArgument(context, 1);
        String algorithm = getStringArgument(context, 2);
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
            throw new BallerinaException("Unsupported algorithm " + algorithm + " for HMAC calculation");
        }

        String result;
        try {
            byte[] keyBytes = keyString.getBytes(Charset.defaultCharset());
            SecretKey secretKey = new SecretKeySpec(keyBytes, hmacAlgorithm);
            Mac mac = Mac.getInstance(hmacAlgorithm);
            mac.init(secretKey);
            byte[] baseStringBytes = baseString.getBytes(Charset.defaultCharset());
            result = new String(Base64.getEncoder().encode(mac.doFinal(baseStringBytes)), Charset.defaultCharset());
        } catch (IllegalArgumentException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new BallerinaException("Error while calculating HMAC for " + hmacAlgorithm + ": " + e.getMessage(),
                    context);
        }

        return getBValues(new BString(result));
    }
}
