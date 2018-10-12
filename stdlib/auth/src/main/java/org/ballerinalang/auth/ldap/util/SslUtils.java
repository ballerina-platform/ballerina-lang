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
package org.ballerinalang.auth.ldap.util;

import org.ballerinalang.auth.ldap.LDAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Utility class for handle ssl related LDAP operations.
 */
public class SslUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SslUtils.class.getSimpleName());

    public static SSLContext getSslContextForCertificateFile(String fileName) throws NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, CertificateException {
        KeyStore keyStore = SslUtils.getKeyStore(fileName);
        SSLContext sslContext = SSLContext.getInstance(LDAPConstants.TLS);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    private static KeyStore getKeyStore(String fileName) throws IOException, CertificateException,
            KeyStoreException, NoSuchAlgorithmException {
        KeyStore keyStore;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(LDAPUtils.substituteVariables(fileName));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca = cf.generateCertificate(inputStream);
            keyStore = KeyStore.getInstance(LDAPConstants.PKCS_STORE_TYPE);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LOG.debug("ca={}", e);
            }
        }
        return keyStore;
    }

    public static KeyStore getKeyStore(File keyStore, String keyStorePassword) throws IOException {
        KeyStore ks = null;
        if (keyStore != null && keyStorePassword != null) {
            try (InputStream is = new FileInputStream(keyStore)) {
                ks = KeyStore.getInstance(LDAPConstants.PKCS_STORE_TYPE);
                ks.load(is, keyStorePassword.toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IOException(e);
            }
        }
        return ks;
    }

    public static SSLContext createClientSslContext(String trustStoreFilePath, String trustStorePassword) throws
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {

        TrustManager[] trustManagers = null;
        File trustStoreFile = new File(LDAPUtils.substituteVariables(trustStoreFilePath));
        KeyStore tks = getKeyStore(trustStoreFile, trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(tks);
        trustManagers = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance(LDAPConstants.TLS);
        sslContext.init(null, trustManagers, null);
        return sslContext;
    }

    public static SSLContext getSslContext() throws NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, IOException, CertificateException {
        String trustStoreFilePath = System.getProperty(LDAPConstants.LDAP_TRUST_STORE_FILE_PATH);
        String trustStorePassword = System.getProperty(LDAPConstants.LDAP_TRUST_STORE_PASSWORD);
        String trustedCertFile = System.getProperty(LDAPConstants.LDAP_TRUST_STORE_TRUST_CERTIFICATES);
        if (!LDAPUtils.isNullOrEmptyAfterTrim(trustStoreFilePath) && !LDAPUtils.isNullOrEmptyAfterTrim
                (trustStorePassword)) {
            return SslUtils.createClientSslContext(trustStoreFilePath, trustStorePassword);
        } else if (!LDAPUtils.isNullOrEmptyAfterTrim(trustedCertFile)) {
            return SslUtils.getSslContextForCertificateFile(trustedCertFile);
        }
        return SSLContext.getDefault();
    }
}
