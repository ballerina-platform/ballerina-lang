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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;
import org.ballerinalang.stdlib.time.util.TimeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Function for decoding public key.
 *
 * @since 0.990.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "crypto",
        functionName = "decodePublicKey", isPublic = true)
public class DecodePublicKey {

    @SuppressWarnings("unchecked")
    public static Object decodePublicKey(Strand strand, Object keyStoreValue, String keyAlias) {
        MapValue<String, Object> keyStore = (MapValue<String, Object>) keyStoreValue;

        File keyStoreFile = new File(
                CryptoUtils.substituteVariables(keyStore.get(Constants.KEY_STORE_RECORD_PATH_FIELD).toString()));
        try (FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
            KeyStore keystore = KeyStore.getInstance(Constants.KEYSTORE_TYPE_PKCS12);
            try {
                keystore.load(fileInputStream, keyStore.get(Constants.KEY_STORE_RECORD_PASSWORD_FIELD).toString()
                        .toCharArray());
            } catch (NoSuchAlgorithmException e) {
                throw CryptoUtils.createError("Keystore integrity check algorithm is not found: " + e.getMessage());
            }

            Certificate certificate = keystore.getCertificate(keyAlias);
            MapValue<String, Object> certificateBMap = BallerinaValues.
                    createRecordValue(Constants.CRYPTO_PACKAGE_ID, Constants.CERTIFICATE_RECORD);
            if (certificate instanceof X509Certificate) {
                X509Certificate x509Certificate = (X509Certificate) certificate;
                certificateBMap.put(Constants.CERTIFICATE_RECORD_ISSUER_FIELD,
                                    x509Certificate.getIssuerX500Principal().getName());
                certificateBMap.put(Constants.CERTIFICATE_RECORD_SUBJECT_FIELD,
                                    x509Certificate.getSubjectX500Principal().getName());
                certificateBMap.put(Constants.CERTIFICATE_RECORD_VERSION_FIELD, x509Certificate.getVersion());
                certificateBMap.put(Constants.CERTIFICATE_RECORD_SERIAL_FIELD,
                                    x509Certificate.getSerialNumber().longValue());

                certificateBMap.put(Constants.CERTIFICATE_RECORD_NOT_BEFORE_FIELD, TimeUtils
                        .createTimeRecord(TimeUtils.getTimeZoneRecord(), TimeUtils.getTimeRecord(),
                                          x509Certificate.getNotBefore().getTime(), Constants.TIMEZONE_GMT));
                certificateBMap.put(Constants.CERTIFICATE_RECORD_NOT_AFTER_FIELD, TimeUtils
                        .createTimeRecord(TimeUtils.getTimeZoneRecord(), TimeUtils.getTimeRecord(),
                                          x509Certificate.getNotAfter().getTime(), Constants.TIMEZONE_GMT));

                certificateBMap.put(Constants.CERTIFICATE_RECORD_SIGNATURE_FIELD,
                                    new ArrayValueImpl(x509Certificate.getSignature()));
                certificateBMap.put(Constants.CERTIFICATE_RECORD_SIGNATURE_ALG_FIELD, x509Certificate.getSigAlgName());
            }
            PublicKey publicKey = certificate.getPublicKey();
            //TODO: Add support for DSA/ECDSA keys and associated crypto operations
            if (publicKey.getAlgorithm().equals("RSA")) {
                MapValue<String, Object> publicKeyMap = BallerinaValues.
                        createRecordValue(Constants.CRYPTO_PACKAGE_ID, Constants.PUBLIC_KEY_RECORD);
                publicKeyMap.addNativeData(Constants.NATIVE_DATA_PUBLIC_KEY, publicKey);
                publicKeyMap.addNativeData(Constants.NATIVE_DATA_PUBLIC_KEY_CERTIFICATE, certificate);
                publicKeyMap.put(Constants.PUBLIC_KEY_RECORD_ALGORITHM_FIELD, publicKey.getAlgorithm());
                if (certificateBMap.size() > 0) {
                    publicKeyMap.put(Constants.PUBLIC_KEY_RECORD_CERTIFICATE_FIELD, certificateBMap);
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
}
