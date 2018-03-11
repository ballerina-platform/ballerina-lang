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
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.ballerinalang.net.grpc.MessageConstants;

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

import static org.ballerinalang.net.grpc.SSLCertificateUtils.loadX509Cert;
import static org.ballerinalang.net.grpc.SSLCertificateUtils.preferredTestCiphers;

/**
 * A class that encapsulates SSL Certificate Information.
 */
public class SSLHandlerFactory {
    ;
    private SSLConfig sslConfig;
    
    public SSLHandlerFactory(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        String protocol = sslConfig.getSSLProtocol();
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
                KeyStore tks = getKeyStore(sslConfig.getTrustStore(), sslConfig.getTrustStorePass());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
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
    
    /**
     * Get keystore object
     *
     * @param keyStore         the key store
     * @param keyStorePassword the key store password.
     * @return keystore object
     * @throws IOException
     */
    private KeyStore getKeyStore(File keyStore, String keyStorePassword) throws IOException {
        KeyStore ks = null;
        String tlsStoreType = sslConfig.getTLSStoreType();
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
     * This method will provide netty ssl context which supports HTTP2 over TLS using
     * Application Layer Protocol Negotiation (ALPN)
     *
     * @return instance of {@link SslContext}
     * @throws SSLException if any error occurred during building SSL context.
     */
    public SslContext createHttp2TLSContext() throws IOException, CertificateException {
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ? Arrays
                .asList(sslConfig.getCipherSuites()) : preferredTestCiphers();
        SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
        return GrpcSslContexts.forServer(new File(MessageConstants.PATH + "server1.pem"),
                new File(MessageConstants.PATH + "server1.key"))
                .trustManager(loadX509Cert(MessageConstants.PATH + "ca.pem"))
                .sslProvider(provider)
                .ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE)
                .clientAuth(ClientAuth.NONE)
                .build();
    }
}
