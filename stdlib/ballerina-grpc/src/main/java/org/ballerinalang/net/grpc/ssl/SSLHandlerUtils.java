/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.ballerinalang.net.grpc.ssl;

import org.ballerinalang.net.grpc.exception.GrpcSSLValidationException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLContext;

/**
 * Util class for generating certificates.
 *
 * @since 1.0.0
 */
public class SSLHandlerUtils {
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    private static final String BEGIN_KEY = "-----BEGIN PRIVATE KEY-----";
    private static final String END_KEY = "-----END PRIVATE KEY-----";
    private static final String SSL_SERVER_KEY_FILE = "grpcSslServer.key";
    private static final String SSL_SERVER_CERT_FILE = "grpcSslServer.pem";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    
    static void writeFile(KeyStore keyStore, String keyStorePass) throws KeyStoreException {
        List<Certificate> userCertificate = getCertificates(keyStore);
        try (BufferedWriter writer2 = Files.newBufferedWriter(Paths.get(SSL_SERVER_KEY_FILE));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(SSL_SERVER_CERT_FILE))) {
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
    
    /**
     * Returns the ciphers preferred to use during tests. They may be chosen because they are widely,
     * available or because they are fast. There is no requirement that they provide confidentiality
     * or integrity.
     */
    public static List<String> preferredTestCiphers() {
        String[] ciphers;
        try {
            ciphers = SSLContext.getDefault().getDefaultSSLParameters().getCipherSuites();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        List<String> ciphersMinusGcm = new ArrayList<>();
        for (String cipher : ciphers) {
            // The GCM implementation in Java is _very_ slow (~1 MB/s)
            if (cipher.contains("_GCM_")) {
                continue;
            }
            ciphersMinusGcm.add(cipher);
        }
        return Collections.unmodifiableList(ciphersMinusGcm);
    }
    
    public static String formatKeyFileContents(final byte[] rawKeyText) {
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
