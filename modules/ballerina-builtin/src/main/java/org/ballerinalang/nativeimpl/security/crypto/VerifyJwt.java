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

package org.ballerinalang.nativeimpl.security.crypto;

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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * function ballerina.security.crypto:verifyJwt.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.security.crypto",
        functionName = "verifyJwt",
        args = { @Argument(name = "jwToken", type = TypeKind.STRING),
                @Argument(name = "key", type = TypeKind.STRING) },
        returnType = { @ReturnType(type = TypeKind.BOOLEAN) },
        isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Verify given JWT token according to the given key") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "jwToken",
        value = "JSON Web Token") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "key",
        value = "if JWT encrypt with RSA algorithm this is the public key, if JWT hashed this is the secret key") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "boolean",
        value = "Returns JWT is valid or not") })
public class VerifyJwt extends AbstractNativeFunction {

     /**
     * Verify JWT according to given key value.
     */

    @Override public BValue[] execute(Context context) {
        String jwToken = getStringArgument(context, 0);
        String key = getStringArgument(context, 1);
        String[] tokenValues;
        byte[] keyBytes;
        String message;
        String header;
        String algorithm;
        boolean returnValue;
        try {
            tokenValues = jwToken.split("\\.");
            message = tokenValues[0] + "." + tokenValues[1];
            byte[] headerBytes = Base64.getDecoder().decode(tokenValues[0].getBytes());
            header = new String(headerBytes, "utf-8");
            header = header.substring(1, header.length() - 1);
            String[] headerValues = header.split(",");
            int index = 0;
            Pattern pattern = Pattern.compile(".*alg.*");
            Matcher matcher;
            for (int i = 0; i < headerValues.length; i++) {
                matcher = pattern.matcher(headerValues[i]);
                if (matcher.matches()) {
                    index = i;
                    break;
                }
            }
            algorithm = headerValues[index].split(":")[1].replace('"', ' ').trim();
            if (algorithm.equals("RS256")) {
                keyBytes = Base64.getDecoder().decode(key.getBytes());
                KeyFactory kf = KeyFactory.getInstance("RSA");
                RSAPublicKey rsaPublicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(keyBytes));
                Signature sign = Signature.getInstance("SHA256withRSA");
                sign.initVerify(rsaPublicKey);
                sign.update(message.getBytes("UTF-8"));
                byte[] signBytes = Base64.getDecoder().decode(
                        tokenValues[2]
                                .replace('-', '+')
                                .replace('_', '/')
                                .getBytes(StandardCharsets.UTF_8)
                );
                returnValue = sign.verify(signBytes);
            } else if (algorithm.equals("HS256")) {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
                mac.init(secretKeySpec);
                String hash = Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes()));
                hash = hash.substring(0, hash.length() - 1);
                returnValue = hash.equals(tokenValues[2]);
            } else {
                returnValue = false;
            }
        } catch (SignatureException | InvalidKeyException | InvalidKeySpecException  e) {
            throw new BallerinaException("Error while verifying JWT : " + e.getMessage(), context);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new BallerinaException("Error while verifying JWT : " + e.getMessage(), context);
        }
        return getBValues(new BBoolean(returnValue));
    }
}
