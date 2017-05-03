/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.output.transport.http;

import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.TestConnectionNotSupportedException;
import org.wso2.siddhi.core.stream.output.sink.Sink;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;

@Extension(
        name = "http",
        namespace = "sink",
        description = ""
)
public class HttpSink extends Sink {
    public static final String ADAPTER_TYPE_HTTP = "http";
    public static final String ADAPTER_MESSAGE_URL = "http.url";
    public static final String ADAPTER_MESSAGE_URL_HINT = "http.url.hint";
    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;
    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS = 20000;
    public static final String ADAPTER_MIN_THREAD_POOL_SIZE_NAME = "minThread";
    public static final String ADAPTER_MAX_THREAD_POOL_SIZE_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";
    public static final String ADAPTER_PROXY_HOST = "http.proxy.host";
    public static final String ADAPTER_PROXY_HOST_HINT = "http.proxy.host.hint";
    public static final String ADAPTER_PROXY_PORT = "http.proxy.port";
    public static final String ADAPTER_PROXY_PORT_HINT = "http.proxy.port.hint";
    public static final String ADAPTER_USERNAME = "http.username";
    public static final String ADAPTER_USERNAME_HINT = "http.username.hint";
    public static final String ADAPTER_PASSWORD = "http.password";
    public static final String ADAPTER_PASSWORD_HINT = "http.password.hint";
    public static final String ADAPTER_HEADERS = "http.headers";
    public static final String ADAPTER_HEADERS_HINT = "http.headers.hint";
    public static final String HEADER_SEPARATOR = ",";
    public static final String ENTRY_SEPARATOR = ":";
    public static final String ADAPTER_HTTP_CLIENT_METHOD = "http.client.method";
    public static final String CONSTANT_HTTP_POST = "HttpPost";
    public static final String CONSTANT_HTTP_PUT = "HttpPut";
    //configurations for the httpConnectionManager
    public static final String DEFAULT_MAX_CONNECTIONS_PER_HOST = "defaultMaxConnectionsPerHost";
    public static final int DEFAULT_DEFAULT_MAX_CONNECTIONS_PER_HOST = 2;
    public static final String MAX_TOTAL_CONNECTIONS = "maxTotalConnections";
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 20;

    private static final Logger log = Logger.getLogger(HttpSink.class);
    private static ExecutorService executorService;
    private static HttpConnectionManager connectionManager;
    private String contentType;
    private HttpClient httpClient = null;
    private HostConfiguration hostConfiguration = null;

