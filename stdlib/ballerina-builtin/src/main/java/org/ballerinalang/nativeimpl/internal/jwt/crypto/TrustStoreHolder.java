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
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TrustStoreHolder process the trust store, store it and provide an API to use trust store.
 * PKCS12 is the keystore format supported.
 *
 * @since 0.970.0
 */
public class TrustStoreHolder {

    private static final String PKCS12 = "pkcs12";
    private static final TrustStoreHolder trustStoreHolderInstance = new TrustStoreHolder();
    private Map<String, KeyStore> trustStoreMap;

    private TrustStoreHolder() {
        trustStoreMap = new ConcurrentHashMap<String, KeyStore>();
    }

    /**
     * Get the trust store.
     *
     * @return trustStoreHolder instance
     */
    public static TrustStoreHolder getInstance() {
        return trustStoreHolderInstance;
    }

    /**
     * Get the public key for a given key alias from trustStore.
     *
     * @param certificateAlia    the alias name
     * @param trustStoreFilePath Trust store file path
     * @param trustStorePassword Trust store password
     * @return public key corresponding to the alias.
     * @throws BallerinaException if the trustStore has not been initialized
     *                           (loaded), or this operation fails for some other reason
     */
    public PublicKey getTrustedPublicKey(String certificateAlia, String trustStoreFilePath, char[]
            trustStorePassword) throws BallerinaException {
        Certificate certificate = getTrustedCertificate(certificateAlia, trustStoreFilePath, trustStorePassword);
        if (certificate == null) {
            throw new BallerinaException("Failed to load trusted key: " + certificateAlia);
        }
        return certificate.getPublicKey();
    }

    /**
     * Get the certificate for a given key alias from trustStore.
     *
     * @param certificateAlia    the alias name
     * @param trustStoreFilePath Trust store file path
     * @param trustStorePassword Trust store password
     * @throws BallerinaException if the trustStore has not been initialized
     *                           (loaded), or this operation fails for some other reason
     */
    public Certificate getTrustedCertificate(String certificateAlia, String trustStoreFilePath, char[]
            trustStorePassword) throws BallerinaException {
        try {
            return getTrustStore(trustStoreFilePath, trustStorePassword).getCertificate(certificateAlia);
        } catch (java.security.KeyStoreException e) {
            throw new BallerinaException("Failed to load certificate: " + certificateAlia, e);
        }
    }

    private KeyStore getTrustStore(String trustStoreFilePath, char[] trustStorePassword) {
        KeyStore trustStore = trustStoreMap.get(trustStoreFilePath);
        if (trustStore == null) {
            KeyStore newTrustStore = loadTrustStore(trustStoreFilePath, trustStorePassword);
            trustStoreMap.put(trustStoreFilePath, newTrustStore);
            trustStore = trustStoreMap.get(trustStoreFilePath);
        }
        return trustStore;
    }

    private KeyStore loadTrustStore(String trustStoreFilePath, char[] trustStorePassword) {
        try (InputStream file = new FileInputStream(new File(trustStoreFilePath))) {
            KeyStore trustStore = KeyStore.getInstance(PKCS12);
            trustStore.load(file, trustStorePassword);
            return trustStore;
        } catch (FileNotFoundException e) {
            throw new BallerinaException("Failed to load trustStore: file not found: " + trustStoreFilePath, e);
        } catch (Exception e) {
            throw new BallerinaException("Failed to load trustStore: " + e.getMessage(), e);
        }
    }
}
