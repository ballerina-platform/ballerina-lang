/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.transport.http.netty.common.ssl;

import io.netty.handler.ssl.SslHandler;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * A class that encapsulates SSL Certificate Information.
 */
public class SSLHandlerFactory {

    private static final String protocol = "TLS";
    private final SSLContext serverContext;
    private SSLConfig sslConfig;
    private boolean needClientAuth;


    public SSLHandlerFactory(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        try {
            KeyStore ks = getKeyStore(sslConfig.getKeyStore(), sslConfig.getKeyStorePass());
            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            KeyManager[] keyManagers = null;
            if (ks != null) {
                kmf.init(ks, sslConfig.getCertPass() != null ?
                        sslConfig.getCertPass().toCharArray() :
                        sslConfig.getKeyStorePass().toCharArray());
                keyManagers = kmf.getKeyManagers();
            }
            TrustManager[] trustManagers = null;
            if (sslConfig.getTrustStore() != null) {
                this.needClientAuth = true;
                KeyStore tks = getKeyStore(sslConfig.getTrustStore(), sslConfig.getTrustStorePass());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
                tmf.init(tks);
                trustManagers = tmf.getTrustManagers();
            }
            serverContext = SSLContext.getInstance(protocol);
            serverContext.init(keyManagers, trustManagers, null);

        } catch (UnrecoverableKeyException | KeyManagementException |
                NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new IllegalArgumentException("Failed to initialize the server-side SSLContext", e);
        }
    }

    private static KeyStore getKeyStore(File keyStore, String keyStorePassword) throws IOException {
        KeyStore ks = null;
        if (keyStore != null && keyStorePassword != null) {
            try (InputStream is = new FileInputStream(keyStore)) {
                ks = KeyStore.getInstance("JKS");
                ks.load(is, keyStorePassword.toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IOException(e);
            }
        }
        return ks;
    }

    /**
     * @return instance of {@code SslHandler}
     */
    public SslHandler create() {
        SSLEngine engine = serverContext.createSSLEngine();
        engine.setNeedClientAuth(needClientAuth);
        engine.setUseClientMode(sslConfig.isClientMode());
        if (sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0) {
            engine.setEnabledCipherSuites(sslConfig.getCipherSuites());
        }
        if (sslConfig.getEnableProtocols() != null && sslConfig.getEnableProtocols().length > 0) {
            engine.setEnabledCipherSuites(sslConfig.getEnableProtocols());

        }
        if (sslConfig.isEnableSessionCreation()) {
            engine.setEnableSessionCreation(true);

        }
        if (sslConfig.getServerNames() != null && sslConfig.getServerNames().length > 0) {
            SSLParameters sslParameters = engine.getSSLParameters();
            sslParameters.setServerNames(new ArrayList(Arrays.asList(sslConfig.getServerNames())));
        }
        if (sslConfig.getSniMatchers() != null && sslConfig.getSniMatchers().length > 0) {
            SSLParameters sslParameters = engine.getSSLParameters();
            sslParameters.setServerNames(new ArrayList(Arrays.asList(sslConfig.getSniMatchers())));
        }
        return new SslHandler(engine);
    }
}
