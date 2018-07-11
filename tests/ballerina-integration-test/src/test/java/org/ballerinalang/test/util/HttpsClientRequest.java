/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.HttpsURLConnection;

/**
 * This class can be used to send http request.
 */
public class HttpsClientRequest {
    /**
     * Sends an HTTP GET request to a url.
     *
     * @param requestUrl - The URL of the service. (Example: "http://www.yahoo.com/search?params=value")
     * @param headers    - http request header map
     * @return - HttpResponse from the end point
     * @throws IOException If an error occurs while sending the GET request
     */
    public static HttpResponse doGet(String requestUrl, Map<String, String> headers, String serverHome)
            throws IOException {
        HttpsURLConnection conn = null;
        HttpResponse httpResponse;
        try {
            conn = getURLConnection(requestUrl, serverHome);
            //setting request headers
            for (Map.Entry<String, String> e : headers.entrySet()) {
                conn.setRequestProperty(e.getKey(), e.getValue());
            }
            conn.setRequestMethod(TestConstant.HTTP_METHOD_GET);
            conn.connect();
            StringBuilder sb = new StringBuilder();
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()
                        , Charset.defaultCharset()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                httpResponse = new HttpResponse(sb.toString(), conn.getResponseCode());
            } catch (IOException ex) {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()
                        , Charset.defaultCharset()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                httpResponse = new HttpResponse(sb.toString(), conn.getResponseCode());
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }
            httpResponse.setHeaders(readHeaders(conn));
            httpResponse.setResponseMessage(conn.getResponseMessage());
            return httpResponse;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Sends an HTTP GET request to a url.
     *
     * @param requestUrl - The URL of the service. (Example: "http://www.yahoo.com/search?params=value")
     * @return - HttpResponse from the end point
     * @throws IOException If an error occurs while sending the GET request
     */
    public static HttpResponse doGet(String requestUrl, String serverHome) throws IOException {
        return doGet(requestUrl, new HashMap<>(), serverHome);
    }

    /**
     * Send a Http POST request to a service.
     *
     * @param endpoint - service endpoint
     * @param postBody - message payload
     * @param headers  http request headers map
     * @return - HttpResponse from end point
     * @throws IOException If an error occurs while sending the GET request
     */
    public static HttpResponse doPost(String endpoint, String postBody, Map<String, String> headers, String serverHome)
            throws IOException {
        HttpsURLConnection urlConnection = null;
        HttpResponse httpResponse;
        try {
            urlConnection = getURLConnection(endpoint, serverHome);
            //setting request headers
            for (Map.Entry<String, String> e : headers.entrySet()) {
                urlConnection.setRequestProperty(e.getKey(), e.getValue());
            }
            urlConnection.setRequestMethod(TestConstant.HTTP_METHOD_POST);
            OutputStream out = urlConnection.getOutputStream();
            try {
                Writer writer = new OutputStreamWriter(out, TestConstant.CHARSET_NAME);
                writer.write(postBody);
                writer.close();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
            // Get the response
            StringBuilder sb = new StringBuilder();
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()
                        , Charset.defaultCharset()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                if (urlConnection.getErrorStream() == null) {
                    return null;
                }
                rd = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()
                        , Charset.defaultCharset()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }
            Map<String, String> responseHeaders = readHeaders(urlConnection);
            httpResponse = new HttpResponse(sb.toString(), urlConnection.getResponseCode(), responseHeaders);
            httpResponse.setResponseMessage(urlConnection.getResponseMessage());
            return httpResponse;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static HttpsURLConnection getURLConnection(String requestUrl, String serverHome)
            throws IOException {
        setSSlSystemProperties(serverHome);
        URL url = new URL(requestUrl);

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setReadTimeout(30000);
        conn.setConnectTimeout(15000);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        return conn;
    }

    private static Map<String, String> readHeaders(URLConnection urlConnection) {
        Iterator<String> itr = urlConnection.getHeaderFields().keySet().iterator();
        Map<String, String> headers = new HashMap();
        while (itr.hasNext()) {
            String key = itr.next();
            if (key != null) {
                headers.put(key, urlConnection.getHeaderField(key));
            }
        }
        return headers;
    }

    /**
     * Helper method to set the SSL context.
     *
     * @param serverHome
     */
    private static void setSSlSystemProperties(String serverHome) {
        String keystorePath = serverHome + File.separator + "bre" + File.separator
                + "security" + File.separator + "ballerinaKeystore.p12";
        String truststorePath = serverHome + File.separator + "bre" + File.separator
                + "security" + File.separator + "ballerinaTruststore.p12";
        Properties systemProps = System.getProperties();
        systemProps.put("javax.net.ssl.keyStoreType", "PKCS12");
        systemProps.put("javax.net.ssl.trustStoreType", "PKCS12");
        systemProps.put("javax.net.ssl.keyStorePassword", "ballerina");
        systemProps.put("javax.net.ssl.keyStore", keystorePath);
        systemProps.put("javax.net.ssl.trustStore", truststorePath);
        systemProps.put("javax.net.ssl.trustStorePassword", "ballerina");
        System.setProperties(systemProps);
    }
}
