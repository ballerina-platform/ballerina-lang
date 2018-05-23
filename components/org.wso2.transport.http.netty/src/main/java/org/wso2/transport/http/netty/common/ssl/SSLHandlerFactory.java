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
package org.wso2.transport.http.netty.common.ssl;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.wso2.transport.http.netty.common.Constants;

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

    private final SSLContext sslContext;
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
        needClientAuth = sslConfig.isNeedClientAuth();
        String protocol = sslConfig.getSSLProtocol();
        try {
            KeyManager[] keyManagers = null;
            if (sslConfig.getKeyStore() != null) {
                KeyStore ks = getKeyStore(sslConfig.getKeyStore(), sslConfig.getKeyStorePass());
                // Set up key manager factory to use our key store
                kmf = KeyManagerFactory.getInstance(algorithm);
                if (ks != null) {
                    kmf.init(ks, sslConfig.getCertPass() != null ?
                            sslConfig.getCertPass().toCharArray() :
                            sslConfig.getKeyStorePass().toCharArray());
                    keyManagers = kmf.getKeyManagers();
                }
            }
            TrustManager[] trustManagers = null;
            if (sslConfig.getTrustStore() != null) {
                KeyStore tks = getKeyStore(sslConfig.getTrustStore(), sslConfig.getTrustStorePass());
                tmf = TrustManagerFactory.getInstance(algorithm);
                tmf.init(tks);
                trustManagers = tmf.getTrustManagers();
            }
            sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManagers, trustManagers, null);

        } catch (UnrecoverableKeyException | KeyManagementException |
                NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new IllegalArgumentException("Failed to initialize the SSLContext", e);
        }
    }

    private KeyStore getKeyStore(File keyStore, String keyStorePassword) throws IOException {
        KeyStore ks = null;
        String  tlsStoreType = sslConfig.getTLSStoreType();
        if (keyStore != null && keyStorePassword != null) {
            try (InputStream is = new FileInputStream(keyStore)) {
                ks = KeyStore.getInstance(tlsStoreType);
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
    public SSLEngine buildServerSSLEngine() {
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(needClientAuth);
        return addCommonConfigs(engine);
    }

    public ReferenceCountedOpenSslContext getServerReferenceCountedOpenSslContext(boolean enableOcsp)
            throws SSLException {
        ReferenceCountedOpenSslContext context = (ReferenceCountedOpenSslContext) SslContextBuilder
                .forServer(kmf).sslProvider(SslProvider.OPENSSL).enableOcsp(true)
                .keyManager(kmf).trustManager(tmf).protocols(sslConfig.getEnableProtocols()).enableOcsp(enableOcsp)
                .clientAuth(needClientAuth ? ClientAuth.REQUIRE : ClientAuth.NONE).build();
        return context;
    }

    /**
     * Build ssl engine for client side.
     *
     * @param host peer host
     * @param port peer port
     * @return client ssl engine
     */
    public SSLEngine buildClientSSLEngine(String host, int port) {
        SSLEngine engine = sslContext.createSSLEngine(host, port);
        engine.setUseClientMode(true);
        return addCommonConfigs(engine);
    }

    /**
     * Add common configs for both client and server ssl engines.
     *
     * @param engine client/server ssl engine.
     * @return sslEngine
     */
    private SSLEngine addCommonConfigs(SSLEngine engine) {
        if (sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0) {
            engine.setEnabledCipherSuites(sslConfig.getCipherSuites());
        }
        if (sslConfig.getEnableProtocols() != null && sslConfig.getEnableProtocols().length > 0) {
            engine.setEnabledProtocols(sslConfig.getEnableProtocols());
        }
        engine.setEnableSessionCreation(sslConfig.isEnableSessionCreation());
        return engine;
    }

    public ReferenceCountedOpenSslContext buildClientReferenceCountedOpenSslContext() throws SSLException {
        ReferenceCountedOpenSslContext context = (ReferenceCountedOpenSslContext) SslContextBuilder.forClient()
                .sslProvider(SslProvider.OPENSSL).enableOcsp(true).keyManager(kmf)
                .trustManager(tmf).protocols(sslConfig.getEnableProtocols()).build();
        return context;
    }

    /**
     * This method will provide netty ssl context which supports HTTP2 over TLS using
     * Application Layer Protocol Negotiation (ALPN)
     *
     * @param enableOcsp whether ocsp is enabled or not (true/false)
     * @return instance of {@link SslContext}
     * @throws SSLException if any error occurred during building SSL context.
     */
    public SslContext createHttp2TLSContextForServer(boolean enableOcsp) throws SSLException {

        // If listener configuration does not include cipher suites , default ciphers required by the HTTP/2
        // specification will be added.
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ?
                Arrays.asList(sslConfig.getCipherSuites()) :
                Http2SecurityUtil.CIPHERS;
        SslProvider provider = SslProvider.OPENSSL;

        return SslContextBuilder.forServer(this.getKeyManagerFactory()).trustManager(this.getTrustStoreFactory())
                .sslProvider(provider).ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE)
                .clientAuth(needClientAuth ? ClientAuth.REQUIRE : ClientAuth.NONE).applicationProtocolConfig(
                        new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1))
                .enableOcsp(enableOcsp).build();
    }

    public SslContext createHttp2TLSContextForClient(boolean enableOcsp) throws SSLException {

        // If sender configuration does not include cipher suites , default ciphers required by the HTTP/2
        // specification will be added.
        SslProvider provider = SslProvider.OPENSSL;
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ?
                Arrays.asList(sslConfig.getCipherSuites()) :
                Http2SecurityUtil.CIPHERS;

        return SslContextBuilder.forClient().sslProvider(provider).keyManager(kmf).trustManager(tmf)
                .protocols(sslConfig.getEnableProtocols()).ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                        // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1)).enableOcsp(enableOcsp).
                        build();
    }

    private KeyManagerFactory getKeyManagerFactory() {
        return kmf;
    }

    private TrustManagerFactory getTrustStoreFactory() {
        return tmf;
    }

    public void setSNIServerNames(SSLEngine sslEngine, String peerHost) {
        SSLParameters sslParameters = new SSLParameters();
        List<SNIServerName> serverNames = new ArrayList<>();
        serverNames.add(new SNIHostName(peerHost));
        sslParameters.setServerNames(serverNames);
        sslEngine.setSSLParameters(sslParameters);
    }

    public void setHostNameVerfication(SSLEngine sslEngine) {
        SSLParameters sslParams = sslEngine.getSSLParameters();
        sslParams.setEndpointIdentificationAlgorithm(Constants.HTTPS_SCHEME);
        sslEngine.setSSLParameters(sslParams);
    }
}
