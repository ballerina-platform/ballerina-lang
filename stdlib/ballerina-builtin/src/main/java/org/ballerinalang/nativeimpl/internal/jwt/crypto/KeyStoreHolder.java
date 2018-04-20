/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.internal.jwt.crypto;

import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KeyStoreHolder process the keystore, store it and provide an API to use keystore.
 * PKCS12 is the keystore format supported.
 *
 * @since 0.970.0
 */
public class KeyStoreHolder {

    private static final String PKCS12 = "pkcs12";
    private static final KeyStoreHolder keyStoreHolderInstance = new KeyStoreHolder();
    private Map<String, KeyStore> keyStoreMap;

    private KeyStoreHolder() {
        keyStoreMap = new ConcurrentHashMap<String, KeyStore>();
    }

    /**
     * Get the key store.
     *
     * @return keyStoreHolder instance
     */
    public static KeyStoreHolder getInstance() {
        return keyStoreHolderInstance;
    }

    /**
     * Get the private key for a given key alias.
     *
     * @param keyAlias    the private key alias name
     * @param keyPassword the password to protect the key
     * @param keyStoreFilePath the key store file path
     * @param keyStorePassword the key store password
     * @return private key corresponding to the alias
     * @throws BallerinaException if the keystore has not been initialized
     *                           (loaded), if the algorithm for recovering the entry cannot be found,
     *                           {@code alias} or {@code keyPassword} does not contain
     *                           the information needed to recover the key (e.g. wrong password)
     */
    public PrivateKey getPrivateKey(String keyAlias, char[] keyPassword, String keyStoreFilePath,
                                    char[] keyStorePassword) throws BallerinaException {
        KeyStore.PrivateKeyEntry pkEntry;
        try {
            pkEntry = (KeyStore.PrivateKeyEntry) getKeyStore(keyStoreFilePath, keyStorePassword)
                    .getEntry(keyAlias, new KeyStore.PasswordProtection(keyPassword));
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | java.security.KeyStoreException e) {
            throw new BallerinaException("Failed to load private key: " + keyAlias, e);
        }
        if (pkEntry == null) {
            return null;
        }
        return pkEntry.getPrivateKey();
    }

    private KeyStore getKeyStore(String keyStoreFilePath, char[] keyStorePassword) {
        KeyStore keyStore = keyStoreMap.get(keyStoreFilePath);
        if (keyStore == null) {
            KeyStore newKeyStore = loadTrustStore(keyStoreFilePath, keyStorePassword);
            keyStoreMap.put(keyStoreFilePath, newKeyStore);
            keyStore = keyStoreMap.get(keyStoreFilePath);
        }
        return keyStore;
    }

    private KeyStore loadTrustStore(String keyStoreFilePath, char[] keyStorePassword) {
        try (InputStream file = new FileInputStream(new File(keyStoreFilePath))) {
            KeyStore trustStore = KeyStore.getInstance(PKCS12);
            trustStore.load(file, keyStorePassword);
            return trustStore;
        } catch (FileNotFoundException e) {
            throw new BallerinaException("Failed to load keyStore: file not found: " + keyStoreFilePath, e);
        } catch (Exception e) {
            throw new BallerinaException("Failed to load keyStore: " + e.getMessage(), e);
        }
    }
}
