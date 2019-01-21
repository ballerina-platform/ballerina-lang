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
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

/**
 * Function for decoding public key.
 *
 * @since 0.990.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "crypto",
        functionName = "decodePublicKey",
        args = {
                @Argument(name = "keyStore", type = TypeKind.STRING),
                @Argument(name = "keyStorePassword", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @Argument(name = "keyAlias", type = TypeKind.STRING)
        },
        returnType = {@ReturnType(type = TypeKind.RECORD, structType = "PrivateKey",
                structPackage = "ballerina/crypto")},
        isPublic = true)
public class DecodePublicKey extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BString keyStore = (BString) context.getNullableRefArgument(0);
        BString keyStorePassword = (BString) context.getNullableRefArgument(1);
        BString keyAlias = (BString) context.getNullableRefArgument(2);

        PublicKey publicKey = null;
        // TODO: Add support for reading key from a provided string or directly using PEM encoded file.
        if (keyStore != null) {
            File keyStoreFile = new File(keyStore.stringValue());
            try (FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
                KeyStore keystore = KeyStore.getInstance("PKCS12");
                keystore.load(fileInputStream, keyStorePassword.stringValue().toCharArray());
                publicKey = keystore.getCertificate(keyAlias.stringValue()).getPublicKey();
            } catch (FileNotFoundException e) {
                throw new BallerinaException("PKCS12 key store not found at: " + keyStoreFile.getAbsoluteFile(),
                        context);
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                throw new BallerinaException(e.getMessage(), e);
            }
        } else {
            throw new BallerinaException("Key store information is required", context);
        }

        BMap<String, BValue> privateKeyStruct = BLangConnectorSPIUtil.createBStruct(context,
                Constants.CRYPTO_PACKAGE, Constants.PUBLIC_KEY_STRUCT);
        privateKeyStruct.addNativeData(Constants.NATIVE_DATA_PUBLIC_KEY, publicKey);
        privateKeyStruct.put("algorithm", new BString(publicKey.getAlgorithm()));
        context.setReturnValues(privateKeyStruct);
    }
}
