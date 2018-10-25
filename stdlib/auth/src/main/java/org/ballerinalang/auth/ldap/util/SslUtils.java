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

import org.ballerinalang.auth.ldap.LdapConstants;
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
 *
 * @since 0.983.0
 */
public class SslUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SslUtils.class.getSimpleName());

    /**
     * Creates an SSLContext based on provided trust certificate chain file path.
     *
     * @param filePath Path to the certificate file.
     * @return SSLContext created from the given certificate file.
     * @throws NoSuchAlgorithmException When the particular cryptographic algorithm is not available in the environment.
     * @throws KeyStoreException When an exception occurs during the keystore creation process.
     * @throws KeyManagementException  When an exception occurs dealing with key management.
     * @throws IOException To signal that an I/O exception of some sort has occurred.
     * @throws CertificateException To indicate one of a variety of certificate problems.
     */
    public static SSLContext getSslContextForCertificateFile(String filePath) throws NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, IOException, CertificateException {
        KeyStore keyStore = SslUtils.getKeyStore(filePath);
        SSLContext sslContext = SSLContext.getInstance(LdapConstants.TLS);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    /**
     * Creates an SSLContext based on provided trust store file path and the password.
     *
     * @param trustStoreFilePath Path to the trust store file.
     * @param trustStorePassword Trust store password.
     * @return SSLContext created from the given trust store and password.
     * @throws NoSuchAlgorithmException When the particular cryptographic algorithm is not available in the environment.
     * @throws KeyStoreException When an exception occurs during the keystore creation process.
     * @throws KeyManagementException  When an exception occurs dealing with key management.
     * @throws IOException To signal that an I/O exception of some sort has occurred.
     */
    public static SSLContext createClientSslContext(String trustStoreFilePath, String trustStorePassword) throws
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        TrustManager[] trustManagers;
        File trustStoreFile = new File(LdapUtils.substituteVariables(trustStoreFilePath));
        KeyStore tks = getKeyStore(trustStoreFile, trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(tks);
        trustManagers = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance(LdapConstants.TLS);
        sslContext.init(null, trustManagers, null);
        return sslContext;
    }

    private static KeyStore getKeyStore(String fileName) throws IOException, CertificateException,
            KeyStoreException, NoSuchAlgorithmException {
        KeyStore keyStore;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(LdapUtils.substituteVariables(fileName));
            CertificateFactory cf = CertificateFactory.getInstance(LdapConstants.X_509);
            Certificate ca = cf.generateCertificate(inputStream);
            keyStore = KeyStore.getInstance(LdapConstants.PKCS_STORE_TYPE);
            keyStore.load(null, null);
            keyStore.setCertificateEntry(LdapConstants.CERTIFICATE_ALIAS, ca);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return keyStore;
    }

    private static KeyStore getKeyStore(File keyStore, String keyStorePassword) throws IOException {
        KeyStore ks = null;
        if (keyStore != null && keyStorePassword != null) {
            try (InputStream is = new FileInputStream(keyStore)) {
                ks = KeyStore.getInstance(LdapConstants.PKCS_STORE_TYPE);
                ks.load(is, keyStorePassword.toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IOException(e);
            }
        }
        return ks;
    }
}
