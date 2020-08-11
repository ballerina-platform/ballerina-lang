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
package org.wso2.transport.http.netty.contractimpl.common.ssl;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.wso2.transport.http.netty.contract.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

    private SSLContext sslContext = null;
    private SSLConfig sslConfig;
    private boolean needClientAuth;
    private boolean wantClientAuth;
    private KeyManagerFactory kmf;
    private TrustManagerFactory tmf;
    private SslContextBuilder sslContextBuilder;

    public SSLHandlerFactory(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
        needClientAuth = sslConfig.isNeedClientAuth();
        wantClientAuth = sslConfig.isWantClientAuth();
    }

    /**
     * This is uset to create the sslContext from keystores.
     *
     * @param isServer identifies whether the server or the client has called this method.
     * @return sslContext represents a secure socket protocol implementation
     */
    public SSLContext createSSLContextFromKeystores(boolean isServer) {
        String protocol = sslConfig.getSSLProtocol();
        try {
            if (sslConfig.useJavaDefaults() && !isServer) {
                return SSLContext.getDefault();
            }
            KeyManager[] keyManagers = null;
            if (sslConfig.getKeyStore() != null) {
                KeyStore ks = getKeyStore(sslConfig.getKeyStore(), sslConfig.getKeyStorePass());
                // Set up key manager factory to use our key store
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
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
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(tks);
                trustManagers = tmf.getTrustManagers();
            }
            sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManagers, trustManagers, null);
            int sessionTimeout = sslConfig.getSessionTimeOut();
            if (isServer) {
                if (sessionTimeout > 0) {
                    sslContext.getServerSessionContext().setSessionTimeout(sessionTimeout);
                }
            } else {
                if (sessionTimeout > 0) {
                    sslContext.getClientSessionContext().setSessionTimeout(sessionTimeout);
                }
            }
            return sslContext;

        } catch (UnrecoverableKeyException | KeyManagementException |
                NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new IllegalArgumentException("Failed to initialize the SSLContext: " + e.getMessage());
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
     * Build the server ssl engine using the ssl context.
     *
     * @param sslContext sslContext.
     * @return SSLEngine.
     */
    public SSLEngine buildServerSSLEngine(SSLContext sslContext) {
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        if (needClientAuth) {
            engine.setNeedClientAuth(true);
        } else if (wantClientAuth) {
            engine.setWantClientAuth(true);
        }
        return addCommonConfigs(engine);
    }

    /**
     * This used to create the open ssl context when ocsp stapling is enabled for server.
     *
     * @param enableOcsp true/false for enabling ocsp stapling.
     * @return ReferenceCountedOpenSslContext.
     * @throws SSLException if any error occurs while creating the ReferenceCountedOpenSslContext.
     */
    public ReferenceCountedOpenSslContext getServerReferenceCountedOpenSslContext(boolean enableOcsp)
            throws SSLException {
        if (sslConfig.getKeyStore() != null) {
            sslContextBuilder = serverContextBuilderWithKs(SslProvider.OPENSSL);
        } else {
            sslContextBuilder = serverContextBuilderWithCerts(SslProvider.OPENSSL);
        }
        setOcspStapling(sslContextBuilder, enableOcsp);
        if (sslConfig.getCipherSuites() != null) {
            List<String> ciphers = Arrays.asList(sslConfig.getCipherSuites());
            setCiphers(sslContextBuilder, ciphers);
        }
        setSslProtocol(sslContextBuilder);
        ReferenceCountedOpenSslContext referenceCountedOpenSslCtx = (ReferenceCountedOpenSslContext) sslContextBuilder
                .build();
        int sessionTimeout = sslConfig.getSessionTimeOut();
        if (sessionTimeout > 0) {
            referenceCountedOpenSslCtx.sessionContext().setSessionTimeout(sessionTimeout);
        }
        return referenceCountedOpenSslCtx;
    }

    /**
     * This used to create the open ssl context when ocsp stapling is enabled for client.
     *
     * @return ReferenceCountedOpenSslContext.
     * @throws SSLException if any error occurs while creating the ReferenceCountedOpenSslContext.
     */
    public ReferenceCountedOpenSslContext buildClientReferenceCountedOpenSslContext() throws SSLException {
        if (sslConfig.getTrustStore() != null) {
            sslContextBuilder = clientContextBuilderWithKs(SslProvider.OPENSSL);
        } else {
            sslContextBuilder = clientContextBuilderWithCerts(SslProvider.OPENSSL);
        }
        setOcspStapling(sslContextBuilder, true);
        if (sslConfig.getCipherSuites() != null) {
            List<String> ciphers = Arrays.asList(sslConfig.getCipherSuites());
            setCiphers(sslContextBuilder, ciphers);
        }
        setSslProtocol(sslContextBuilder);
        ReferenceCountedOpenSslContext referenceCountedOpenSslCtx = (ReferenceCountedOpenSslContext) sslContextBuilder
                .build();
        int sessionTimeout = sslConfig.getSessionTimeOut();
        if (sessionTimeout > 0) {
            referenceCountedOpenSslCtx.sessionContext().setSessionTimeout(sessionTimeout);
        }
        return referenceCountedOpenSslCtx;
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
    public SSLEngine addCommonConfigs(SSLEngine engine) {
        if (sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0) {
            engine.setEnabledCipherSuites(sslConfig.getCipherSuites());
        }
        if (sslConfig.getEnableProtocols() != null && sslConfig.getEnableProtocols().length > 0) {
            engine.setEnabledProtocols(sslConfig.getEnableProtocols());
        }
        engine.setEnableSessionCreation(sslConfig.isEnableSessionCreation());
        return engine;
    }

    /**
     * This method will provide netty ssl context which supports HTTP2 over TLS using
     * Application Layer Protocol Negotiation (ALPN)
     *
     * @param sslConfig ssl configurations
     * @return instance of {@link SslContext}
     * @throws SSLException if any error occurred during building SSL context.
     */
    public SslContext createHttp2TLSContextForServer(SSLConfig sslConfig) throws SSLException {

        // If listener configuration does not include cipher suites , default ciphers required by the HTTP/2
        // specification will be added.
        createSSLContextFromKeystores(true);
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ?
                Arrays.asList(sslConfig.getCipherSuites()) :
                Http2SecurityUtil.CIPHERS;
        SslProvider provider = SslProvider.OPENSSL;
        if (sslConfig.getKeyStore() != null) {
            sslContextBuilder = serverContextBuilderWithKs(provider);
        } else {
            sslContextBuilder = serverContextBuilderWithCerts(provider);
        }
        setCiphers(sslContextBuilder, ciphers);
        setSslProtocol(sslContextBuilder);
        setAlpnConfigs(sslContextBuilder);
        setOcspStapling(sslContextBuilder, sslConfig.isOcspStaplingEnabled());

        SslContext sslCtx = sslContextBuilder.build();
        int sessionTimeout = sslConfig.getSessionTimeOut();
        if (sessionTimeout > 0) {
            sslCtx.sessionContext().setSessionTimeout(sessionTimeout);
        }

        return sslCtx;
    }

    /**
     * This method will provide netty ssl context which supports HTTP over TLS using.
     *
     * @return instance of {@link SslContext}
     * @throws SSLException if any error occurred during building SSL context.
     */
    public SslContext createHttpTLSContextForServer() throws SSLException {
        SslProvider provider = SslProvider.JDK;
        SslContext certsSslContext = serverContextBuilderWithCerts(provider).build();
        int sessionTimeout = sslConfig.getSessionTimeOut();
        if (sessionTimeout > 0) {
            certsSslContext.sessionContext().setSessionTimeout(sessionTimeout);
        }
        return certsSslContext;
    }

    /**
     * This method will provide netty ssl context which supports HTTP over TLS using certificates and keys.
     *
     * @return instance of {@link SslContext}
     * @throws SSLException if any error occurred during building SSL context.
     */
    public SslContext createHttpTLSContextForClient() throws SSLException {
        SslProvider provider = SslProvider.JDK;
        SslContext certsSslContext = clientContextBuilderWithCerts(provider).build();
        int sessionTimeout = sslConfig.getSessionTimeOut();
        if (sessionTimeout > 0) {
            certsSslContext.sessionContext().setSessionTimeout(sessionTimeout);
        }
        return certsSslContext;
    }

    private SslContextBuilder serverContextBuilderWithKs(SslProvider sslProvider) {
        SslContextBuilder serverSslContextBuilder = SslContextBuilder.forServer(this.getKeyManagerFactory())
                .trustManager(this.getTrustStoreFactory())
                .sslProvider(sslProvider);
        setClientAuth(serverSslContextBuilder);
        return serverSslContextBuilder;
    }

    private SslContextBuilder clientContextBuilderWithKs(SslProvider sslProvider) {
        return SslContextBuilder.forClient().sslProvider(sslProvider).keyManager(kmf).trustManager(tmf);
    }

    private SslContextBuilder serverContextBuilderWithCerts(SslProvider sslProvider) {
        String keyPassword = sslConfig.getServerKeyPassword();
        SslContextBuilder serverSslContextBuilder = SslContextBuilder
                .forServer(sslConfig.getServerCertificates(), sslConfig.getServerKeyFile())
                .keyManager(sslConfig.getServerCertificates(), sslConfig.getServerKeyFile(), keyPassword)
                .trustManager(sslConfig.getServerTrustCertificates()).sslProvider(sslProvider);
        setClientAuth(serverSslContextBuilder);
        return serverSslContextBuilder;
    }

    private SslContextBuilder clientContextBuilderWithCerts(SslProvider sslProvider) {
        String keyPassword = sslConfig.getClientKeyPassword();
        return SslContextBuilder.forClient().sslProvider(sslProvider)
                .keyManager(sslConfig.getClientCertificates(), sslConfig.getClientKeyFile(), keyPassword)
                .trustManager(sslConfig.getClientTrustCertificates());
    }

    public SslContext createHttp2TLSContextForClient(boolean enableOcsp) throws SSLException {

        // If sender configuration does not include cipher suites , default ciphers required by the HTTP/2
        // specification will be added.
        SslProvider provider = SslProvider.OPENSSL;
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ?
                Arrays.asList(sslConfig.getCipherSuites()) :
                Http2SecurityUtil.CIPHERS;
        if (sslConfig.getTrustStore() != null) {
            sslContextBuilder = clientContextBuilderWithKs(provider);
        } else {
            sslContextBuilder = clientContextBuilderWithCerts(provider);
        }
        setCiphers(sslContextBuilder, ciphers);
        setSslProtocol(sslContextBuilder);
        setAlpnConfigs(sslContextBuilder);
        setOcspStapling(sslContextBuilder, enableOcsp);
        SslContext sslContext = sslContextBuilder.build();
        int sessionTimeout = sslConfig.getSessionTimeOut();
        if (sessionTimeout > 0) {
            sslContext.sessionContext().setSessionTimeout(sessionTimeout);
        }
        return sslContextBuilder.build();
    }

    private void setCiphers(SslContextBuilder sslContextBuilder, List<String> ciphers) {
        sslContextBuilder.ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE);
    }

    private void setSslProtocol(SslContextBuilder sslContextBuilder) {
        sslContextBuilder.protocols(sslConfig.getEnableProtocols());
    }

    private void setAlpnConfigs(SslContextBuilder sslContextBuilder) {
        sslContextBuilder.applicationProtocolConfig(
                new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1));
    }

    private void setOcspStapling(SslContextBuilder sslContextBuilder, boolean enableOcsp) {
        sslContextBuilder.enableOcsp(enableOcsp);
    }

    private KeyManagerFactory getKeyManagerFactory() {
        return kmf;
    }

    private TrustManagerFactory getTrustStoreFactory() {
        return tmf;
    }

    private void setClientAuth(SslContextBuilder serverSslContextBuilder) {
        if (needClientAuth) {
            serverSslContextBuilder.clientAuth(ClientAuth.REQUIRE);
        } else if (wantClientAuth) {
            serverSslContextBuilder.clientAuth(ClientAuth.OPTIONAL);
        } else {
            serverSslContextBuilder.clientAuth(ClientAuth.NONE);
        }
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
