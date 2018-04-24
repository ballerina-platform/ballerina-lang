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
package org.ballerinalang.net.grpc.ssl;

import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.ballerinalang.net.grpc.exception.GrpcSSLValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;

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
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import static org.ballerinalang.net.grpc.ssl.SSLHandlerUtils.preferredTestCiphers;
import static org.ballerinalang.net.grpc.ssl.SSLHandlerUtils.writeFile;

/**
 * A class that encapsulates SSL Certificate Information.
 *
 * @since 1.0.0
 */
public class SSLHandlerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SSLHandlerFactory.class);
    private static final String SSL_SERVER_KEY_FILE = "grpcSslServer.key";
    private static final String SSL_SERVER_CERT_FILE = "grpcSslServer.pem";
    private SSLConfig sslConfig;
    private KeyManagerFactory kmf;
    private TrustManagerFactory tmf;
    
    public SSLHandlerFactory(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
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
            if ((sslConfig.getTrustStore() != null) && (sslConfig.getTrustStore().isFile())) {
                KeyStore tks = getKeyStore(sslConfig.getTrustStore(), sslConfig.getTrustStorePass());
                tmf = TrustManagerFactory.getInstance(algorithm);
                tmf.init(tks);
                trustManagers = tmf.getTrustManagers();
            }
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManagers, trustManagers, null);
            
        } catch (UnrecoverableKeyException | KeyManagementException |
                NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new IllegalArgumentException("Failed to initialize the SSLContext", e);
        }
    }
    
    private KeyStore getKeyStore(File trustStore, String trustStorePassword) throws IOException {
        KeyStore ks = null;
        String tlsStoreType = sslConfig.getTLSStoreType();
        if ((trustStore != null && trustStore.isFile()) && (trustStorePassword != null
                && !trustStorePassword.isEmpty())) {
            try (InputStream is = new FileInputStream(trustStore)) {
                ks = KeyStore.getInstance(tlsStoreType);
                ks.load(is, trustStorePassword.toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new IOException(e);
            }
        }
        return ks;
    }
    
    /**
     * This method will provide netty ssl context which supports HTTP2 over TLS using ALPN.
     * Application Layer Protocol Negotiation (ALPN)
     *
     * @return instance of {@link SslContext}
     */
    public SslContext createHttp2TLSContextForServer() {
        
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ? Arrays
                .asList(sslConfig.getCipherSuites()) : preferredTestCiphers();
        SslProvider provider = SslProvider.OPENSSL;
        KeyStore keyStore;
        File keyFile = new File(SSL_SERVER_KEY_FILE);
        File certFile = new File(SSL_SERVER_CERT_FILE);
        try {
            if (keyFile.createNewFile() && certFile.createNewFile()) {
                LOG.debug("Successfully created meta cert and key files. ");
            }
            keyStore = getKeyStore(sslConfig.getKeyStore(), sslConfig.getKeyStorePass());
        } catch (IOException e) {
            throw new GrpcSSLValidationException("Error generating intermediate cert temporary files.", e);
        }
        try {
            writeFile(keyStore, sslConfig.getKeyStorePass());
        } catch (KeyStoreException e) {
            throw new GrpcSSLValidationException("Error writing intermediate cert temporary files.", e);
        }
        SslContext grpcSslContexts;
        try {
            grpcSslContexts = GrpcSslContexts.forServer(certFile, keyFile)
                    .keyManager(kmf)
                    .sslProvider(provider)
                    .ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(
                            new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                    ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1))
                    .clientAuth(sslConfig.isNeedClientAuth() ? ClientAuth.REQUIRE : ClientAuth.NONE)
                    .build();
        } catch (SSLException e) {
            throw new GrpcSSLValidationException("Error generating SSL context.", e);
        }
        if (keyFile.delete() && certFile.delete()) {
            LOG.debug("Successfully deleted meta cert and key files. ");
        }
        return grpcSslContexts;
    }
    
    public SslContext createHttp2TLSContextForClient() throws SSLException {
        // If sender configuration does not include cipher suites , default ciphers required by the HTTP/2
        // specification will be added.
        SslProvider provider = SslProvider.OPENSSL;
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ? Arrays
                .asList(sslConfig.getCipherSuites()) : preferredTestCiphers();
        return GrpcSslContexts.forClient().sslProvider(provider).trustManager(tmf)
                .protocols(sslConfig.getEnableProtocols()).ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                        // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1)).build();
    }
}
