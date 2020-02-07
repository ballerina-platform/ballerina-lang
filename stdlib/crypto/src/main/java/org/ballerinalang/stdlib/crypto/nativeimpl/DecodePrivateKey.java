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
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Function for decoding private key.
 *
 * @since 0.990.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "crypto",
        functionName = "decodePrivateKey", isPublic = true)
public class DecodePrivateKey {

    @SuppressWarnings("unchecked")
    public static Object decodePrivateKey(Strand strand, Object keyStoreValue, String keyAlias, String keyPassword) {
        MapValue<String, Object> keyStore = (MapValue<String, Object>) keyStoreValue;

        PrivateKey privateKey;

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

            privateKey = (PrivateKey) keystore.getKey(keyAlias, keyPassword.toCharArray());
            //TODO: Add support for DSA/ECDSA keys and associated crypto operations
            if (privateKey.getAlgorithm().equals("RSA")) {
                MapValue<String, Object> privateKeyRecord = BallerinaValues.
                        createRecordValue(Constants.CRYPTO_PACKAGE_ID, Constants.PRIVATE_KEY_RECORD);
                privateKeyRecord.addNativeData(Constants.NATIVE_DATA_PRIVATE_KEY, privateKey);
                privateKeyRecord.put(Constants.PRIVATE_KEY_RECORD_ALGORITHM_FIELD, privateKey.getAlgorithm());
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
        } catch (NullPointerException e) {
            return CryptoUtils.createError("Key cannot be recovered by using given keyAlias [" + keyAlias +
                    "] and keyPassword [" + keyPassword + "]");
        } catch (UnrecoverableKeyException e) {
            return CryptoUtils.createError("Key cannot be recovered: " + e.getMessage());
        }
    }
}
