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

package org.ballerinalang.stdlib.crypto.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;

/**
 * Function for generating CRC32 hashes.
 *
 * @since 0.970.0-alpha1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "crypto",
        functionName = "decodePrivateKey",
        args = {
                @Argument(name = "keyContent", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @Argument(name = "keyFile", type = TypeKind.STRING),
                @Argument(name = "keyStore", type = TypeKind.STRING),
                @Argument(name = "keyStorePassword", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @Argument(name = "keyAlias", type = TypeKind.STRING),
                @Argument(name = "keyPassword", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
        },
        returnType = {@ReturnType(type = TypeKind.RECORD, structType = "PrivateKey",
                structPackage = "ballerina/crypto")},
        isPublic = true)
public class DecodePrivateKey extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BValue keyContentBValue = context.getNullableRefArgument(0);
        BString keyFile = (BString) context.getNullableRefArgument(1);
        BString keyStore = (BString) context.getNullableRefArgument(2);
        BString keyStorePassword = (BString) context.getNullableRefArgument(3);
        BString keyAlias = (BString) context.getNullableRefArgument(4);
        BString keyPassword = (BString) context.getNullableRefArgument(5);

        try {
            PrivateKey privateKey = null;
            if (keyFile != null || keyContentBValue != null) {
                PEMParser pemParser = null;
                if (keyFile != null) {
                    pemParser = new PEMParser(new FileReader(keyFile.stringValue()));
                } else {
                    byte[] content = ((BValueArray) keyContentBValue).getBytes();
                    pemParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(content)));
                }
                JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();

                Object object = pemParser.readObject();
                if (object instanceof PEMEncryptedKeyPair) {
                    if (keyPassword == null) {
                        throw new BallerinaException("Password required to decrypt the private key", context);
                    } else {
                        Security.addProvider(new BouncyCastleProvider());
                        PEMDecryptorProvider pemDecryptorProvider = new JcePEMDecryptorProviderBuilder()
                                .build(keyPassword.stringValue().toCharArray());
                        KeyPair keyPair = jcaPEMKeyConverter.getKeyPair(((PEMEncryptedKeyPair) object)
                                .decryptKeyPair(pemDecryptorProvider));
                        privateKey = keyPair.getPrivate();
                    }
                } else if (object instanceof PEMKeyPair) {
                    KeyPair keyPair = jcaPEMKeyConverter.getKeyPair((PEMKeyPair) object);
                    privateKey = keyPair.getPrivate();
                } else if (object instanceof EncryptedPrivateKeyInfo) {
                    // TODO:
                } else if (object instanceof PrivateKeyInfo) {
                    privateKey = jcaPEMKeyConverter.getPrivateKey((PrivateKeyInfo) object);
                } else {
                    throw new BallerinaException("Unrecognized private key format.", context);
                }
            } else if (keyStore != null) {
                KeyStore keystore = KeyStore.getInstance("PKCS12");
                keystore.load(new FileInputStream(keyStore.stringValue()),
                        keyStorePassword.stringValue().toCharArray());
                privateKey = (PrivateKey) keystore.getKey(keyAlias.stringValue(),
                        keyPassword.stringValue().toCharArray());
            } else {
                throw new BallerinaException("Key content, key file or key store information is required", context);
            }

            BMap<String, BValue> privateKeyStruct = BLangConnectorSPIUtil.createBStruct(context,
                    Constants.CRYPTO_PACKAGE, Constants.PRIVATE_KEY_STRUCT);
            privateKeyStruct.addNativeData(Constants.NATIVE_DATA_PRIVATE_KEY, privateKey);
            privateKeyStruct.put("algorithm", new BString(privateKey.getAlgorithm()));
            context.setReturnValues(privateKeyStruct);
        } catch (Exception e) {
            throw new BallerinaException("Error occurred while parsing private key: " + e.getMessage(), context);
        }
    }
}
