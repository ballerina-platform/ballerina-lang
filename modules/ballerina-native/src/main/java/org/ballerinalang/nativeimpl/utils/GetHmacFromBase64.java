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

package org.ballerinalang.nativeimpl.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
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
 * Native function ballerina.utils:getHmacFromBase64.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.utils",
        functionName = "getHmacFromBase64",
        args = { @Argument(name = "baseString", type = TypeEnum.STRING),
                 @Argument(name = "keyString", type = TypeEnum.STRING),
                 @Argument(name = "algorithm", type = TypeEnum.STRING) },
        returnType = { @ReturnType(type = TypeEnum.STRING) },
        isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns a hash of a given string in Base64 format using the key provided ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "baseString",
        value = "The string to be hashed") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "keyString",
        value = "The key string ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "algorithm",
        value = "The hashing algorithm to be used") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The hashed string") })
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
