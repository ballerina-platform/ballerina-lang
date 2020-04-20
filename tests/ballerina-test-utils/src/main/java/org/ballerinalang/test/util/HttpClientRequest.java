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

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
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
     * Sends an HTTP GET request to a url.
     *
     * @param requestUrl - The URL of the service. (Example: "http://www.yahoo.com/search?params=value")
     * @param headers    - http request header map
     * @return - HttpResponse from the end point
     * @throws IOException If an error occurs while sending the GET request
     */
    public static HttpResponse doGet(String requestUrl, Map<String, String> headers)
            throws IOException {
        return executeRequestWithoutRequestBody(TestConstant.HTTP_METHOD_GET, requestUrl, headers);
    }

    /**
     * Sends an HTTP GET request to a url.
     *
     * @param requestUrl - The URL of the service. (Example: "http://www.yahoo.com/search?params=value")
     * @param readTimeout - The read timeout of the request
     * @param responseBuilder - Function that allows customizing reading/returning the actual response payload
     * @return - HttpResponse from the end point
     * @throws IOException If an error occurs while sending the GET request
     */
    public static HttpResponse doGet(String requestUrl, int readTimeout, CheckedFunction responseBuilder)
            throws IOException {
        return executeRequestWithoutRequestBody(TestConstant.HTTP_METHOD_GET, requestUrl, new HashMap<>(), readTimeout,
                responseBuilder);
    }

    /**
     * Sends an HTTP GET request to a url.
     *
     * @param requestUrl - The URL of the service. (Example: "http://www.yahoo.com/search?params=value")
     * @return - HttpResponse from the end point
     * @throws IOException If an error occurs while sending the GET request
     */
    public static HttpResponse doGet(String requestUrl) throws IOException {
        return doGet(requestUrl, new HashMap<>());
    }

    /**
     * Sends an HTTP GET request to a url. In case of IOException, instead of printing the stacktrace that error
     * will get bubbled up to the caller.
     *
     * @param requestUrl The URL of the service. (Example: "http://www.yahoo.com/search?params=value")
     * @param throwError Boolean representing whether the error should be thrown instead of printing the stack trace
     * @return HttpResponse from the end point
     * @throws IOException If an error occurs while sending the GET request or in case an error response is received
     */
    public static HttpResponse doGet(String requestUrl, boolean throwError) throws IOException {
        return executeRequestWithoutRequestBody(TestConstant.HTTP_METHOD_GET, requestUrl, new HashMap<>(), throwError);
    }

    public static HttpResponse doGetAndPreserveNewlineInResponseData(String requestUrl) throws IOException {
        return executeRequestAndPreserveNewline(TestConstant.HTTP_METHOD_GET, requestUrl, new HashMap<>());
    }

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
            setHeadersAndMethod(urlConnection, headers, TestConstant.HTTP_METHOD_POST);
            OutputStream out = urlConnection.getOutputStream();
            try {
                Writer writer = new OutputStreamWriter(out, TestConstant.CHARSET_NAME);
                writer.write(postBody);
                writer.close();
            } finally {
                out.close();
            }
            return buildResponse(urlConnection);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Sends an HTTP OPTIONS request to a url.
     *
     * @param requestUrl - The URL of the service. (Example: "http://www.yahoo.com/search?params=value")
     * @param headers    http request headers map
     * @return - HttpResponse from the end point
     * @throws IOException If an error occurs while sending the OPTIONS request
     */
    public static HttpResponse doOptions(String requestUrl, Map<String, String> headers) throws IOException {
        return executeRequestWithoutRequestBody(TestConstant.HTTP_METHOD_OPTIONS, requestUrl, headers);
    }

    /**
     * Sends an HTTP HEAD request to a url.
     *
     * @param requestUrl - The URL of the service. (Example: "http://www.yahoo.com/search?params=value")
     * @return - HttpResponse from the end point
     * @throws IOException If an error occurs while sending the HEAD request
     */
    public static HttpResponse doHead(String requestUrl) throws IOException {
        return doHead(requestUrl, new HashMap<>());
    }

    /**
     * Sends an HTTP HEAD request to a url.
     *
     * @param requestUrl - The URL of the service. (Example: "http://www.yahoo.com/search?params=value")
     * @param headers    - http request header map
     * @return - HttpResponse from the end point
     * @throws IOException If an error occurs while sending the HEAD request
     */
    public static HttpResponse doHead(String requestUrl, Map<String, String> headers) throws IOException {
        HttpURLConnection conn = null;
        HttpResponse httpResponse;
        try {
            conn = getURLConnection(requestUrl);
            setHeadersAndMethod(conn, headers, TestConstant.HTTP_METHOD_HEAD);
            conn.connect();
            httpResponse = new HttpResponse(null, conn.getResponseCode());
            httpResponse.setHeaders(readHeaders(conn));
            httpResponse.setResponseMessage(conn.getResponseMessage());
            return httpResponse;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static HttpResponse executeRequestWithoutRequestBody(String method, String requestUrl, Map<String
            , String> headers) throws IOException {
        return executeRequestWithoutRequestBody(method, requestUrl, headers, DEFAULT_READ_TIMEOUT,
                defaultResponseBuilder);
    }

    private static HttpResponse executeRequestWithoutRequestBody(String method, String requestUrl, Map<String
            , String> headers, boolean throwError) throws IOException {
        return executeRequestWithoutRequestBody(method, requestUrl, headers, DEFAULT_READ_TIMEOUT,
                defaultResponseBuilder, throwError);
    }

    private static HttpResponse executeRequestAndPreserveNewline(String method, String requestUrl, Map<String
            , String> headers) throws IOException {
        return executeRequestWithoutRequestBody(method, requestUrl, headers, DEFAULT_READ_TIMEOUT,
                                                preserveNewLineResponseBuilder);
    }

    private static HttpResponse executeRequestWithoutRequestBody(String method, String requestUrl,
            Map<String, String> headers, int readTimeout, CheckedFunction responseBuilder) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = getURLConnection(requestUrl, readTimeout);
            setHeadersAndMethod(conn, headers, method);
            conn.connect();
            return buildResponse(conn, responseBuilder, false);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static HttpResponse executeRequestWithoutRequestBody(String method, String requestUrl,
                                                                 Map<String, String> headers, int readTimeout,
                                                                 CheckedFunction responseBuilder, boolean throwError)
            throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = getURLConnection(requestUrl, readTimeout);
            setHeadersAndMethod(conn, headers, method);
            conn.connect();
            return buildResponse(conn, responseBuilder, throwError);
        } finally {
            if (conn != null) {
                conn.disconnect();
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
        try {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()
                    , Charset.defaultCharset()));
            responseData = responseBuilder.apply(rd);
        } catch (IOException ex) {
            if (conn.getErrorStream() == null) {
                if (throwError) {
                    if (ex instanceof FileNotFoundException) {
                        throw new IOException("Server returned HTTP response code: 404 or 410");
                    }
                    throw ex;
                } else {
                    LOG.error("Error in building HTTP response", ex.getMessage());
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
        httpResponse.setResponseMessage(conn.getResponseMessage());
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

    private static CheckedFunction<BufferedReader, String> preserveNewLineResponseBuilder = ((bufferedReader) -> {
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
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

    /**
     * Sends multipart/form-data requests.
     *
     * @param requestUrl - The URL of the service
     * @param headers    - http request header map
     * @param formData   - map of form data
     * @return - HttpResponse from the end point
     * @throws IOException If an error occurs while sending the GET request
     */
    public static HttpResponse doMultipartFormData(String requestUrl, Map<String, String> headers,
                                                   Map<String, String> formData) throws IOException {
        String boundary = "---" + System.currentTimeMillis() + "---";
        String lineFeed = "\r\n";
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = getURLConnection(requestUrl);
            setHeadersAndMethod(urlConnection, headers, TestConstant.HTTP_METHOD_POST);
            urlConnection.setUseCaches(false);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty(HttpHeaderNames.CONTENT_TYPE.toString(),
                                             HttpHeaderValues.MULTIPART_FORM_DATA + "; boundary=" + boundary);

            try (OutputStream out = urlConnection.getOutputStream()) {
                Writer writer = new OutputStreamWriter(out, TestConstant.CHARSET_NAME);
                for (Map.Entry<String, String> data : formData.entrySet()) {
                    writer.append("--" + boundary).append(lineFeed);
                    writer.append("Content-Disposition: form-data; name=\"" + data.getKey() + "\"")
                            .append(lineFeed);
                    writer.append(HttpHeaderNames.CONTENT_TYPE.toString() + ":" + HttpHeaderValues.TEXT_PLAIN +
                                          "; charset=" + TestConstant.CHARSET_NAME).append(lineFeed);
                    writer.append(lineFeed);
                    writer.append(data.getValue()).append(lineFeed);
                    writer.flush();
                }
                writer.append(lineFeed).flush();
                writer.append("--" + boundary + "--").append(lineFeed);
                writer.close();
            }
            return buildResponse(urlConnection);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
