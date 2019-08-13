/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 *  Checks if there is a latest version in central if version is not mentioned. If there is then the version of the
 *  module is updated with that version.
 */
public class URIDryConverter extends URIConverter {
    private PrintStream errStream = System.err;
    private List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
    private Proxy proxy;
    
    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
            }
    };
    
    public URIDryConverter(URI base) {
        this(base, false);
    }
    
    public URIDryConverter(URI base, boolean isBuild) {
        super(base, isBuild);
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            supportedPlatforms.add("any");
            proxy = getProxy();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // ignore errors
        }
    }
    
    public Stream<CompilerInput> finalize(URI remoteURI, PackageID moduleID) {
        try {
            // only continue if module version is not set. a module version may be set through Ballerina.toml or
            // Ballerina.lock already.
            if ("".equals(moduleID.version.value) || "*".equals(moduleID.version.value)) {
                for (String supportedPlatform : supportedPlatforms) {
                    HttpURLConnection conn;
                    // set proxy if exists.
                    if (null == this.proxy) {
                        conn = (HttpURLConnection) remoteURI.toURL().openConnection();
                    } else {
                        conn = (HttpURLConnection) remoteURI.toURL().openConnection(this.proxy);
                    }
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestMethod("GET");
                    // set implementation version
                    conn.setRequestProperty("Ballerina-Platform", supportedPlatform);
                    // TODO: conn.setRequestProperty("Ballerina-Language-Specification-Version",
                    //  IMPLEMENTATION_VERSION);
                    conn.setRequestProperty("Ballerina-Language-Specficiation-Version", IMPLEMENTATION_VERSION);
                    if (conn.getResponseCode() == 302) {
                        // get the version from the 'Location' header.
                        String location = conn.getHeaderField("Location");
                        String version = location.split("/")[location.split("/").length - 3];
                        // update version
                        moduleID.version = new Name(version);
                        return Stream.empty();
                    } else {
                        errStream.println("could not connect to remote repository or unexpected response received.");
                    }
                    conn.disconnect();
                    Authenticator.setDefault(null);
                }
            }
        } catch (IOException e) {
            // ignore error and don't set the version.
        }
    
        return Stream.empty();
    }
    
    /**
     * Get proxy for http connection.
     *
     * @return The proxy object.
     */
    private Proxy getProxy() {
        org.ballerinalang.toml.model.Proxy proxy = TomlParserUtils.readSettings().getProxy();
        if (!"".equals(proxy.getHost())) {
            InetSocketAddress proxyInet = new InetSocketAddress(proxy.getHost(), proxy.getPort());
            if (!"".equals(proxy.getUserName()) && "".equals(proxy.getPassword())) {
                Authenticator authenticator = new RemoteAuthenticator();
                Authenticator.setDefault(authenticator);
            }
            return new Proxy(Proxy.Type.HTTP, proxyInet);
        }
        
        return null;
    }
    
    /**
     * Authenticator for the proxy server if provided.
     */
    static class RemoteAuthenticator extends Authenticator {
        org.ballerinalang.toml.model.Proxy proxy;
        public RemoteAuthenticator() {
            proxy = TomlParserUtils.readSettings().getProxy();
        }
        
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return (new PasswordAuthentication(this.proxy.getUserName(), this.proxy.getPassword().toCharArray()));
        }
    }
}
