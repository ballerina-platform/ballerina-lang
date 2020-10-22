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

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *  Checks if there is a latest version in central if version is not mentioned. If there is then the version of the
 *  module is updated with that version.
 */
public class URIDryConverter extends URIConverter {

    /**
     * This is to keep track that an error has already been logged so that it doesn't repeatedly log the same error.
     */
    private static boolean loggedError = false;
    private static final Pattern semVerPatchPattern = Pattern.compile("(\\d+)\\.(\\d+)");
    private PrintStream errStream = System.err;
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

    public URIDryConverter(URI base, Map<PackageID, Manifest> dependencyManifests) {
        this(base, dependencyManifests, false);
    }

    public URIDryConverter(URI base, Map<PackageID, Manifest> dependencyManifests, boolean isBuild) {
        super(base, dependencyManifests, isBuild);
        this.base = URI.create(base.toString() + "/modules/info/");
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            proxy = getProxy();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // ignore errors
        }
    }

    public Stream<CompilerInput> finalize(URI remoteURI, PackageID moduleID) {
        try {
            // only continue if a fixed module version is not set. a module version may be set through Ballerina.toml or
            // Ballerina.lock already.
            Matcher matcher = semVerPatchPattern.matcher(moduleID.version.value);
            if ("".equals(moduleID.version.value) || "*".equals(moduleID.version.value) || matcher.matches()) {
                HttpURLConnection conn;
                // set proxy if exists.
                if (null == this.proxy) {
                    conn = (HttpURLConnection) remoteURI.toURL().openConnection();
                } else {
                    conn = (HttpURLConnection) remoteURI.toURL().openConnection(this.proxy);
                }
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("User-Agent", RepoUtils.getBallerinaVersion());
                conn.setRequestMethod("GET");

                // status code and meaning
                //// 200 - module info found
                //// 400 - bad request sent
                //// 500 - backend is broken
                int statusCode = conn.getResponseCode();
                if (statusCode == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),
                            Charset.defaultCharset()))) {
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        Object payload = JSONParser.parse(result.toString());
                        if (payload instanceof BMap) {
                            BMap moduleInfo = ((BMap) payload).getMapValue(StringUtils.fromString("module"));
                            String version = moduleInfo.getStringValue(StringUtils.fromString("version")).getValue();
                            moduleID.version = new Name(version);
                        }
                    }
                } else if (statusCode == 400 && !loggedError) {
                    try (BufferedReader errorStream = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                        String errorContent = errorStream.lines().collect(Collectors.joining("\n"));
                        this.errStream.println("error: could not connect to remote repository to find the latest " +
                                "version of module: " + moduleID.toString() + ". use '--offline' " +
                                "flag to build in offline mode. reason: " + errorContent);
                        setErrorLoggedStatusAsTrue();
                    }
                } else if (statusCode == 500 && !loggedError) {
                    this.errStream.println("error: could not connect to remote repository to find the latest " +
                            "version of module: " + moduleID.toString() + ". use '--offline' " +
                            "flag to build in offline mode.");
                    setErrorLoggedStatusAsTrue();
                }
                conn.disconnect();
                Authenticator.setDefault(null);
            }
        } catch (IOException e) {
            // ignore error and don't set the version.
        }

        return Stream.empty();
    }

    /**
     * Set the status that an error has been logged.
     */
    private static void setErrorLoggedStatusAsTrue() {
        loggedError = true;
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