    @Override
    public void init(String type, Map<String, String> options, Map<String, String> unmappedDynamicOptions) {
        if (executorService == null) {
            int minThread = (options.get(ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null)
                    ? Integer.parseInt(options.get(ADAPTER_MIN_THREAD_POOL_SIZE_NAME))
                    : ADAPTER_MIN_THREAD_POOL_SIZE;
            int maxThread = (options.get(ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null)
                    ? Integer.parseInt(options.get(ADAPTER_MAX_THREAD_POOL_SIZE_NAME))
                    : ADAPTER_MAX_THREAD_POOL_SIZE;
            long defaultKeepAliveTime = (options.get(ADAPTER_KEEP_ALIVE_TIME_NAME) != null)
                    ? Integer.parseInt(options.get(ADAPTER_KEEP_ALIVE_TIME_NAME))
                    : DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS;
            int jobQueSize = (options.get(ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null)
                    ? Integer.parseInt(options.get(ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME))
                    : ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            int defaultMaxConnectionsPerHost = (options.get(DEFAULT_MAX_CONNECTIONS_PER_HOST) != null)
                    ? Integer.parseInt(options.get(DEFAULT_MAX_CONNECTIONS_PER_HOST))
                    : DEFAULT_DEFAULT_MAX_CONNECTIONS_PER_HOST;
            int maxTotalConnections = (options.get(MAX_TOTAL_CONNECTIONS) != null)
                    ? Integer.parseInt(options.get(MAX_TOTAL_CONNECTIONS))
                    : DEFAULT_MAX_TOTAL_CONNECTIONS;

            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(jobQueSize));

            connectionManager = new MultiThreadedHttpConnectionManager();
            connectionManager.getParams().setDefaultMaxConnectionsPerHost(defaultMaxConnectionsPerHost);
            connectionManager.getParams().setMaxTotalConnections(maxTotalConnections);
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("Test connection is not available");
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        // TODO: 1/8/17 fix properly
        // checkHTTPClientInit(eventAdapterConfiguration.getStaticProperties());
        checkHTTPClientInit(new HashMap<String, String>());
    }

    @Override
    public void publish(Object event, Map<String, String> dynamicTransportOptions)
            throws ConnectionUnavailableException {
        // TODO: 1/8/17 load dynamic properties and fix properly
        // String url = dynamicProperties.get(ADAPTER_MESSAGE_URL);
        // String username = dynamicProperties.get(ADAPTER_USERNAME);
        // String password = dynamicProperties.get(ADAPTER_PASSWORD);
        // Map<String, String> headers = this.extractHeaders(dynamicProperties.get(ADAPTER_HEADERS));

        String url = "http://0.0.0.0:8080/endpoint";
        String username = null;
        String password = null;
        Map<String, String> headers = this.extractHeaders(null);
        String payload = event.toString();

        try {
            executorService.submit(new HTTPSender(url, payload, username, password, headers, httpClient));
        } catch (RejectedExecutionException e) {
            log.error("Job queue is full : " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        // not required
    }

    @Override
    public void destroy() {
        // not required
    }

    @Override
    public boolean isPolled() {
        return false;
    }

    private void checkHTTPClientInit(Map<String, String> staticProperties) {
        if (httpClient != null) {
            return;
        }
        synchronized (HttpSink.class) {
            if (this.httpClient != null) {
                return;
            }
            httpClient = new HttpClient(connectionManager);
            String proxyHost = staticProperties.get(ADAPTER_PROXY_HOST);
            String proxyPort = staticProperties.get(ADAPTER_PROXY_PORT);
            if (proxyHost != null && proxyHost.trim().length() > 0) {
                try {
                    HttpHost host = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
                    this.httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
                } catch (NumberFormatException e) {
                    log.error("Invalid proxy port: " + proxyPort + ", "
                            + "ignoring proxy settings for HTTP output event adaptor...");
                }
            }

            // TODO: 1/8/17 fix properly
            // String messageFormat = eventAdapterConfiguration.getMessageFormat();
            String messageFormat = "json";
            if (messageFormat.equalsIgnoreCase("json")) {
                contentType = "application/json";
            } else if (messageFormat.equalsIgnoreCase("text")) {
                contentType = "text/plain";
            } else {
                contentType = "text/xml";
            }
        }
    }

    private Map<String, String> extractHeaders(String headers) {
        if (headers == null || headers.trim().length() == 0) {
            return null;
        }
        String[] entries = headers.split(HEADER_SEPARATOR);
        String[] keyValue;
        Map<String, String> result = new HashMap<String, String>();
        for (String header : entries) {
            try {
                keyValue = header.split(ENTRY_SEPARATOR, 2);
                result.put(keyValue[0].trim(), keyValue[1].trim());
            } catch (Exception e) {
                log.warn("Header property '" + header + "' is not defined in the correct format.", e);
            }
        }
        return result;
    }

    /**
     * This class represents a job to send an HTTP request to a target URL.
     */
    class HTTPSender implements Runnable {
        private String url;
        private String payload;
        private String username;
        private String password;
        private Map<String, String> headers;
        private HttpClient httpClient;

        public HTTPSender(String url, String payload, String username, String password,
                          Map<String, String> headers, HttpClient httpClient) {
            this.url = url;
            this.payload = payload;
            this.username = username;
            this.password = password;
            this.headers = headers;
            this.httpClient = httpClient;
        }

        public String getUrl() {
            return url;
        }

        public String getPayload() {
            return payload;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public HttpClient getHttpClient() {
            return httpClient;
        }

        public void run() {
            EntityEnclosingMethod method = null;
            try {
                // TODO: 1/8/17 fix properly
                //    if (clientMethod.equalsIgnoreCase(CONSTANT_HTTP_PUT)) {
                //        method = new PutMethod(this.getUrl());
                //    } else {
                //        method = new PostMethod(this.getUrl());
                //    }
                method = new PostMethod(this.getUrl());

                if (hostConfiguration == null) {
                    URL hostUrl = new URL(this.getUrl());
                    hostConfiguration = new HostConfiguration();
                    hostConfiguration.setHost(
                            hostUrl.getHost(),
                            hostUrl.getPort(),
                            hostUrl.getProtocol()
                    );
                }

                method.setRequestEntity(new StringRequestEntity(this.getPayload(), contentType, "UTF-8"));

                if (this.getUsername() != null && this.getUsername().trim().length() > 0) {
                    method.setRequestHeader(
                            "Authorization",
                            "Basic " + DatatypeConverter.printBase64Binary(
                                    (this.getUsername() + ENTRY_SEPARATOR + this.getPassword()).getBytes()
                            )
                    );
                }

                if (this.getHeaders() != null) {
                    for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
                        method.setRequestHeader(header.getKey(), header.getValue());
                    }
                }

                this.getHttpClient().executeMethod(hostConfiguration, method);

            } catch (UnknownHostException e) {
                log.error("Cannot connect to " + this.getUrl() + " : " + e.getMessage(), e);
            } catch (Throwable e) {
                log.error("Event dropped at Output Adapter : " + e.getMessage(), e);
            } finally {
                if (method != null) {
                    method.releaseConnection();
                }
            }
        }
    }
}