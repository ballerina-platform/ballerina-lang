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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
     * Create https URL connection.
     *
     * @param url           connection URL
     * @param proxyHost     proxy host
     * @param proxyPort     proxy port
     * @param proxyUsername proxy username
     * @param proxyPassword proxy password
     * @return http URL connection
     */
    public static HttpsURLConnection createHttpsUrlConnection(URL url, String proxyHost, int proxyPort,
            String proxyUsername, String proxyPassword) {
        try {
            Proxy proxy = getProxy(proxyHost, proxyPort, proxyUsername, proxyPassword);
            // set proxy if exists.
            if (proxy == null) {
                return (HttpsURLConnection) url.openConnection();
            } else {
                return (HttpsURLConnection) url.openConnection(proxy);
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(e.getMessage());
        }
    }

    /**
     * Set request method of the http connection.
     *
     * @param conn   https connection
     * @param method request method
     */
    public static void setRequestMethod(HttpsURLConnection conn, RequestMethod method) {
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
     * @param conn https connection
     * @return status code
     */
    public static int getStatusCode(HttpsURLConnection conn) {
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            throw ErrorUtil
                    .createCommandException("connection to the remote repository host failed: " + e.getMessage());
        }
    }
}
