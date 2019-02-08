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
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility functions relevant to crypto operations.
 *
 * @since 0.95.1
 */
public class CryptoUtils {

    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");

    private CryptoUtils() {

    }

    /**
     * Generate HMAC of a byte array based on the provided HMAC algorithm.
     *
     * @param context BRE context used to raise error messages
     * @param algorithm algorithm used during HMAC generation
     * @param key key used during HMAC generation
     * @param input input byte array for HMAC generation
     * @return calculated HMAC value
     */
    public static byte[] hmac(Context context, String algorithm, byte[] key, byte[] input) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKey);
            return mac.doFinal(input);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new BallerinaException("error occurred while calculating HMAC: " + e.getMessage(), context);
        }
    }

    /**
     * Generate Hash of a byte array based on the provided hashing algorithm.
     *
     * @param context BRE context used to raise error messages
     * @param algorithm algorithm used during hashing
     * @param input input byte array for hashing
     * @return calculated hash value
     */
    public static byte[] hash(Context context, String algorithm, byte[] input) {
        try {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(input);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new BallerinaException("error occurred while calculating hash: " + e.getMessage(), context);
        }
    }


    /**
     * Generate signature of a byte array based on the provided signing algorithm.
     *
     * @param context BRE context used to raise error messages
     * @param algorithm algorithm used during signing
     * @param privateKey private key to be used during signing
     * @param input input byte array for signing
     * @return calculated signature
     * @throws InvalidKeyException if the privateKey is invalid
     */
    public static byte[] sign(Context context, String algorithm, PrivateKey privateKey, byte[] input)
            throws InvalidKeyException {
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(privateKey);
            sig.update(input);
            return sig.sign();
        } catch (NoSuchAlgorithmException | SignatureException e) {
            throw new BallerinaException("error occurred while calculating signature: " + e.getMessage(), context);
        }
    }

    /**
     * Verify signature of a byte array based on the provided signing algorithm.
     *
     * @param context BRE context used to raise error messages
     * @param algorithm algorithm used during verification
     * @param publicKey public key to be used during verification
     * @param data input byte array for verification
     * @param signature signature byte array for verification
     * @return validity of the signature
     * @throws InvalidKeyException if the publicKey is invalid
     */
    public static boolean verify(Context context, String algorithm, PublicKey publicKey, byte[] data,
                                byte[] signature) throws InvalidKeyException {
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (NoSuchAlgorithmException | SignatureException e) {
            throw new BallerinaException("error occurred while calculating signature: " + e.getMessage(), context);
        }
    }

    /**
     * Create crypto error.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error description
     * @return conversion error
     */
    public static BError createCryptoError(Context context, String errMsg) {
        BMap<String, BValue> errorRecord = BLangConnectorSPIUtil.createBStruct(context, Constants.CRYPTO_PACKAGE,
                Constants.CRYPTO_ERROR);
        errorRecord.put(Constants.MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, Constants.ENCODING_ERROR_CODE, errorRecord);
    }

    public static String substituteVariables(String value) {
        Matcher matcher = varPattern.matcher(value);
        boolean found = matcher.find();
        if (!found) {
            return value;
        } else {
            StringBuffer sb = new StringBuffer();

            do {
                String sysPropKey = matcher.group(1);
                String sysPropValue = getSystemVariableValue(sysPropKey, null);
                if (sysPropValue == null || sysPropValue.length() == 0) {
                    throw new RuntimeException("System property " + sysPropKey + " is not specified");
                }

                sysPropValue = sysPropValue.replace("\\", "\\\\");
                matcher.appendReplacement(sb, sysPropValue);
            } while(matcher.find());

            matcher.appendTail(sb);
            return sb.toString();
        }
    }

    public static String getSystemVariableValue(String variableName, String defaultValue) {
        String value;
        if (System.getProperty(variableName) != null) {
            value = System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            value = System.getenv(variableName);
        } else {
            value = defaultValue;
        }

        return value;
    }
}
