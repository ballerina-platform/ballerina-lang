/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
 */
package org.ballerinalang.datamapper.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class can be used to send http request.
 */
public class HttpClientRequest {
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientRequest.class);
    private static final int DEFAULT_READ_TIMEOUT = 30000;

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
        return getURLConnection(requestUrl, DEFAULT_READ_TIMEOUT);
    }

    private static HttpURLConnection getURLConnection(String requestUrl, int readTimeout) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setReadTimeout(readTimeout);
        conn.setConnectTimeout(15000);
        conn.setDoInput(true);
        conn.setUseCaches(true);
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

    private static void setHeadersAndMethod(HttpURLConnection conn, Map<String, String> headers, String method)
            throws ProtocolException {
        for (Map.Entry<String, String> e : headers.entrySet()) {
            conn.addRequestProperty(e.getKey(), e.getValue());
        }
        conn.setRequestMethod(method);
    }

    private static HttpResponse buildResponse(HttpURLConnection conn) throws IOException {
        return buildResponse(conn, defaultResponseBuilder, false);
    }

    private static HttpResponse buildResponse(HttpURLConnection conn,
                                              CheckedFunction<BufferedReader, String> responseBuilder,
                                              boolean throwError) throws IOException {
        HttpResponse httpResponse;
        BufferedReader rd = null;
        String responseData;
        try (InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream(),
                Charset.defaultCharset())) {
            rd = new BufferedReader(inputStreamReader);
            responseData = responseBuilder.apply(rd);
        } catch (IOException ex) {
            if (conn.getErrorStream() == null) {
                if (throwError) {
                    if (ex instanceof FileNotFoundException) {
                        if (rd != null) {
                            rd.close();
                        }
                        throw new IOException("Server returned HTTP response code: 404 or 410");
                    }
                    throw ex;
                } else {
                    LOG.error("Error in building HTTP response", ex.getMessage());
                    if (rd != null) {
                        rd.close();
                    }
                    return null;
                }
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
        Map<String, String> responseHeaders = readHeaders(conn);
        httpResponse = new HttpResponse(responseData, conn.getResponseCode(), responseHeaders);
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
