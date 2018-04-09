// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package ballerina.http;

// TODO: Document these. Should we make FORWARD a private constant?
@final public HttpOperation HTTP_FORWARD = "FORWARD";
@final public HttpOperation HTTP_GET = "GET";
@final public HttpOperation HTTP_POST = "POST";
@final public HttpOperation HTTP_DELETE = "DELETE";
@final public HttpOperation HTTP_OPTIONS = "OPTIONS";
@final public HttpOperation HTTP_PUT = "PUT";
@final public HttpOperation HTTP_PATCH = "PATCH";
@final public HttpOperation HTTP_HEAD = "HEAD";
@final public HttpOperation HTTP_NONE = "NONE";

public type HttpOperation "FORWARD" | "GET" | "POST" | "DELETE" | "OPTIONS" | "PUT" | "PATCH" | "HEAD" | "NONE";

// makes the actual endpoints call according to the http operation passed in.
public function invokeEndpoint (string path, Request outRequest,
                                HttpOperation requestAction, HttpClient httpClient) returns Response|HttpConnectorError {
    if (HTTP_GET == requestAction) {
        return httpClient.get(path, outRequest);
    } else if (HTTP_POST == requestAction) {
        return httpClient.post(path, outRequest);
    } else if (HTTP_OPTIONS == requestAction) {
        return httpClient.options(path, outRequest);
    } else if (HTTP_PUT == requestAction) {
        return httpClient.put(path, outRequest);
    } else if (HTTP_DELETE == requestAction) {
        return httpClient.delete(path, outRequest);
    } else if (HTTP_PATCH == requestAction) {
        return httpClient.patch(path, outRequest);
    } else if (HTTP_FORWARD == requestAction) {
        return httpClient.forward(path, outRequest);
    } else if (HTTP_HEAD == requestAction) {
        return httpClient.head(path, outRequest);
    } else {
        return getError();
    }
}

// Extracts HttpOperation from the Http verb passed in.
function extractHttpOperation (string httpVerb) returns HttpOperation {
    HttpOperation inferredConnectorAction = HTTP_NONE;
    if ("GET" == httpVerb) {
        inferredConnectorAction = HTTP_GET;
    } else if ("POST" == httpVerb) {
        inferredConnectorAction = HTTP_POST;
    } else if ("OPTIONS" == httpVerb) {
        inferredConnectorAction = HTTP_OPTIONS;
    } else if ("PUT" == httpVerb) {
        inferredConnectorAction = HTTP_PUT;
    } else if ("DELETE" == httpVerb) {
        inferredConnectorAction = HTTP_DELETE;
    } else if ("PATCH" == httpVerb) {
        inferredConnectorAction = HTTP_PATCH;
    } else if ("FORWARD" == httpVerb) {
        inferredConnectorAction = HTTP_FORWARD;
    } else if ("HEAD" == httpVerb) {
        inferredConnectorAction = HTTP_HEAD;
    }
    return inferredConnectorAction;
}

// Populate boolean index array by looking at the configured Http status codes to get better performance
// at runtime.
function populateErrorCodeIndex (int[] errorCode) returns boolean[] {
    boolean[] result = [];
    foreach i in errorCode {
        result[i] = true;
    }
    return result;
}

function createHttpClientArray (ClientEndpointConfig config) returns HttpClient[] {
    HttpClient[] httpClients = [];
    int i=0;
    boolean httpClientRequired = false;
    string uri = config.targets[0].url;
    var cbConfig = config.circuitBreaker;
    match cbConfig {
        CircuitBreakerConfig cb => {
            if (uri.hasSuffix("/")) {
                int lastIndex = uri.length() - 1;
                uri = uri.subString(0, lastIndex);
            }
            httpClientRequired = false;
        }
        () => {
            httpClientRequired = true;
        }
    }

    foreach target in config.targets {
        uri = target.url;
        if (uri.hasSuffix("/")) {
            int lastIndex = uri.length() - 1;
            uri = uri.subString(0, lastIndex);
        } 
        if (!httpClientRequired) {
            httpClients[i] = createCircuitBreakerClient(uri, config);
        } else {
            var retryConfig = config.retry;
            match retryConfig {
                Retry retry => {
                    httpClients[i] = createRetryClient(uri, config);
                }
                () => {
                    if (config.cache.enabled) {
                        httpClients[i] = createHttpCachingClient(uri, config, config.cache);
                    } else {
                        httpClients[i] = createHttpClient(uri, config);
                    }
                }
            }
        }
        httpClients[i].config = config;
        i = i+1;
    }
    return httpClients;
}

function getError() returns HttpConnectorError {
    HttpConnectorError httpConnectorError = {};
    httpConnectorError.statusCode = 400;
    httpConnectorError.message = "Unsupported connector action received.";
    return httpConnectorError;
}
