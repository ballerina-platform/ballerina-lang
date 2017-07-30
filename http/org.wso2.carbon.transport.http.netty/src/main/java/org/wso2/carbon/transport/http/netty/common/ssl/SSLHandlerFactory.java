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

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;

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
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
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
    private KeyManagerFactory kmf;
    private TrustManagerFactory tmf;


    public SSLHandlerFactory(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        try {
            KeyStore ks = getKeyStore(sslConfig.getKeyStore(), sslConfig.getKeyStorePass());
            // Set up key manager factory to use our key store
            kmf = KeyManagerFactory.getInstance(algorithm);
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
                tmf = TrustManagerFactory.getInstance(algorithm);
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
    public SSLEngine build() {
        SSLEngine engine = serverContext.createSSLEngine();
        engine.setNeedClientAuth(needClientAuth);
        engine.setUseClientMode(sslConfig.isClientMode());
        if (sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0) {
            engine.setEnabledCipherSuites(sslConfig.getCipherSuites());
        }
        if (sslConfig.getEnableProtocols() != null && sslConfig.getEnableProtocols().length > 0) {
            engine.setEnabledProtocols(sslConfig.getEnableProtocols());
        }
        if (sslConfig.isEnableSessionCreation()) {
            engine.setEnableSessionCreation(true);
        }
        return engine;
    }

    /**
     * This method will provide netty ssl context which supports HTTP2 over TLS using
     * Application Layer Protocol Negotiation (ALPN)
     *
     * @return instance of {@link SslContext}
     * @throws SSLException
     */
    public SslContext createHttp2TLSContext() throws SSLException {

        // If listener configuration does not include cipher suites , default ciphers required by the HTTP/2
        // specification will be added.
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ? Arrays
                .asList(sslConfig.getCipherSuites()) : Http2SecurityUtil.CIPHERS;
        SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
        return SslContextBuilder.forServer(this.getKeyManagerFactory())
                .trustManager(this.getTrustStoreFactory())
                .sslProvider(provider)
                .ciphers(ciphers,
                        SupportedCipherSuiteFilter.INSTANCE)
                .clientAuth(needClientAuth ? ClientAuth.REQUIRE : ClientAuth.NONE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1)).build();
    }

    public KeyManagerFactory getKeyManagerFactory() {
        return kmf;
    }

    public TrustManagerFactory getTrustStoreFactory() {
        return tmf;
    }

    public void setSNIServerNames(SSLEngine sslEngine, String peerHost) {
        SSLParameters sslParameters = new SSLParameters();
        List<SNIServerName> serverNames = new ArrayList<>();
        serverNames.add(new SNIHostName(peerHost));
        sslParameters.setServerNames(serverNames);
        sslEngine.setSSLParameters(sslParameters);
    }
}
