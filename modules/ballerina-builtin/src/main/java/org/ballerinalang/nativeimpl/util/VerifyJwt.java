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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Native function ballerina.utils:getHash.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.utils",
        functionName = "verifyJwt",
        args = { @Argument(name = "jwToken", type = TypeKind.STRING),
                @Argument(name = "key", type = TypeKind.STRING) },
        returnType = { @ReturnType(type = TypeKind.BOOLEAN) },
        isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns a hash of a given string using the SHA-256 algorithm ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "jwToken",
        value = "The string to be hashed") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "key",
        value = "The key string ") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "boolean",
        value = "The hashed string") })
public class VerifyJwt extends AbstractNativeFunction {

    /**
     * Hashes the string contents (assumed to be UTF-8) using the SHA-256 algorithm.
     */

    @Override public BValue[] execute(Context context) {
        String jwToken = getStringArgument(context, 0);
        String key = getStringArgument(context, 1);
        String[] tokenValues;
        byte[] keyByte;
        String message;
        String header;
        String algorithm;
        boolean returnValue = false;
        JsonObject headerJson;
        JsonParser jsonParser = new JsonParser();
        try {
            tokenValues = jwToken.split("\\.");
            message = tokenValues[0] + "." + tokenValues[1];
            header = new String(Base64.decodeBase64(tokenValues[0]), "UTF-8");
            headerJson = (JsonObject) jsonParser.parse(header);
            algorithm = headerJson.get("alg").getAsString();
            if (algorithm.equals("RS256")) {
                keyByte = Base64.decodeBase64(key);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                RSAPublicKey rsaPublicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(keyByte));
                Signature sign = Signature.getInstance("SHA256withRSA");
                sign.initVerify(rsaPublicKey);
                sign.update(message.getBytes("UTF-8"));
                returnValue = sign.verify(Base64.decodeBase64(tokenValues[2].getBytes("UTF-8")));
            }
            if (algorithm.equals("HS256")) {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
                mac.init(secretKeySpec);
                String hash = Base64.encodeBase64String(mac.doFinal(message.getBytes()));
                hash = hash.substring(0, hash.length() - 1);
                returnValue = hash.equals(tokenValues[2]);
            }
        } catch (SignatureException | InvalidKeyException | InvalidKeySpecException  e) {
            throw new BallerinaException("Error while calculating SHA256 with RSA for : " + e.getMessage(), context);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new BallerinaException("Error while calculating SHA256 with RSA for : " + e.getMessage(), context);
        }
        return getBValues(new BBoolean(returnValue));
    }

}
