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
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

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
        functionName = "decodePrivateKey",
        args = {
                @Argument(name = "keyStore", type = TypeKind.RECORD, structType = Constants.KEY_STORE_RECORD),
                @Argument(name = "keyAlias", type = TypeKind.STRING),
                @Argument(name = "keyPassword", type = TypeKind.STRING),
        },
        returnType = {
                @ReturnType(type = TypeKind.RECORD, structType = Constants.PRIVATE_KEY_RECORD,
                        structPackage = Constants.CRYPTO_PACKAGE),
                @ReturnType(type = TypeKind.RECORD, structType = Constants.CRYPTO_ERROR,
                        structPackage = Constants.CRYPTO_PACKAGE)
        },
        isPublic = true)
public class DecodePrivateKey extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BMap<String, BValue> keyStore = (BMap<String, BValue>) context.getNullableRefArgument(0);
        BString keyAlias = (BString) context.getNullableRefArgument(1);
        BString keyPassword = (BString) context.getNullableRefArgument(2);

        PrivateKey privateKey;
        // TODO: Add support for reading key from a provided string or directly using PEM encoded file.
        if (keyStore != null) {
            File keyStoreFile = new File(keyStore.get(Constants.KEY_STORE_RECORD_PATH_FIELD).stringValue());
            try (FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
                KeyStore keystore = KeyStore.getInstance(Constants.KEYSTORE_TYPE_PKCS12);
                try {
                    keystore.load(fileInputStream, keyStore.get(Constants.KEY_STORE_RECORD_PASSWORD_FIELD).stringValue()
                            .toCharArray());
                } catch (NoSuchAlgorithmException e) {
                    context.setReturnValues(CryptoUtils.createCryptoError(context,
                            "keystore integrity check algorithm is not found: " + e.getMessage()));
                    return;
                }

                try {
                    privateKey = (PrivateKey) keystore.getKey(keyAlias.stringValue(),
                            keyPassword.stringValue().toCharArray());
                } catch (NoSuchAlgorithmException e) {
                    context.setReturnValues(CryptoUtils.createCryptoError(context,
                            "algorithm for key recovery is not found: " + e.getMessage()));
                    return;
                } catch (UnrecoverableKeyException e) {
                    context.setReturnValues(CryptoUtils.createCryptoError(context, "key cannot be recovered: " +
                            e.getMessage()));
                    return;
                }

                //TODO: Add support for DSA/ECDSA keys and associated crypto operations
                if (privateKey.getAlgorithm().equals("RSA")) {
                    BMap<String, BValue> privateKeyStruct = BLangConnectorSPIUtil.createBStruct(context,
                            Constants.CRYPTO_PACKAGE, Constants.PRIVATE_KEY_RECORD);
                    privateKeyStruct.addNativeData(Constants.NATIVE_DATA_PRIVATE_KEY, privateKey);
                    privateKeyStruct.put(Constants.PRIVATE_KEY_RECORD_ALGORITHM_FIELD,
                            new BString(privateKey.getAlgorithm()));
                    context.setReturnValues(privateKeyStruct);
                } else {
                    context.setReturnValues(CryptoUtils.createCryptoError(context, "not a valid RSA key"));
                }
            } catch (FileNotFoundException e) {
                throw new BallerinaException("PKCS12 key store not found at: " + keyStoreFile.getAbsoluteFile(),
                        context);
            } catch (KeyStoreException | CertificateException | IOException e) {
                throw new BallerinaException("unable to open keystore: " + e.getMessage(), context);
            }
        } else {
            throw new BallerinaException("key store information is required", context);
        }
    }
}
