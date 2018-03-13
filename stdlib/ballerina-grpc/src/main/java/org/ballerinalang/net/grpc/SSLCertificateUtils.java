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
package org.ballerinalang.net.grpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLContext;

/**
 * SSL certificate generation util class.
 */
public class SSLCertificateUtils {
    /**
     * Creates a new {@link InetSocketAddress} on localhost that overrides the host with.
     */
    public static InetSocketAddress testServerAddress(int port) {
        try {
            InetAddress inetAddress = InetAddress.getByName("localhost");
            inetAddress = InetAddress.getByAddress("foo.test.google.fr", inetAddress.getAddress());
            return new InetSocketAddress(inetAddress, port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
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
        List<String> ciphersMinusGcm = new ArrayList<String>();
        for (String cipher : ciphers) {
            // The GCM implementation in Java is _very_ slow (~1 MB/s)
            if (cipher.contains("_GCM_")) {
                continue;
            }
            ciphersMinusGcm.add(cipher);
        }
        return Collections.unmodifiableList(ciphersMinusGcm);
    }
    
    /**
     * Loads an X.509 certificate from the classpath resources in src/main/resources/certs.
     *
     */
    public static X509Certificate loadX509Cert(String fileName)
            throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        
        InputStream in = new FileInputStream(new File(fileName));
        try {
            return (X509Certificate) cf.generateCertificate(in);
        } finally {
            in.close();
        }
    }
}
