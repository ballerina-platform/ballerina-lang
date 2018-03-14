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
import org.ballerinalang.net.grpc.exception.GrpcSSLValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import static org.ballerinalang.net.grpc.SSLCertificateUtils.preferredTestCiphers;

/**
 * A class that encapsulates SSL Certificate Information.
 */
public class SSLHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSLHandlerFactory.class);
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    private static final String BEGIN_KEY = "-----BEGIN PRIVATE KEY-----";
    private static final String END_KEY = "-----END PRIVATE KEY-----";
    private static final String SSL_SERVER_KEY_FILE = "grpcSslServer.key";
    private static final String SSL_SERVER_CERT_FILE = "grpcSslServer.pem";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    
    public static KeyManagerFactory generateKeyManagerFactory(SSLConfig sslConfig) {
        KeyManagerFactory kmf;
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        try {
            KeyStore ks = getKeyStore(sslConfig);
            kmf = KeyManagerFactory.getInstance(algorithm);
            if (ks != null) {
                kmf.init(ks, sslConfig.getCertPass() != null ?
                        sslConfig.getCertPass().toCharArray() :
                        sslConfig.getKeyStorePass().toCharArray());
            }
            return kmf;
        } catch (UnrecoverableKeyException |
                NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new IllegalArgumentException("Failed to initialize the SSLContext", e);
        }
    }
    
    /**
     * Generate trust manager.
     *
     * @param sslConfig endpoint configuration.
     * @return trust manager.
     */
    public static TrustManagerFactory generateTrustManagerFactory(SSLConfig sslConfig) {
        TrustManagerFactory tmf = null;
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        try {
            if (sslConfig.getTrustStore() != null) {
                KeyStore tks = getKeyStore(sslConfig);
                tmf = TrustManagerFactory.getInstance(algorithm);
                tmf.init(tks);
            }
            return tmf;
        } catch (
                NoSuchAlgorithmException | KeyStoreException | IOException e) {
            throw new IllegalArgumentException("Failed to initialize the SSLContext", e);
        }
    }
    
    /**
     * Get keystore object.
     *
     * @param sslConfig endpoint configuration.
     * @return keystore file
     * @throws IOException
     */
    private static KeyStore getKeyStore(SSLConfig sslConfig) throws IOException {
        KeyStore ks = null;
        String tlsStoreType = sslConfig.getTLSStoreType();
        if (sslConfig.getKeyStore() != null && sslConfig.getKeyStorePass() != null) {
            try (InputStream is = new FileInputStream(sslConfig.getKeyStore())) {
                ks = KeyStore.getInstance(tlsStoreType);
                ks.load(is, sslConfig.getKeyStorePass().toCharArray());
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
    public static SslContext createSSLContext(SSLConfig sslConfig, TrustManagerFactory tmf,
                                              KeyManagerFactory kmf) {
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ? Arrays
                .asList(sslConfig.getCipherSuites()) : preferredTestCiphers();
        SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
        KeyStore keyStore;
        File keyfile = new File(SSL_SERVER_KEY_FILE);
        File certfile = new File(SSL_SERVER_CERT_FILE);
        try {
            if (keyfile.createNewFile() && certfile.createNewFile()) {
                LOGGER.debug("Successfully created meta cert and key files. ");
            }
            keyStore = getKeyStore(sslConfig);
        } catch (IOException e) {
            throw new GrpcSSLValidationException("Error generating intermediate cert temporary files.", e);
        }
        try {
            writeFile(keyStore, sslConfig.getKeyStorePass());
        } catch (KeyStoreException e) {
            throw new GrpcSSLValidationException("Error writing intermediate cert temporary files.", e);
        }
        SslContext grpcSslContexts = null;
        try {
            grpcSslContexts = GrpcSslContexts.forServer(certfile, keyfile)
                    .keyManager(kmf)
                    .trustManager(tmf)
                    .sslProvider(provider)
                    .ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE)
                    .clientAuth(ClientAuth.NONE)
                    .build();
        } catch (SSLException e) {
            throw new GrpcSSLValidationException("Error generating SSL context.", e);
        }
        if (keyfile.delete() && certfile.delete()) {
            LOGGER.debug("Successfully deleted meta cert and key files. ");
        }
        return grpcSslContexts;
    }
    
    private static void writeFile(KeyStore keyStore, String keyStorePass) throws KeyStoreException {
        List<Certificate> userCertificate = getCertificates(keyStore);
        try (BufferedWriter writer2 = new BufferedWriter(new FileWriter(SSL_SERVER_KEY_FILE));
             BufferedWriter writer = new BufferedWriter(new FileWriter(SSL_SERVER_CERT_FILE))) {
            writer.write(formatCrtFileContents(userCertificate));
            writer2.write(formatKeyFileContents(keyStore.getKey(keyStorePass, keyStorePass.toCharArray())
                    .getEncoded()));
        } catch (IOException |
                UnrecoverableKeyException |
                NoSuchAlgorithmException e) {
            throw new GrpcSSLValidationException("Error writing cert files.", e);
        }
    }
    
    private static List<Certificate> getCertificates(KeyStore keyStore) throws KeyStoreException {
        List<Certificate> certs = new ArrayList<>();
        for (String alias : Collections.list(keyStore.aliases())) {
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);
            if (certificateChain != null) {
                certs.addAll(Arrays.asList(certificateChain));
            }
            certs.add(keyStore.getCertificate(alias));
        }
        return certs;
    }
    
    private static String formatCrtFileContents(final List<Certificate> rawCrtText) {
        Base64.Encoder encoder;
        StringBuilder encodedCerts = new StringBuilder();
        try {
            encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new GrpcSSLValidationException("Invalid Charset Getting Encoder.", e);
        }
        for (Certificate certificate : rawCrtText) {
            try {
                encodedCerts.append(new String(encoder.encode(certificate.getEncoded()), "UTF-8"))
                        .append(LINE_SEPARATOR);
            } catch (CertificateEncodingException e) {
                throw new GrpcSSLValidationException("Error getting cert chain.", e);
            } catch (UnsupportedEncodingException e) {
                throw new GrpcSSLValidationException("Invalid Charset Name at Cert generation.", e);
            }
        }
        return BEGIN_CERT + LINE_SEPARATOR + encodedCerts.toString() + LINE_SEPARATOR + END_CERT;
    }
    
    private static String formatKeyFileContents(final byte[] rawKeyText) {
        Base64.Encoder encoder;
        String encodedCertText;
        try {
            encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new GrpcSSLValidationException("Invalid Charset Getting Encoder.", e);
        }
        try {
            encodedCertText = new String(encoder.encode(rawKeyText), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new GrpcSSLValidationException("Invalid Charset Name at key generation.", e);
        }
        return BEGIN_KEY + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_KEY;
    }
}
