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

package org.ballerinalang.nativeimpl.crypto;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.HashUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Native function ballerina.crypto:getHmac.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "crypto",
        functionName = "hmac",
        args = {
                @Argument(name = "baseString", type = TypeKind.STRING),
                @Argument(name = "keyString", type = TypeKind.STRING),
                @Argument(name = "algorithm", type = TypeKind.STRING)
        },
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class Hmac extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String baseString = context.getStringArgument(0);
        String keyString = context.getStringArgument(1);
        BString algorithm = context.getNullableRefArgument(0) != null ?
                (BString) context.getNullableRefArgument(0) : new BString("");
        String hmacAlgorithm;

        //todo document the supported algorithm
        switch (algorithm.stringValue()) {
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
            result = HashUtils.toHexString(mac.doFinal(baseStringBytes));
        } catch (IllegalArgumentException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new BallerinaException("Error while calculating HMAC for " + hmacAlgorithm + ": " + e.getMessage(),
                    context);
        }
        context.setReturnValues(new BString(result));
    }
}
