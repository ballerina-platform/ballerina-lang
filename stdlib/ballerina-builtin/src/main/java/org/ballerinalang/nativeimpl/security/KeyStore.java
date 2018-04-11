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

package org.ballerinalang.nativeimpl.security;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;

/**
 * KeyStore process the configured keystore and trust store and provide an API.
 * PKCS12 is the default keystore format supported.
 *
 * @since 0.964.0
 */
public class KeyStore {

    private static final Logger log = LoggerFactory.getLogger(KeyStore.class);

    // KeyStore configurations
    private static final String KEY_STORE_CONFIG = "keyStore";
    private static final String KEY_STORE_LOCATION = "location";
    private static final String KEY_STORE_TYPE = "type";
    private static final String KEY_STORE_PASSWORD = "keyStorePassword";
    private static final String KEY_ALIAS = "keyAlias";
    private static final String KEY_PASSWORD = "keyPassword";
    // TrustStore configurations
    private static final String TRUST_STORE_CONFIG = "trustStore";
    private static final String TRUST_STORE_LOCATION = "location";
    private static final String TRUST_STORE_TYPE = "type";
    private static final String TRUST_STORE_PASSWORD = "trustStorePassword";

    private static final KeyStore keyStoreInstance = new KeyStore();
    private java.security.KeyStore keyStore;
    private java.security.KeyStore trustStore;

    private KeyStore() {
        loadKeyStore();
        loadTrustStore();
    }

    /**
     * Get the KeyStore.
     *
     * @return KeyStore instance
     */
    public static KeyStore getKeyStore() {
        return keyStoreInstance;
    }

    /**
     * Get the public key for a given key alias from trustStore.
     *
     * @param alias the alias name
     * @return public key corresponding to the alias.
     * @throws KeyStoreException if the truststore has not been initialized
     *                           (loaded), or this operation fails for some other reason
     */
    public PublicKey getTrustedPublicKey(String alias) throws KeyStoreException {
        Certificate certificate = getTrustedCertificate(alias);
        if (certificate == null) {
            return null;
        }
        return certificate.getPublicKey();
    }

    /**
     * Get the certificate for a given key alias from trustStore.
     *
     * @param alias the alias name
     * @return certificate corresponding to the alias
     * @throws KeyStoreException if the truststore has not been initialized
     *                           (loaded), or this operation fails for some other reason
     */
    public Certificate getTrustedCertificate(String alias) throws KeyStoreException {
        try {
            return trustStore.getCertificate(alias);
        } catch (java.security.KeyStoreException e) {
            throw new KeyStoreException("Failed to load certificate: " + alias, e);
        }
    }

    /**
     * Get the private key for a given key alias.
     *
     * @param alias       the alias name
     * @param keyPassword the password to protect the key
     * @return private key corresponding to the alias
     * @throws KeyStoreException if the keystore has not been initialized
     *                           (loaded), if the algorithm for recovering the entry cannot be found,
     *                           {@code alias} or {@code keyPassword} does not contain
     *                           the information needed to recover the key (e.g. wrong password)
     */
    public PrivateKey getPrivateKey(String alias, char[] keyPassword) throws KeyStoreException {
        PrivateKeyEntry pkEntry;
        try {
            pkEntry = (PrivateKeyEntry) keyStore.getEntry(alias, new PasswordProtection(keyPassword));
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | java.security.KeyStoreException e) {
            throw new KeyStoreException("Failed to load private key: " + alias, e);
        }
        if (pkEntry == null) {
            return null;
        }
        return pkEntry.getPrivateKey();
    }

    /**
     * Get the public key for a given key alias from keyStore.
     *
     * @param alias the alias name
     * @return public key corresponding to the alias
     * @throws KeyStoreException if the keystore has not been initialized
     *                           (loaded), or this operation fails for some other reason
     */
    public PublicKey getPublicKey(String alias) throws KeyStoreException {
        Certificate certificate = getCertificate(alias);
        if (certificate == null) {
            return null;
        }
        return certificate.getPublicKey();
    }

    /**
     * Get the certificate for a given key alias from keyStore.
     *
     * @param alias the alias name
     * @return certificate corresponding to the alias
     * @throws KeyStoreException if the keystore has not been initialized
     *                           (loaded), or this operation fails for some other reason
     */
    public Certificate getCertificate(String alias) throws KeyStoreException {
        try {
            return keyStore.getCertificate(alias);
        } catch (java.security.KeyStoreException e) {
            throw new KeyStoreException("Failed to load certificate: " + alias, e);
        }
    }

