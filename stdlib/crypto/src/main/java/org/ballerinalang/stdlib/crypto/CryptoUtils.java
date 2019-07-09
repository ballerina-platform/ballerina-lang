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
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.exceptions.BallerinaException;

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
    private static final int[] VALID_AES_KEY_SIZES = new int[] { 16 , 24, 32 };

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
    //TODO Remove after migration : implemented using bvm values/types
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
            throw new org.ballerinalang.jvm.util.exceptions.BallerinaException(
                    "error occurred while calculating HMAC: " + e.getMessage());
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
     * Generate Hash of a byte array based on the provided hashing algorithm.
     *
     * @param algorithm algorithm used during hashing
     * @param input input byte array for hashing
     * @return calculated hash value
     */
    public static byte[] hash(String algorithm, byte[] input) {
        try {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(input);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new org.ballerinalang.jvm.util.exceptions.BallerinaException(
                    "error occurred while calculating hash: " + e.getMessage());
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
    //TODO Remove after migration : implemented using bvm values/types
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
     * Generate signature of a byte array based on the provided signing algorithm.
     *
     * @param algorithm algorithm used during signing
     * @param privateKey private key to be used during signing
     * @param input input byte array for signing
     * @return calculated signature
     * @throws InvalidKeyException if the privateKey is invalid
     */
    public static byte[] sign(String algorithm, PrivateKey privateKey, byte[] input) throws InvalidKeyException {
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(privateKey);
            sig.update(input);
            return sig.sign();
        } catch (NoSuchAlgorithmException | SignatureException e) {
            throw new org.ballerinalang.jvm.util.exceptions.BallerinaException(
                    "error occurred while calculating signature: " + e.getMessage());
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
    //TODO Remove after migration : implemented using bvm values/types
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
     * Verify signature of a byte array based on the provided signing algorithm.
     *
     * @param algorithm algorithm used during verification
     * @param publicKey public key to be used during verification
     * @param data input byte array for verification
     * @param signature signature byte array for verification
     * @return validity of the signature
     * @throws InvalidKeyException if the publicKey is invalid
     */
    public static boolean verify(String algorithm, PublicKey publicKey, byte[] data, byte[] signature)
            throws InvalidKeyException {
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (NoSuchAlgorithmException | SignatureException e) {
            throw new org.ballerinalang.jvm.util.exceptions.BallerinaException(
                    "error occurred while calculating signature: " + e.getMessage());
        }
    }

    /**
     * Create crypto error.
     *
     * @param context represent ballerina context
     * @param errMsg error description
     * @return conversion error
     */
    //TODO Remove after migration : implemented using bvm values/types
    public static BError createCryptoError(Context context, String errMsg) {
        BMap<String, BValue> errorRecord = BLangConnectorSPIUtil.createBStruct(context, Constants.CRYPTO_PACKAGE,
                Constants.CRYPTO_ERROR);
        errorRecord.put(Constants.MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, Constants.CRYPTO_ERROR_CODE, errorRecord);
    }

    /**
     * Create crypto error.
     *
     * @param errMsg error description
     * @return conversion error
     */
    public static ErrorValue createCryptoError(String errMsg) {
        return BallerinaErrors.createError(Constants.CRYPTO_ERROR_CODE, errMsg);
    }

    /**
     * Encrypt or decrypt byte array based on RSA algorithm.
     *
     * @param context BRE context used to raise error messages
     * @param cipherMode cipher mode depending on encryption or decryption
     * @param algorithmMode mode used during encryption
     * @param algorithmPadding padding used during encryption
     * @param key key to be used during encryption
     * @param input input byte array for encryption
     * @param iv initialization vector
     * @param tagSize tag size used for GCM encryption
     */
    //TODO Remove after migration : implemented using bvm values/types
    public static void rsaEncryptDecrypt(Context context, CipherMode cipherMode, String algorithmMode,
                                           String algorithmPadding, Key key, byte[] input, byte[] iv, long tagSize) {
        try {
            String transformedAlgorithmMode = transformAlgorithmMode(context, algorithmMode);
            String transformedAlgorithmPadding = transformAlgorithmPadding(context, algorithmPadding);
            if (tagSize != -1 && Arrays.stream(VALID_GCM_TAG_SIZES).noneMatch(i -> tagSize == i)) {
                context.setReturnValues(CryptoUtils.createCryptoError(context, "valid tag sizes are: " +
                        Arrays.toString(VALID_GCM_TAG_SIZES)));
                return;
            }
            AlgorithmParameterSpec paramSpec = buildParameterSpec(context, transformedAlgorithmMode, iv, (int) tagSize);
            Cipher cipher = Cipher.getInstance(Constants.RSA +  "/" + transformedAlgorithmMode + "/"
                    + transformedAlgorithmPadding);
            initCipher(cipher, cipherMode, key, paramSpec);
            context.setReturnValues(new BValueArray(cipher.doFinal(input)));
        } catch (NoSuchAlgorithmException e) {
            context.setReturnValues(CryptoUtils.createCryptoError(context, "unsupported algorithm: AES " +
                    algorithmMode + " " + algorithmPadding));
        } catch (NoSuchPaddingException e) {
            context.setReturnValues(CryptoUtils.createCryptoError(context, "unsupported padding scheme defined in " +
                    "the algorithm: AES " + algorithmMode + " " + algorithmPadding));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException |
                IllegalBlockSizeException | BallerinaException e) {
            context.setReturnValues(CryptoUtils.createCryptoError(context, e.getMessage()));
        }
    }

    /**
     * Encrypt or decrypt byte array based on RSA algorithm.
     *
     * @param cipherMode cipher mode depending on encryption or decryption
     * @param algorithmMode mode used during encryption
     * @param algorithmPadding padding used during encryption
     * @param key key to be used during encryption
     * @param input input byte array for encryption
     * @param iv initialization vector
     * @param tagSize tag size used for GCM encryption
     * @return Decrypted data or error if key is invalid
     */
    public static Object rsaEncryptDecrypt(CipherMode cipherMode, String algorithmMode,
                                           String algorithmPadding, Key key, byte[] input, byte[] iv, long tagSize) {
        try {
            String transformedAlgorithmMode = transformAlgorithmMode(algorithmMode);
            String transformedAlgorithmPadding = transformAlgorithmPadding(algorithmPadding);
            if (tagSize != -1 && Arrays.stream(VALID_GCM_TAG_SIZES).noneMatch(i -> tagSize == i)) {
                return CryptoUtils.createCryptoError("valid tag sizes are: " + Arrays.toString(VALID_GCM_TAG_SIZES));
            }
            AlgorithmParameterSpec paramSpec = buildParameterSpec(transformedAlgorithmMode, iv, (int) tagSize);
            Cipher cipher = Cipher.getInstance(Constants.RSA + "/" + transformedAlgorithmMode + "/"
                                                       + transformedAlgorithmPadding);
            initCipher(cipher, cipherMode, key, paramSpec);
            return new ArrayValue(cipher.doFinal(input));
        } catch (NoSuchAlgorithmException e) {
            return CryptoUtils.createCryptoError("unsupported algorithm: AES " + algorithmMode + " "
                                                         + algorithmPadding);
        } catch (NoSuchPaddingException e) {
            return CryptoUtils.createCryptoError("unsupported padding scheme defined in the algorithm: AES "
                                                         + algorithmMode + " " + algorithmPadding);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException |
                IllegalBlockSizeException | BallerinaException e) {
            return CryptoUtils.createCryptoError(e.getMessage());
        }
    }

    /**
     * Encrypt or decrypt byte array based on AES algorithm.
     *
     * @param context BRE context used to raise error messages
     * @param cipherMode cipher mode depending on encryption or decryption
     * @param algorithmMode mode used during encryption
     * @param algorithmPadding padding used during encryption
     * @param key key to be used during encryption
     * @param input input byte array for encryption
     * @param iv initialization vector
     * @param tagSize tag size used for GCM encryption
     */
    //TODO Remove after migration : implemented using bvm values/types
    public static void aesEncryptDecrypt(Context context, CipherMode cipherMode, String algorithmMode,
                                           String algorithmPadding, byte[] key, byte[] input, byte[] iv, long tagSize) {
        try {
            if (Arrays.stream(VALID_AES_KEY_SIZES).noneMatch(validSize -> validSize == key.length)) {
                context.setReturnValues(CryptoUtils.createCryptoError(context, "invalid key size. valid key sizes" +
                        " in bytes: " + Arrays.toString(VALID_AES_KEY_SIZES)));
                return;
            }
            String transformedAlgorithmMode = transformAlgorithmMode(context, algorithmMode);
            String transformedAlgorithmPadding = transformAlgorithmPadding(context, algorithmPadding);
            SecretKeySpec keySpec = new SecretKeySpec(key, Constants.AES);
            if (tagSize != -1 && Arrays.stream(VALID_GCM_TAG_SIZES).noneMatch(validSize -> validSize == tagSize)) {
                context.setReturnValues(CryptoUtils.createCryptoError(context, "invalid tag size. valid tag sizes" +
                        " in bytes: " + Arrays.toString(VALID_GCM_TAG_SIZES)));
                return;
            }
            AlgorithmParameterSpec paramSpec = buildParameterSpec(context, transformedAlgorithmMode, iv, (int) tagSize);
            Cipher cipher = Cipher.getInstance("AES/" + transformedAlgorithmMode + "/" + transformedAlgorithmPadding);
            initCipher(cipher, cipherMode, keySpec, paramSpec);
            context.setReturnValues(new BValueArray(cipher.doFinal(input)));
        } catch (NoSuchAlgorithmException e) {
            context.setReturnValues(CryptoUtils.createCryptoError(context, "unsupported algorithm: AES " +
                    algorithmMode + " " + algorithmPadding));
        } catch (NoSuchPaddingException e) {
            context.setReturnValues(CryptoUtils.createCryptoError(context, "unsupported padding scheme defined in " +
                    "the algorithm: AES " + algorithmMode + " " + algorithmPadding));
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException |
                InvalidKeyException | BallerinaException e) {
            context.setReturnValues(CryptoUtils.createCryptoError(context, e.getMessage()));
        }
    }

    /**
     * Encrypt or decrypt byte array based on AES algorithm.
     *
     * @param cipherMode cipher mode depending on encryption or decryption
     * @param algorithmMode mode used during encryption
     * @param algorithmPadding padding used during encryption
     * @param key key to be used during encryption
     * @param input input byte array for encryption
     * @param iv initialization vector
     * @param tagSize tag size used for GCM encryption
     * @return Decrypted data or error if key is invalid
     */
    public static Object aesEncryptDecrypt(CipherMode cipherMode, String algorithmMode,
                                           String algorithmPadding, byte[] key, byte[] input, byte[] iv, long tagSize) {
        try {
            if (Arrays.stream(VALID_AES_KEY_SIZES).noneMatch(validSize -> validSize == key.length)) {
                return CryptoUtils.createCryptoError("invalid key size. valid key sizes in bytes: " +
                                                             Arrays.toString(VALID_AES_KEY_SIZES));
            }
            String transformedAlgorithmMode = transformAlgorithmMode(algorithmMode);
            String transformedAlgorithmPadding = transformAlgorithmPadding(algorithmPadding);
            SecretKeySpec keySpec = new SecretKeySpec(key, Constants.AES);
            if (tagSize != -1 && Arrays.stream(VALID_GCM_TAG_SIZES).noneMatch(validSize -> validSize == tagSize)) {
                return CryptoUtils.createCryptoError("invalid tag size. valid tag sizes in bytes: " +
                                                             Arrays.toString(VALID_GCM_TAG_SIZES));
            }
            AlgorithmParameterSpec paramSpec = buildParameterSpec(transformedAlgorithmMode, iv, (int) tagSize);
            Cipher cipher = Cipher.getInstance("AES/" + transformedAlgorithmMode + "/" + transformedAlgorithmPadding);
            initCipher(cipher, cipherMode, keySpec, paramSpec);
            return new ArrayValue(cipher.doFinal(input));
        } catch (NoSuchAlgorithmException e) {
            return CryptoUtils.createCryptoError("unsupported algorithm: AES " +
                                                         algorithmMode + " " + algorithmPadding);
        } catch (NoSuchPaddingException e) {
            return CryptoUtils.createCryptoError("unsupported padding scheme defined in  the algorithm: AES " +
                                                         algorithmMode + " " + algorithmPadding);
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException |
                InvalidKeyException | BallerinaException e) {
            return CryptoUtils.createCryptoError(e.getMessage());
        }
    }

    /**
     * Initialize cipher for encryption and decryption operations.
     *
     * @param cipher cipher instance to initialize
     * @param cipherMode mode denoting if cipher is used for encryption or decryption
     * @param key key used for crypto operation
     * @param paramSpec cipher parameter specification
     * @throws InvalidKeyException if provided key was not valid
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
     * @param context BRE context used to raise error messages
     * @param algorithmMode algorithm mode
     * @param iv initialization vector for CBC and GCM mode
     * @param tagSize tag size for GCM mode
     * @return algorithm parameter specification
     */
    //TODO Remove after migration : implemented using bvm values/types
    private static AlgorithmParameterSpec buildParameterSpec(Context context, String algorithmMode, byte[] iv,
                                                             int tagSize) {
        switch (algorithmMode) {
            case Constants.GCM:
                if (iv == null) {
                    throw new BallerinaException("GCM mode requires 16 byte IV", context);
                } else {
                    return new GCMParameterSpec(tagSize, iv);
                }
            case Constants.CBC:
                if (iv == null) {
                    throw new BallerinaException("CBC mode requires 16 byte IV", context);
                } else {
                    return new IvParameterSpec(iv);
                }
            case Constants.ECB:
                if (iv != null) {
                    throw new BallerinaException("ECB mode cannot use IV", context);
                }
        }
        return null;
    }

    /**
     * Build algorithm parameter specification based on the cipher mode.
     *
     * @param algorithmMode algorithm mode
     * @param iv initialization vector for CBC and GCM mode
     * @param tagSize tag size for GCM mode
     * @return algorithm parameter specification
     */
    private static AlgorithmParameterSpec buildParameterSpec(String algorithmMode, byte[] iv, int tagSize) {
        switch (algorithmMode) {
            case Constants.GCM:
                if (iv == null) {
                    throw new org.ballerinalang.jvm.util.exceptions.BallerinaException("GCM mode requires 16 byte IV");
                } else {
                    return new GCMParameterSpec(tagSize, iv);
                }
            case Constants.CBC:
                if (iv == null) {
                    throw new org.ballerinalang.jvm.util.exceptions.BallerinaException("CBC mode requires 16 byte IV");
                } else {
                    return new IvParameterSpec(iv);
                }
            case Constants.ECB:
                if (iv != null) {
                    throw new org.ballerinalang.jvm.util.exceptions.BallerinaException("ECB mode cannot use IV");
                }
        }
        return null;
    }

    /**
     * Transform Ballerina algorithm mode names to Java algorithm mode names.
     *
     * @param context BRE context used to raise error messages
     * @param algorithmMode algorithm mode
     * @return transformed algorithm mode
     * @throws BallerinaException if algorithm mode is not supported
     */
    //TODO Remove after migration : implemented using bvm values/types
    private static String transformAlgorithmMode(Context context, String algorithmMode) throws BallerinaException {
        if (!algorithmMode.equals(Constants.CBC) && !algorithmMode.equals(Constants.ECB)
                && !algorithmMode.equals(Constants.GCM)) {
            throw new BallerinaException("unsupported mode: " + algorithmMode, context);
        }
        return algorithmMode;
    }

    /**
     * Transform Ballerina algorithm mode names to Java algorithm mode names.
     *
     * @param algorithmMode algorithm mode
     * @return transformed algorithm mode
     * @throws BallerinaException if algorithm mode is not supported
     */
    private static String transformAlgorithmMode(String algorithmMode) throws
                                          org.ballerinalang.jvm.util.exceptions.BallerinaException {
        if (!algorithmMode.equals(Constants.CBC) && !algorithmMode.equals(Constants.ECB)
                && !algorithmMode.equals(Constants.GCM)) {
            throw new org.ballerinalang.jvm.util.exceptions.BallerinaException("unsupported mode: " + algorithmMode);
        }
        return algorithmMode;
    }

    /**
     * Transform Ballerina padding algorithm names to Java padding algorithm names.
     *
     * @param context BRE context used to raise error messages
     * @param algorithmPadding padding algorithm name
     * @return transformed  padding algorithm name
     * @throws BallerinaException if padding algorithm is not supported
     */
    //TODO Remove after migration : implemented using bvm values/types
    private static String transformAlgorithmPadding(Context context, String algorithmPadding)
            throws BallerinaException {
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
                throw new BallerinaException("unsupported padding: " + algorithmPadding, context);
        }
        return algorithmPadding;
    }

    /**
     * Transform Ballerina padding algorithm names to Java padding algorithm names.
     *
     * @param algorithmPadding padding algorithm name
     * @return transformed  padding algorithm name
     * @throws BallerinaException if padding algorithm is not supported
     */
    private static String transformAlgorithmPadding(String algorithmPadding)
            throws org.ballerinalang.jvm.util.exceptions.BallerinaException {
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
                throw new org.ballerinalang.jvm.util.exceptions.BallerinaException(
                        "unsupported padding: " + algorithmPadding);
        }
        return algorithmPadding;
    }

    /**
     * Replace system property holders in the property values.
     * e.g. Replace ${ballerina.home} with value of the ballerina.home system property.
     *
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
            } while(matcher.find());

            matcher.appendTail(sb);
            return sb.toString();
        }
    }

    private static String getSystemVariableValue(String variableName, String defaultValue) {
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
