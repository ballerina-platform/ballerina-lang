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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
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
public class DecodePrivateKey extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
    }

    @SuppressWarnings("unchecked")
    public static Object decodePrivateKey(Strand strand, Object keyStoreValue, Object keyAliasValue, Object password) {
        MapValue<String, Object> keyStore = (MapValue<String, Object>) keyStoreValue;
        String keyAlias = String.valueOf(keyAliasValue);
        String keyPassword = String.valueOf(password);

        PrivateKey privateKey;
        // TODO: Add support for reading key from a provided string or directly using PEM encoded file.
        if (keyStore == null) {
            throw new BallerinaException("Key store information is required");
        }

        File keyStoreFile = new File(CryptoUtils.substituteVariables(
                keyStore.get(Constants.KEY_STORE_RECORD_PATH_FIELD).toString()));
        try (FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
            KeyStore keystore = KeyStore.getInstance(Constants.KEYSTORE_TYPE_PKCS12);
            try {
                keystore.load(fileInputStream, keyStore.get(Constants.KEY_STORE_RECORD_PASSWORD_FIELD).toString()
                        .toCharArray());
            } catch (NoSuchAlgorithmException e) {
                return CryptoUtils.createCryptoError(
                        "keystore integrity check algorithm is not found: " + e.getMessage());
            }

            try {
                privateKey = (PrivateKey) keystore.getKey(keyAlias, keyPassword.toCharArray());
            } catch (NoSuchAlgorithmException e) {
                return CryptoUtils.createCryptoError("algorithm for key recovery is not found: " + e.getMessage());
            } catch (UnrecoverableKeyException e) {
                return CryptoUtils.createCryptoError("key cannot be recovered: " + e.getMessage());
            }

            //TODO: Add support for DSA/ECDSA keys and associated crypto operations
            if (privateKey.getAlgorithm().equals("RSA")) {
                MapValue<String, Object> privateKeyRecord = BallerinaValues.createRecordValue(
                        Constants.CRYPTO_PACKAGE, Constants.PRIVATE_KEY_RECORD);
                privateKeyRecord.addNativeData(Constants.NATIVE_DATA_PRIVATE_KEY, privateKey);
                privateKeyRecord.put(Constants.PRIVATE_KEY_RECORD_ALGORITHM_FIELD, privateKey.getAlgorithm());
                return privateKeyRecord;
            } else {
                return CryptoUtils.createCryptoError("not a valid RSA key");
            }
        } catch (FileNotFoundException e) {
            throw new BallerinaException("PKCS12 key store not found at: " + keyStoreFile.getAbsoluteFile());
        } catch (KeyStoreException | CertificateException | IOException e) {
            throw new BallerinaException("Unable to open keystore: " + e.getMessage());
        }
    }
}
