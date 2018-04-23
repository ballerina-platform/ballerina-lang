// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// under the License.package http;


import ballerina/io;
import ballerina/runtime;
import ballerina/mime;

@final string EMPTY_STRING = "";
@final string WHITE_SPACE = " ";
@final string COLON = ":";
@final string CONTENT_TYPE_HEADER = "Content-Type";
@final string BASIC_SCHEME = "basic";
@final string OAUTH_SCHEME = "oauth";
@final string JWT_SCHEME = "jwt";

@Description {value:"An HTTP secure client for interacting with an HTTP server with authentication."}
public type HttpSecureClient object {
    //These properties are populated from the init call to the client connector as these were needed later stage
    //for retry and other few places.
    public {
        string serviceUri;
        ClientEndpointConfig config;
        CallerActions httpClient;
    }

    public new(serviceUri, config) {
        self.httpClient = createSimpleHttpClient(serviceUri, config);
    }

    @Description {value:"The POST action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function post(string path, Request? request = ()) returns (Response|HttpConnectorError) {
        Request req = request ?: new;
        check generateSecureRequest(req, config);
        Request newOutRequest = check cloneRequest(req);
        Response response = check httpClient.post(path, request = req);
        boolean isRetry = isRetryRequired(response, config);
        if (isRetry) {
            check updateRequestAndConfig(newOutRequest, config);
            return httpClient.post(path, request = newOutRequest);
        }
        return response;
    }

    @Description {value:"The HEAD action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function head(string path, Request? request = ()) returns (Response|HttpConnectorError) {
        Request req = request ?: new;
        check generateSecureRequest(req, config);
        Request newOutRequest = check cloneRequest(req);
        Response response = check httpClient.head(path, request = req);
        boolean isRetry = isRetryRequired(response, config);
        if (isRetry) {
            check updateRequestAndConfig(newOutRequest, config);
            return httpClient.head(path, request = newOutRequest);
        }
        return response;
    }

    @Description {value:"The PUT action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function put(string path, Request? request = ()) returns (Response|HttpConnectorError) {
        Request req = request ?: new;
        check generateSecureRequest(req, config);
        Request newOutRequest = check cloneRequest(req);
        Response response = check httpClient.put(path, request = req);
        boolean isRetry = isRetryRequired(response, config);
        if (isRetry) {
            check updateRequestAndConfig(newOutRequest, config);
            return httpClient.put(path, request = newOutRequest);
        }
        return response;
    }

    @Description {value:"Invokes an HTTP call with the specified HTTP verb."}
    @Param {value:"httpVerb: HTTP verb value"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function execute(string httpVerb, string path, Request request) returns (Response|HttpConnectorError) {
        var details = generateSecureRequest(request, config);
        check generateSecureRequest(request, config);
        Request newOutRequest = check cloneRequest(request);
        Response response = check httpClient.execute(httpVerb, path, request);
        boolean isRetry = isRetryRequired(response, config);
        if (isRetry) {
            check updateRequestAndConfig(newOutRequest, config);
            return httpClient.execute(httpVerb, path, newOutRequest);
        }
        return response;
    }

    @Description {value:"The PATCH action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function patch(string path, Request? request = ()) returns (Response|HttpConnectorError) {
        Request req = request ?: new;
        check generateSecureRequest(req, config);
        Request newOutRequest = check cloneRequest(req);
        Response response = check httpClient.patch(path, request = req);
        boolean isRetry = isRetryRequired(response, config);
        if (isRetry) {
            check updateRequestAndConfig(newOutRequest, config);
            return httpClient.patch(path, request = newOutRequest);
        }
        return response;
    }

    @Description {value:"The DELETE action implementation of the HTTP connector"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function delete(string path, Request? request = ()) returns (Response|HttpConnectorError) {
        Request req = request ?: new;
        check generateSecureRequest(req, config);
        Request newOutRequest = check cloneRequest(req);
        Response response = check httpClient.delete(path, request = req);
        boolean isRetry = isRetryRequired(response, config);
        if (isRetry) {
            check updateRequestAndConfig(newOutRequest, config);
            return httpClient.delete(path, request = newOutRequest);
        }
        return response;
    }

    @Description {value:"GET action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function get(string path, Request? request = ()) returns (Response|HttpConnectorError) {
        Request req = request ?: new;
        check generateSecureRequest(req, config);
        Request newOutRequest = check cloneRequest(req);
        Response response = check httpClient.get(path, request = req);
        boolean isRetry = isRetryRequired(response, config);
        if (isRetry) {
            check updateRequestAndConfig(newOutRequest, config);
            return httpClient.get(path, request = newOutRequest);
        }
        return response;
    }

    @Description {value:"OPTIONS action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function options(string path, Request? request = ()) returns (Response|HttpConnectorError) {
        Request req = request ?: new;
        check generateSecureRequest(req, config);
        Request newOutRequest = check cloneRequest(req);
        Response response = check httpClient.options(path, request = req);
        boolean isRetry = isRetryRequired(response, config);
        if (isRetry) {
            check updateRequestAndConfig(newOutRequest, config);
            return httpClient.options(path, request = newOutRequest);
        }
        return response;
    }

    @Description {value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP verb"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP inbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function forward(string path, Request request) returns (Response|HttpConnectorError) {
        check generateSecureRequest(request, config);
        Request newOutRequest = check cloneRequest(request);
        Response response = check httpClient.forward(path, request);
        boolean isRetry = isRetryRequired(response, config);
        if (isRetry) {
            check updateRequestAndConfig(newOutRequest, config);
            return httpClient.forward(path, newOutRequest);
        }
        return response;
    }

    @Description {value:"Submits an HTTP request to a service with the specified HTTP verb."}
    @Param {value:"httpVerb: The HTTP verb value"}
    @Param {value:"path: The Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The Future for further interactions"}
    @Return {value:"The Error occured during HTTP client invocation"}
    public function submit(string httpVerb, string path, Request request) returns (HttpFuture|HttpConnectorError) {
        check generateSecureRequest(request, config);
        return httpClient.submit(httpVerb, path, request);
    }

    @Description {value:"Retrieves response for a previously submitted request."}
    @Param {value:"httpFuture: The Future which relates to previous async invocation"}
    @Return {value:"The HTTP response message"}
    @Return {value:"The Error occured during HTTP client invocation"}
    public function getResponse(HttpFuture httpFuture) returns (Response|HttpConnectorError) {
        return httpClient.getResponse(httpFuture);
    }

    @Description {value:"Checks whether server push exists for a previously submitted request."}
    @Param {value:"httpFuture: The Future which relates to previous async invocation"}
    @Return {value:"Whether push promise exists"}
    public function hasPromise(HttpFuture httpFuture) returns boolean {
        return httpClient.hasPromise(httpFuture);
    }

    @Description {value:"Retrieves the next available push promise for a previously submitted request."}
    @Param {value:"httpFuture: The Future which relates to previous async invocation"}
    @Return {value:"The HTTP Push Promise message"}
    @Return {value:"The Error occured during HTTP client invocation"}
    public function getNextPromise(HttpFuture httpFuture) returns (PushPromise|HttpConnectorError) {
        return httpClient.getNextPromise(httpFuture);
    }

    @Description {value:"Retrieves the promised server push response."}
    @Param {value:"promise: The related Push Promise message"}
    @Return {value:"HTTP The Push Response message"}
    @Return {value:"The Error occured during HTTP client invocation"}
    public function getPromisedResponse(PushPromise promise) returns (Response|HttpConnectorError) {
        return httpClient.getPromisedResponse(promise);
    }

    @Description {value:"Rejects a push promise."}
    @Param {value:"promise: The Push Promise need to be rejected"}
    public function rejectPromise(PushPromise promise) {
        return httpClient.rejectPromise(promise);
    }
};

@Description {value:"Creates an HTTP client capable of securing HTTP requests with authentication."}
@Param {value:"url: Base url"}
@Param {value:"config: Client endpoint configurations"}
@Return {value:"Created secure HTTP client"}
public function createHttpSecureClient(string url, ClientEndpointConfig config) returns CallerActions {
    match config.auth {
        AuthConfig => {
            HttpSecureClient httpSecureClient = new(url, config);
            return httpSecureClient;
        }
        () => {
            CallerActions httpClient = createSimpleHttpClient(url, config);
            return httpClient;
        }
    }
}

@Description {value:"Prepare HTTP request with the required headers for authentication."}
@Param {value:"req: An HTTP outbound request message"}
@Param {value:"config: Client endpoint configurations"}
@Return {value:"The Error occured during HTTP client invocation"}
function generateSecureRequest(Request req, ClientEndpointConfig config) returns (()|HttpConnectorError) {
    string scheme = config.auth.scheme but { () => EMPTY_STRING };
    if (scheme == BASIC_SCHEME) {
        string username = config.auth.username but { () => EMPTY_STRING };
        string password = config.auth.password but { () => EMPTY_STRING };
        string str = username + COLON + password;
        string token = check str.base64Encode();
        req.setHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE + token);
    } else if (scheme == OAUTH_SCHEME) {
        string accessToken = config.auth.accessToken but { () => EMPTY_STRING };
        if (accessToken == EMPTY_STRING) {
            string refreshToken = config.auth.refreshToken but { () => EMPTY_STRING };
            string clientId = config.auth.clientId but { () => EMPTY_STRING };
            string clientSecret = config.auth.clientSecret but { () => EMPTY_STRING };
            string refreshUrl = config.auth.refreshUrl but { () => EMPTY_STRING };

            if (refreshToken != EMPTY_STRING && clientId != EMPTY_STRING && clientSecret != EMPTY_STRING) {
                return updateRequestAndConfig(req, config);
            } else {
                HttpConnectorError httpConnectorError = {};
                httpConnectorError.message = "Valid accessToken or refreshToken is not available to process the request";
                return httpConnectorError;
            }
        } else {
            req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
        }
    } else if (scheme == JWT_SCHEME){
        string authToken = runtime:getInvocationContext().authContext.authToken;
        req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + authToken);
    }
    return ();
}