    /**
     * Get the default private key of the service.
     *
     * @return default private key
     * @throws KeyStoreException if the keystore has not been initialized
     *                           (loaded), if the algorithm for recovering the entry cannot be found,
     *                           Keystore configuration does not contain
     *                           the information needed to recover the key (e.g. wrong password)
     */
    public PrivateKey getDefaultPrivateKey() throws KeyStoreException {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        char[] keyStorePassword = configRegistry.getConfigAsCharArray(KEY_STORE_CONFIG, KEY_PASSWORD);
        String keyAlias = configRegistry.getAsString(KEY_STORE_CONFIG, KEY_ALIAS);
        return getPrivateKey(keyAlias, keyStorePassword);
    }

    /**
     * Get the default public key for the service.
     *
     * @return default public key
     * @throws KeyStoreException if the keystore has not been initialized
     *                           (loaded), or this operation fails for some other reason
     */
    public PublicKey getDefaultPublicKey() throws KeyStoreException {
        String keyAlias = ConfigRegistry.getInstance().getAsString(KEY_STORE_CONFIG, KEY_ALIAS);
        return getPublicKey(keyAlias);
    }

    /**
     * Get the default certificate for the service.
     *
     * @return certificate corresponding to the alias
     * @throws KeyStoreException if the keystore has not been initialized
     *                           (loaded), or this operation fails for some other reason
     */
    public Certificate getDefaultCertificate() throws KeyStoreException {
        String keyAlias = ConfigRegistry.getInstance().getAsString(KEY_STORE_CONFIG, KEY_ALIAS);
        return getCertificate(keyAlias);
    }

    private void loadKeyStore() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        String keyStoreLocation = configRegistry.getAsString(KEY_STORE_CONFIG, KEY_STORE_LOCATION);
        if (keyStoreLocation != null) {
            char[] keyStorePassword = configRegistry.getConfigAsCharArray(
                    KEY_STORE_CONFIG, KEY_STORE_PASSWORD);
            String keystoreType = configRegistry.getAsString(KEY_STORE_CONFIG, KEY_STORE_TYPE);
            try (InputStream file = new FileInputStream(new File(keyStoreLocation))) {
                keyStore = java.security.KeyStore.getInstance(keystoreType);
                keyStore.load(file, keyStorePassword);
            } catch (FileNotFoundException e) {
                throw new BallerinaException("Failed to load keystore: file not found: " + keyStoreLocation, e);
            } catch (Exception e) {
                throw new BallerinaException("Failed to load keystore: " + e.getMessage(), e);
            }
        } else {
            log.warn("KeyStore is not configured");
            try {
                keyStore = java.security.KeyStore.getInstance("pkcs12");
            } catch (java.security.KeyStoreException e) {
                throw new BallerinaException("Failed to instantiate keystore: ", e);
            }
        }
    }

    private void loadTrustStore() {
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        String trustStoreLocation = configRegistry.getAsString(TRUST_STORE_CONFIG, TRUST_STORE_LOCATION);
        if (trustStoreLocation != null) {
            char[] trustStorePassword = configRegistry.getConfigAsCharArray(TRUST_STORE_CONFIG, TRUST_STORE_PASSWORD);
            String trustStoreType = configRegistry.getAsString(TRUST_STORE_CONFIG, TRUST_STORE_TYPE);
            try (InputStream file = new FileInputStream(new File(trustStoreLocation))) {
                trustStore = java.security.KeyStore.getInstance(trustStoreType);
                trustStore.load(file, trustStorePassword);
            } catch (FileNotFoundException e) {
                throw new BallerinaException("Failed to load trustStore: file not found: " + trustStoreLocation, e);
            } catch (Exception e) {
                throw new BallerinaException("Failed to load trustStore: " + e.getMessage(), e);
            }
        } else {
            log.warn("TrustStore is not configured");
            try {
                trustStore = java.security.KeyStore.getInstance("pkcs12");
            } catch (java.security.KeyStoreException e) {
                throw new BallerinaException("Failed to instantiate trustStore: ", e);
            }
        }
    }
}
