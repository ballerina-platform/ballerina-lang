// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.cli.module.util;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.ballerinalang.cli.module.util.CliModuleConstants.SSL;

/**
 * Class contains miscellaneous utility methods.
 *
 * @since 1.2.0
 */
public class Utils {

    /**
     * Request method types.
     */
    public enum RequestMethod {
        GET, POST
    }

    private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            //No need to implement.
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            //No need to implement.
        }
    } };

    /**
     * Get proxy for http connection.
     *
     * @param proxyHost     proxy host
     * @param proxyPort     proxy port
     * @param proxyUsername proxy username
     * @param proxyPassword proxy password
     * @return The proxy object.
     */
    public static Proxy getProxy(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        if (!"".equals(proxyHost)) {
            InetSocketAddress proxyInet = new InetSocketAddress(proxyHost, proxyPort);
            if (!"".equals(proxyUsername) && "".equals(proxyPassword)) {
                Authenticator authenticator = new Authenticator() {
                    @Override public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
            }
            return new Proxy(Proxy.Type.HTTP, proxyInet);
        }
        return null;
    }

    /**
     * Convert string to URL.
     *
     * @param url string URL
     * @return URL
     */
    public static URL convertToUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw ErrorUtil.createCommandException(e.getMessage());
        }
    }

    /**
     * Initialize SSL.
     */
    public static void initializeSsl() {
        try {
            SSLContext sc = SSLContext.getInstance(SSL);
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw ErrorUtil.createCommandException("initializing SSL failed: " + e.getMessage());
        }
    }

    /**
     * Create http URL connection.
     *
     * @param url           connection URL
     * @param proxyHost     proxy host
     * @param proxyPort     proxy port
     * @param proxyUsername proxy username
     * @param proxyPassword proxy password
     * @return http URL connection
     */
    public static HttpURLConnection createHttpUrlConnection(URL url, String proxyHost, int proxyPort,
            String proxyUsername, String proxyPassword) {
        try {
            Proxy proxy = getProxy(proxyHost, proxyPort, proxyUsername, proxyPassword);
            // set proxy if exists.
            if (proxy == null) {
                return (HttpURLConnection) url.openConnection();
            } else {
                return (HttpURLConnection) url.openConnection(proxy);
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(e.getMessage());
        }
    }

    /**
     * Set request method of the http connection.
     *
     * @param conn   http connection
     * @param method request method
     */
    public static void setRequestMethod(HttpURLConnection conn, RequestMethod method) {
        try {
            conn.setRequestMethod(getRequestMethodAsString(method));
        } catch (ProtocolException e) {
            throw ErrorUtil.createCommandException(e.getMessage());
        }
    }

    private static String getRequestMethodAsString(RequestMethod method) {
        switch (method) {
        case GET:
            return "GET";
        case POST:
            return "POST";
        default:
            throw ErrorUtil.createCommandException("invalid request method:" + method);
        }
    }

    /**
     * Get status code of http response.
     *
     * @param conn http connection
     * @return status code
     */
    public static int getStatusCode(HttpURLConnection conn) {
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            throw ErrorUtil
                    .createCommandException("connection to the remote repository host failed: " + e.getMessage());
        }
    }
}
