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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Native function ballerina.utils:getHash.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.utils",
        functionName = "getHash",
        args = { @Argument(name = "baseString", type = TypeEnum.STRING),
                 @Argument(name = "algorithm", type = TypeEnum.STRING) },
        returnType = { @ReturnType(type = TypeEnum.STRING) },
        isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns a hash of a given string using the SHA-256 algorithm ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "baseString",
        value = "The string to be hashed") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "algorithm",
        value = "The hashing algorithm to be used") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The hashed string") })
public class GetHash extends AbstractNativeFunction {

    /**
     * Hashes the string contents (assumed to be UTF-8) using the SHA-256 algorithm.
     */
    private static final Logger log = LoggerFactory.getLogger(GetHash.class);

    @Override public BValue[] execute(Context context) {
        String baseString = getArgument(context, 0).stringValue();
        String algorithm = getArgument(context, 1).stringValue();

        //todo document the supported algorithm
        switch (algorithm) {
            case "SHA1":
            case "SHA256":
            case "MD5":
                break;
            default:
                throw new BallerinaException(
                        "Unsupported algorithm " + algorithm + " for HMAC calculation");
        }

        String result = "";
        try {
            baseString = baseString.replace("\\n", "\n");
            MessageDigest messageDigest = null;
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(baseString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();

            final char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[bytes.length * 2];

            for (int j = 0; j < bytes.length; j++) {
                final int byteVal = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[byteVal >>> 4];
                hexChars[j * 2 + 1] = hexArray[byteVal & 0x0F];
            }

            result = new String(hexChars);

        } catch (NoSuchAlgorithmException e) {
            throw new BallerinaException(
                    "Error while calculating HMAC for " + algorithm + ": " + e.getMessage(),
                    context);
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException("Error while encoding" + e.getMessage(), context);
        }

        return getBValues(new BString(result));
    }

}