@Description {value:"Update request and client config with new access tokens retrieved."}
@Return {value:"req: Request object to be updated"}
@Param {value:"config: Client endpoint configurations"}
@Return {value:"Error occured during HTTP client invocation"}
function updateRequestAndConfig(Request req, ClientEndpointConfig config) returns (()|HttpConnectorError) {
    string accessToken = check getAccessTokenFromRefreshToken(config);
    req.setHeader(AUTH_HEADER, AUTH_SCHEME_BEARER + WHITE_SPACE + accessToken);
    AuthConfig? authConfig = config.auth;
    match authConfig {
        () => {}
        AuthConfig ac => ac.accessToken = accessToken;
    }
    return ();
}

@Description {value:"Request an access token from authorization server using the provided refresh token."}
@Param {value:"config: Client endpoint configurations"}
@Return {value:"AccessToken received from the authorization server"}
@Return {value:"Error occured during HTTP client invocation"}
function getAccessTokenFromRefreshToken(ClientEndpointConfig config) returns (string|HttpConnectorError) {
    string refreshToken = config.auth.refreshToken but { () => EMPTY_STRING };
    string clientId = config.auth.clientId but { () => EMPTY_STRING };
    string clientSecret = config.auth.clientSecret but { () => EMPTY_STRING };
    string refreshUrl = config.auth.refreshUrl but { () => EMPTY_STRING };

    CallerActions refreshTokenClient = createHttpSecureClient(refreshUrl, {});

    string clientIdSecret = clientId + COLON + clientSecret;
    string base64ClientIdSecret = check clientIdSecret.base64Encode();

    Request refreshTokenRequest = new;
    refreshTokenRequest.addHeader(AUTH_HEADER, AUTH_SCHEME_BASIC + WHITE_SPACE + base64ClientIdSecret);
    refreshTokenRequest.setStringPayload("grant_type=refresh_token&refresh_token=" + refreshToken,
        contentType = mime:APPLICATION_FORM_URLENCODED);
    Response refreshTokenResponse = check refreshTokenClient.post(EMPTY_STRING, request = refreshTokenRequest);

    json generatedToken = check refreshTokenResponse.getJsonPayload();
    if (refreshTokenResponse.statusCode == OK_200) {
        return generatedToken.access_token.toString();
    } else {
        HttpConnectorError httpConnectorError = {};
        httpConnectorError.message = "Failed to generate new access token from the given refresh token";
        return httpConnectorError;
    }
}

@Description {value:"Clone the given request into a new request with request entity."}
@Param {value:"req: Request object to be cloned"}
@Return {value:"New request object created"}
function cloneRequest(Request req) returns (Request|HttpConnectorError) {
    mime:Entity mimeEntity = check req.getEntity();
    Request newOutRequest = new;
    newOutRequest.setEntity(mimeEntity);
    return newOutRequest;
}

@Description {value:"Check whether retry is required for the response. This returns true if the scheme is OAuth and
the response status is 401 only. That implies user has given a expired access token and the client should update it with
the given refresh url."}
@Param {value:"response: Response object"}
@Param {value:"config: Client endpoint configurations"}
@Return {value:"Whether the client should retry or not"}
function isRetryRequired(Response response, ClientEndpointConfig config) returns boolean {
    string scheme = config.auth.scheme but { () => EMPTY_STRING };
    if (scheme == OAUTH_SCHEME && response.statusCode == UNAUTHORIZED_401) {
        return true;
    }
    return false;
}
