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

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BError;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.ballerinalang.stdlib.crypto.Constants.CRYPTO_ERROR;
import static org.ballerinalang.stdlib.crypto.Constants.CRYPTO_PACKAGE_ID;

/**
 * Utility functions relevant to crypto operations.
 *
 * @since 0.95.1
 */
public class CryptoUtils {

    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");

    /**
     * Cipher mode that is used to decide if encryption or decryption operation should be performed.
     */
    public enum CipherMode { ENCRYPT, DECRYPT }

    /**
     * Valid tag sizes usable with GCM mode encryption.
     */
    private static final int[] VALID_GCM_TAG_SIZES = new int[] { 32, 63, 96, 104, 112, 120, 128 };

    /**
     * Valid AES key sizes.
     */
    private static final int[] VALID_AES_KEY_SIZES = new int[] { 16, 24, 32 };

    private CryptoUtils() {

    }

    /**
     * Generate HMAC of a byte array based on the provided HMAC algorithm.
     *
     * @param algorithm algorithm used during HMAC generation
     * @param key       key used during HMAC generation
     * @param input     input byte array for HMAC generation
     * @return calculated HMAC value
     */
    public static byte[] hmac(String algorithm, byte[] key, byte[] input) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKey);
            return mac.doFinal(input);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw CryptoUtils.createError("Error occurred while calculating HMAC: " + e.getMessage());
        }
    }

    /**
     * Generate Hash of a byte array based on the provided hashing algorithm.
     *
     * @param algorithm algorithm used during hashing
     * @param input     input byte array for hashing
     * @return calculated hash value
     */
    public static byte[] hash(String algorithm, byte[] input) {
        try {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(input);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw CryptoUtils.createError("Error occurred while calculating hash: " + e.getMessage());
        }
    }

    /**
     * Generate signature of a byte array based on the provided signing algorithm.
     *
     * @param algorithm  algorithm used during signing
     * @param privateKey private key to be used during signing
     * @param input      input byte array for signing
     * @return calculated signature or error if key is invalid
     */
    public static Object sign(String algorithm, PrivateKey privateKey, byte[] input) {
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(privateKey);
            sig.update(input);
            return ValueCreator.createArrayValue(sig.sign());
        } catch (InvalidKeyException e) {
            return CryptoUtils.createError("Uninitialized private key: " + e.getMessage());
        } catch (NoSuchAlgorithmException | SignatureException e) {
            throw CryptoUtils.createError("Error occurred while calculating signature: " + e.getMessage());
        }
    }

    /**
     * Verify signature of a byte array based on the provided signing algorithm.
     *
     * @param algorithm algorithm used during verification
     * @param publicKey public key to be used during verification
     * @param data      input byte array for verification
     * @param signature signature byte array for verification
     * @return validity of the signature or error if key is invalid
     */
    public static Object verify(String algorithm, PublicKey publicKey, byte[] data, byte[] signature) {
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (InvalidKeyException e) {
            return CryptoUtils.createError("Uninitialized public key: " + e.getMessage());
        } catch (NoSuchAlgorithmException | SignatureException e) {
            throw CryptoUtils.createError("Error occurred while calculating signature: " + e.getMessage());
        }
    }

    /**
     * Create crypto error.
     *
     * @param errMsg error description
     * @return conversion error
     */
    public static BError createError(String errMsg) {
        return ErrorCreator.createDistinctError(CRYPTO_ERROR, CRYPTO_PACKAGE_ID, StringUtils.fromString(errMsg));
    }

    /**
     * Encrypt or decrypt byte array based on RSA algorithm.
     *
     * @param cipherMode       cipher mode depending on encryption or decryption
     * @param algorithmMode    mode used during encryption
     * @param algorithmPadding padding used during encryption
     * @param key              key to be used during encryption
     * @param input            input byte array for encryption
     * @param iv               initialization vector
     * @param tagSize          tag size used for GCM encryption
     * @return Decrypted data or error if key is invalid
     */
    public static Object rsaEncryptDecrypt(CipherMode cipherMode, String algorithmMode,
                                           String algorithmPadding, Key key, byte[] input, byte[] iv, long tagSize) {
        try {
            String transformedAlgorithmMode = transformAlgorithmMode(algorithmMode);
            String transformedAlgorithmPadding = transformAlgorithmPadding(algorithmPadding);
            if (tagSize != -1 && Arrays.stream(VALID_GCM_TAG_SIZES).noneMatch(i -> tagSize == i)) {
                return CryptoUtils.createError("Valid tag sizes are: " + Arrays.toString(VALID_GCM_TAG_SIZES));
            }
            AlgorithmParameterSpec paramSpec = buildParameterSpec(transformedAlgorithmMode, iv, (int) tagSize);
            Cipher cipher = Cipher.getInstance(Constants.RSA + "/" + transformedAlgorithmMode + "/"
                    + transformedAlgorithmPadding);
            initCipher(cipher, cipherMode, key, paramSpec);
            return ValueCreator.createArrayValue(cipher.doFinal(input));
        } catch (NoSuchAlgorithmException e) {
            return CryptoUtils.createError("Unsupported algorithm: RSA " + algorithmMode + " " + algorithmPadding +
                    ": " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            return CryptoUtils.createError("Unsupported padding scheme defined in the algorithm: RSA "
                    + algorithmMode + " " + algorithmPadding + ": " + e.getMessage());
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException |
                IllegalBlockSizeException | BError e) {
            return CryptoUtils.createError("Error occurred while RSA encrypt/decrypt: " + e.getMessage());
        }
    }

    /**
     * Encrypt or decrypt byte array based on AES algorithm.
     *
     * @param cipherMode       cipher mode depending on encryption or decryption
     * @param algorithmMode    mode used during encryption
     * @param algorithmPadding padding used during encryption
     * @param key              key to be used during encryption
     * @param input            input byte array for encryption
     * @param iv               initialization vector
     * @param tagSize          tag size used for GCM encryption
     * @return Decrypted data or error if key is invalid
     */
    public static Object aesEncryptDecrypt(CipherMode cipherMode, String algorithmMode,
                                           String algorithmPadding, byte[] key, byte[] input, byte[] iv, long tagSize) {
        try {
            if (Arrays.stream(VALID_AES_KEY_SIZES).noneMatch(validSize -> validSize == key.length)) {
                return CryptoUtils.createError("Invalid key size. valid key sizes in bytes: " +
                        Arrays.toString(VALID_AES_KEY_SIZES));
            }
            String transformedAlgorithmMode = transformAlgorithmMode(algorithmMode);
            String transformedAlgorithmPadding = transformAlgorithmPadding(algorithmPadding);
            SecretKeySpec keySpec = new SecretKeySpec(key, Constants.AES);
            if (tagSize != -1 && Arrays.stream(VALID_GCM_TAG_SIZES).noneMatch(validSize -> validSize == tagSize)) {
                return CryptoUtils.createError("Invalid tag size. valid tag sizes in bytes: " +
                        Arrays.toString(VALID_GCM_TAG_SIZES));
            }
            AlgorithmParameterSpec paramSpec = buildParameterSpec(transformedAlgorithmMode, iv, (int) tagSize);
            Cipher cipher = Cipher.getInstance("AES/" + transformedAlgorithmMode + "/" + transformedAlgorithmPadding);
            initCipher(cipher, cipherMode, keySpec, paramSpec);
            return ValueCreator.createArrayValue(cipher.doFinal(input));
        } catch (NoSuchAlgorithmException e) {
            return CryptoUtils.createError("Unsupported algorithm: AES " + algorithmMode + " " + algorithmPadding +
                    ": " + e.getMessage());
        } catch (NoSuchPaddingException e) {
            return CryptoUtils.createError("Unsupported padding scheme defined in  the algorithm: AES " +
                    algorithmMode + " " + algorithmPadding + ": " + e.getMessage());
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException |
                InvalidKeyException | BError e) {
            return CryptoUtils.createError("Error occurred while AES encrypt/decrypt: " + e.getMessage());
        }
    }

    /**
     * Initialize cipher for encryption and decryption operations.
     *
     * @param cipher     cipher instance to initialize
     * @param cipherMode mode denoting if cipher is used for encryption or decryption
     * @param key        key used for crypto operation
     * @param paramSpec  cipher parameter specification
     * @throws InvalidKeyException                if provided key was not valid
     * @throws InvalidAlgorithmParameterException if algorithm parameters are insufficient
     */
    private static void initCipher(Cipher cipher, CipherMode cipherMode, Key key, AlgorithmParameterSpec paramSpec)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        switch (cipherMode) {
            case ENCRYPT:
                if (paramSpec == null) {
                    cipher.init(Cipher.ENCRYPT_MODE, key);
                } else {
                    cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
                }
                break;
            case DECRYPT:
                if (paramSpec == null) {
                    cipher.init(Cipher.DECRYPT_MODE, key);
                } else {
                    cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
                }
                break;
        }
    }

    /**
     * Build algorithm parameter specification based on the cipher mode.
     *
     * @param algorithmMode algorithm mode
     * @param iv            initialization vector for CBC and GCM mode
     * @param tagSize       tag size for GCM mode
     * @return algorithm parameter specification
     * @throws BError if initialization vector is not specified
     */
    private static AlgorithmParameterSpec buildParameterSpec(String algorithmMode, byte[] iv, int tagSize) {
        switch (algorithmMode) {
            case Constants.GCM:
                if (iv == null) {
                    throw CryptoUtils.createError("GCM mode requires 16 byte IV");
                } else {
                    return new GCMParameterSpec(tagSize, iv);
                }
            case Constants.CBC:
                if (iv == null) {
                    throw CryptoUtils.createError("CBC mode requires 16 byte IV");
                } else {
                    return new IvParameterSpec(iv);
                }
            case Constants.ECB:
                if (iv != null) {
                    throw CryptoUtils.createError("ECB mode cannot use IV");
                }
        }
        return null;
    }

    /**
     * Transform Ballerina algorithm mode names to Java algorithm mode names.
     *
     * @param algorithmMode algorithm mode
     * @return transformed algorithm mode
     * @throws BError if algorithm mode is not supported
     */
    private static String transformAlgorithmMode(String algorithmMode) throws BError {
        if (!algorithmMode.equals(Constants.CBC) && !algorithmMode.equals(Constants.ECB)
                && !algorithmMode.equals(Constants.GCM)) {
            throw CryptoUtils.createError("Unsupported mode: " + algorithmMode);
        }
        return algorithmMode;
    }

    /**
     * Transform Ballerina padding algorithm names to Java padding algorithm names.
     *
     * @param algorithmPadding padding algorithm name
     * @return transformed  padding algorithm name
     * @throws BError if padding algorithm is not supported
     */
    private static String transformAlgorithmPadding(String algorithmPadding) throws BError {
        switch (algorithmPadding) {
            case "PKCS1":
                algorithmPadding = "PKCS1Padding";
                break;
            case "PKCS5":
                algorithmPadding = "PKCS5Padding";
                break;
            case "OAEPwithMD5andMGF1":
                algorithmPadding = "OAEPWithMD5AndMGF1Padding";
                break;
            case "OAEPWithSHA1AndMGF1":
                algorithmPadding = "OAEPWithSHA-1AndMGF1Padding";
                break;
            case "OAEPWithSHA256AndMGF1":
                algorithmPadding = "OAEPWithSHA-256AndMGF1Padding";
                break;
            case "OAEPwithSHA384andMGF1":
                algorithmPadding = "OAEPWithSHA-384AndMGF1Padding";
                break;
            case "OAEPwithSHA512andMGF1":
                algorithmPadding = "OAEPWithSHA-512AndMGF1Padding";
                break;
            case "NONE":
                algorithmPadding = "NoPadding";
                break;
            default:
                throw CryptoUtils.createError("Unsupported padding: " + algorithmPadding);
        }
        return algorithmPadding;
    }

    /**
     * Replace system property holders in the property values.
     * e.g. Replace ${ballerina.home} with value of the ballerina.home system property.
     * <p>
     * This logic is originally from http-transport-utils. Since, HTTP stdlib depends on http-transport,
     * HTTP stdlib directly uses this method form the original utility. This is added here, not to make Auth stdlib
     * depend on http-transport.
     *
     * @param value string value to substitute
     * @return String substituted string
     */
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
            } while (matcher.find());

            matcher.appendTail(sb);
            return sb.toString();
        }
    }

    private static String getSystemVariableValue(String variableName, String defaultValue) {
        if (System.getProperty(variableName) != null) {
            return System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            return System.getenv(variableName);
        } else {
            return defaultValue;
        }
    }
}
