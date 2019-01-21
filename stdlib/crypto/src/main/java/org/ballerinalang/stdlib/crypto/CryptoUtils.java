/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.crypto;

import org.ballerinalang.bre.Context;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility functions relevant to crypto operations.
 *
 * @since 0.95.1
 */
public class CryptoUtils {

    private CryptoUtils() {

    }

    public static byte[] hmac(Context context, byte[] input, byte[] key, String hmacAlgorithm) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, hmacAlgorithm);
            Mac mac = Mac.getInstance(hmacAlgorithm);
            mac.init(secretKey);
            return mac.doFinal(input);
        } catch (IllegalArgumentException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new BallerinaException("Error occurred while calculating HMAC: " + e.getMessage(), context);
        }
    }

    public static byte[] hash(Context context, String algorithm, byte[] input) {
        try {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(input);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new BallerinaException("Error occurred while calculating hash: " + e.getMessage(), context);
        }
    }

    public static byte[] sign(Context context, String algorithm, PrivateKey privateKey, byte[] input) {
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(privateKey);
            sig.update(input);
            return sig.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new BallerinaException("Error occurred while calculating signature: " + e.getMessage(), context);
        }
    }
}
