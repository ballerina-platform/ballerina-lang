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

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLException;

import static org.ballerinalang.net.grpc.SSLCertificateUtils.loadX509Cert;
import static org.ballerinalang.net.grpc.SSLCertificateUtils.preferredTestCiphers;

/**
 * A class that encapsulates SSL Certificate Information.
 */
public class SSLHandlerFactory {
    
    /**
     * This method will provide netty ssl context which supports HTTP2 over TLS using
     * Application Layer Protocol Negotiation (ALPN)
     *
     * @return instance of {@link SslContext}
     * @throws SSLException if any error occurred during building SSL context.
     */
    public static SslContext createHttp2TLSContext(SSLConfig sslConfig) throws IOException, CertificateException {
        List<String> ciphers = sslConfig.getCipherSuites() != null && sslConfig.getCipherSuites().length > 0 ? Arrays
                .asList(sslConfig.getCipherSuites()) : preferredTestCiphers();
        SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
        return GrpcSslContexts.forServer(new File(sslConfig.getKeyChainFile()),
                new File(sslConfig.getKeyFile()))
                .trustManager(loadX509Cert(sslConfig.getCertFile()))
                .sslProvider(provider)
                .ciphers(ciphers, SupportedCipherSuiteFilter.INSTANCE)
                .clientAuth(ClientAuth.NONE)
                .build();
    }
}
