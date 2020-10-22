/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.crypto.nativeimpl;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.apache.commons.codec.binary.Base64;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;
import org.ballerinalang.stdlib.time.util.TimeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

/**
 * Extern functions ballerina decoding keys.
 *
 * @since 0.990.3
 */
public class Decode {

    @SuppressWarnings("unchecked")
    public static Object decodePrivateKey(Object keyStoreValue, BString keyAlias, BString keyPassword) {
        BMap<BString, Object> keyStore = (BMap<BString, Object>) keyStoreValue;

        File keyStoreFile = new File(CryptoUtils.substituteVariables(
                keyStore.get(Constants.KEY_STORE_RECORD_PATH_FIELD).toString()));
        try (FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
            KeyStore keystore = KeyStore.getInstance(Constants.KEYSTORE_TYPE_PKCS12);
            try {
                keystore.load(fileInputStream, keyStore.get(Constants.KEY_STORE_RECORD_PASSWORD_FIELD).toString()
                        .toCharArray());
            } catch (NoSuchAlgorithmException e) {
                return CryptoUtils.createError("Keystore integrity check algorithm is not found: " + e.getMessage());
            }

            PrivateKey privateKey = (PrivateKey) keystore.getKey(keyAlias.getValue(),
                                                                 keyPassword.getValue().toCharArray());
            if (privateKey == null) {
                return CryptoUtils.createError("Key cannot be recovered by using given key alias: " + keyAlias);
            }
            //TODO: Add support for DSA/ECDSA keys and associated crypto operations
            if (privateKey.getAlgorithm().equals("RSA")) {
                BMap<BString, Object> privateKeyRecord = ValueCreator.
                        createRecordValue(Constants.CRYPTO_PACKAGE_ID, Constants.PRIVATE_KEY_RECORD);
                privateKeyRecord.addNativeData(Constants.NATIVE_DATA_PRIVATE_KEY, privateKey);
                privateKeyRecord.put(StringUtils.fromString(Constants.PRIVATE_KEY_RECORD_ALGORITHM_FIELD),
                                     StringUtils.fromString(privateKey.getAlgorithm()));
                return privateKeyRecord;
            } else {
                return CryptoUtils.createError("Not a valid RSA key");
            }
        } catch (FileNotFoundException e) {
            throw CryptoUtils.createError("PKCS12 key store not found at: " + keyStoreFile.getAbsoluteFile());
        } catch (KeyStoreException | CertificateException | IOException e) {
            throw CryptoUtils.createError("Unable to open keystore: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            return CryptoUtils.createError("Algorithm for key recovery is not found: " + e.getMessage());
        } catch (UnrecoverableKeyException e) {
            return CryptoUtils.createError("Key cannot be recovered: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Object decodePublicKey(Object keyStoreValue, BString keyAlias) {
        BMap<BString, Object> keyStore = (BMap<BString, Object>) keyStoreValue;

        File keyStoreFile = new File(CryptoUtils.substituteVariables(
                keyStore.get(Constants.KEY_STORE_RECORD_PATH_FIELD).toString()));
        try (FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
            KeyStore keystore = KeyStore.getInstance(Constants.KEYSTORE_TYPE_PKCS12);
            try {
                keystore.load(fileInputStream, keyStore.get(Constants.KEY_STORE_RECORD_PASSWORD_FIELD).toString()
                        .toCharArray());
            } catch (NoSuchAlgorithmException e) {
                throw CryptoUtils.createError("Keystore integrity check algorithm is not found: " + e.getMessage());
            }

            Certificate certificate = keystore.getCertificate(keyAlias.getValue());
            if (certificate == null) {
                return CryptoUtils.createError("Certificate cannot be recovered by using given key alias: " + keyAlias);
            }
            BMap<BString, Object> certificateBMap = ValueCreator.
                    createRecordValue(Constants.CRYPTO_PACKAGE_ID, Constants.CERTIFICATE_RECORD);
            if (certificate instanceof X509Certificate) {
                X509Certificate x509Certificate = (X509Certificate) certificate;
                certificateBMap.put(StringUtils.fromString(Constants.CERTIFICATE_RECORD_ISSUER_FIELD),
                                    StringUtils.fromString(x509Certificate.getIssuerX500Principal().getName()));
                certificateBMap.put(StringUtils.fromString(Constants.CERTIFICATE_RECORD_SUBJECT_FIELD),
                                    StringUtils.fromString(x509Certificate.getSubjectX500Principal().getName()));
                certificateBMap.put(StringUtils.fromString(Constants.CERTIFICATE_RECORD_VERSION_FIELD),
                                    x509Certificate.getVersion());
                certificateBMap.put(StringUtils.fromString(Constants.CERTIFICATE_RECORD_SERIAL_FIELD),
                                    x509Certificate.getSerialNumber().longValue());

                certificateBMap.put(StringUtils.fromString(Constants.CERTIFICATE_RECORD_NOT_BEFORE_FIELD),
                                    TimeUtils.createTimeRecord(TimeUtils.getTimeZoneRecord(), TimeUtils.getTimeRecord(),
                                                               x509Certificate.getNotBefore().getTime(),
                                                               StringUtils.fromString(Constants.TIMEZONE_GMT)));
                certificateBMap.put(StringUtils.fromString(Constants.CERTIFICATE_RECORD_NOT_AFTER_FIELD),
                                    TimeUtils.createTimeRecord(TimeUtils.getTimeZoneRecord(), TimeUtils.getTimeRecord(),
                                                               x509Certificate.getNotAfter().getTime(),
                                                               StringUtils.fromString(Constants.TIMEZONE_GMT)));

                certificateBMap.put(StringUtils.fromString(Constants.CERTIFICATE_RECORD_SIGNATURE_FIELD),
                                    ValueCreator.createArrayValue(x509Certificate.getSignature()));
                certificateBMap.put(StringUtils.fromString(Constants.CERTIFICATE_RECORD_SIGNATURE_ALG_FIELD),
                                    StringUtils.fromString(x509Certificate.getSigAlgName()));
            }
            PublicKey publicKey = certificate.getPublicKey();
            //TODO: Add support for DSA/ECDSA keys and associated crypto operations
            if (publicKey.getAlgorithm().equals("RSA")) {
                BMap<BString, Object> publicKeyMap = ValueCreator.
                        createRecordValue(Constants.CRYPTO_PACKAGE_ID, Constants.PUBLIC_KEY_RECORD);
                publicKeyMap.addNativeData(Constants.NATIVE_DATA_PUBLIC_KEY, publicKey);
                publicKeyMap.addNativeData(Constants.NATIVE_DATA_PUBLIC_KEY_CERTIFICATE, certificate);
                publicKeyMap.put(StringUtils.fromString(Constants.PUBLIC_KEY_RECORD_ALGORITHM_FIELD),
                                 StringUtils.fromString(publicKey.getAlgorithm()));
                if (certificateBMap.size() > 0) {
                    publicKeyMap.put(StringUtils.fromString(Constants.PUBLIC_KEY_RECORD_CERTIFICATE_FIELD),
                                     certificateBMap);
                }
                return publicKeyMap;
            } else {
                return CryptoUtils.createError("Not a valid RSA key");
            }
        } catch (FileNotFoundException e) {
            throw CryptoUtils.createError("PKCS12 key store not found at: " + keyStoreFile.getAbsoluteFile());
        } catch (KeyStoreException | CertificateException | IOException e) {
            throw CryptoUtils.createError("Unable to open keystore: " + e.getMessage());
        }
    }

    public static Object buildRsaPublicKey(BString modulus, BString exponent) {
        try {
            byte[] decodedModulus = Base64.decodeBase64(modulus.getValue());
            byte[] decodedExponent = Base64.decodeBase64(exponent.getValue());
            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1, decodedModulus),
                                                         new BigInteger(1, decodedExponent));
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);

            BMap<BString, Object> publicKeyMap = ValueCreator.
                    createRecordValue(Constants.CRYPTO_PACKAGE_ID, Constants.PUBLIC_KEY_RECORD);
            publicKeyMap.addNativeData(Constants.NATIVE_DATA_PUBLIC_KEY, publicKey);
            publicKeyMap.put(StringUtils.fromString(Constants.PUBLIC_KEY_RECORD_ALGORITHM_FIELD),
                             StringUtils.fromString(publicKey.getAlgorithm()));
            return publicKeyMap;
        } catch (InvalidKeySpecException e) {
            return CryptoUtils.createError("Invalid modulus or exponent: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw CryptoUtils.createError("Algorithm of the key factory is not found: " + e.getMessage());
        }
    }
}
