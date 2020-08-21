/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.datamapper.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This class can be used to send http request.
 */
public class HttpClientRequest {
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientRequest.class);
    private static final int DEFAULT_READ_TIMEOUT = 30000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 15000;

    /**
     * Send an HTTP POST request to a service.
     *
     * @param endpoint - service endpoint
     * @param postBody - message payload
     * @param headers  http request headers map
     * @return - HttpResponse from end point
     * @throws IOException If an error occurs while sending the GET request
     */
    public static HttpResponse doPost(String endpoint, String postBody, Map<String, String> headers)
            throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = getURLConnection(endpoint);
            setHeadersAndMethod(urlConnection, headers, "POST");
            try (OutputStream out = urlConnection.getOutputStream();
                 Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                writer.write(postBody);
            }
            return buildResponse(urlConnection);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static HttpURLConnection getURLConnection(String requestUrl) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setReadTimeout(DEFAULT_READ_TIMEOUT);
        conn.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        conn.setDoInput(true);
        conn.setUseCaches(true);
        conn.setAllowUserInteraction(false);
        return conn;
    }

    private static void setHeadersAndMethod(HttpURLConnection conn, Map<String, String> headers, String method)
            throws ProtocolException {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            conn.addRequestProperty(e.getKey(), e.getValue());
        }
        conn.setRequestMethod(method);
    }

    private static HttpResponse buildResponse(HttpURLConnection conn) throws IOException {
        return buildResponse(conn, defaultResponseBuilder);
    }

    private static HttpResponse buildResponse(HttpURLConnection conn,
                                              CheckedFunction<BufferedReader, String> responseBuilder)
            throws IOException {
        HttpResponse httpResponse;
        BufferedReader rd = null;
        String responseData;
        try (InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream(),
                Charset.defaultCharset())) {
            rd = new BufferedReader(inputStreamReader);
            responseData = responseBuilder.apply(rd);
        } catch (IOException ex) {
            if (conn.getErrorStream() == null) {
                LOG.error("Error in building HTTP response", ex.getMessage());
                if (rd != null) {
                    rd.close();
                }
                return null;
            }
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()
                    , Charset.defaultCharset()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            responseData = sb.toString();
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
        httpResponse = new HttpResponse(responseData, conn.getResponseCode());
        return httpResponse;
    }

    private static CheckedFunction<BufferedReader, String> defaultResponseBuilder = ((bufferedReader) -> {
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    });

    /**
     * This is a custom functional interface which allows defining a method that throws an IOException.
     *
     * @param <T> Input parameter type
     * @param <R> Return type
     */
    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws IOException;
    }
}
