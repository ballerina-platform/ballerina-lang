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

package ballerina.http;

import ballerina/io;
import ballerina/util;
import ballerina/runtime;

@Description {value:"An HTTP secure client for interacting with an HTTP server with authentication."}
public type HttpSecureClient object {
    //These properties are populated from the init call to the client connector as these were needed later stage
    //for retry and other few places.
    public {
        string serviceUri;
        ClientEndpointConfig config;
        HttpClient httpClient;
    }

    public new(serviceUri, config) {
        self.httpClient = createHttpClient(serviceUri, config);
    }

    @Description {value:"The POST action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function post(string path, Request req) returns (Response|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.post(path, req);
    }

    @Description {value:"The HEAD action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function head(string path, Request req) returns (Response|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.head(path, req);
    }

    @Description {value:"The PUT action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function put(string path, Request req) returns (Response|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.put(path, req);
    }

    @Description {value:"Invokes an HTTP call with the specified HTTP verb."}
    @Param {value:"httpVerb: HTTP verb value"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function execute(string httpVerb, string path, Request req) returns (Response|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.execute(httpVerb, path, req);
    }

    @Description {value:"The PATCH action implementation of the HTTP Connector."}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function patch(string path, Request req) returns (Response|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.patch(path, req);
    }

    @Description {value:"The DELETE action implementation of the HTTP connector"}
    @Param {value:"path: Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function delete(string path, Request req) returns (Response|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.delete(path, req);
    }

    @Description {value:"GET action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function get(string path, Request req) returns (Response|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.get(path, req);
    }

    @Description {value:"OPTIONS action implementation of the HTTP Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function options(string path, Request req) returns (Response|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.options(path, req);
    }

    @Description {value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP verb"}
    @Param {value:"path: Request path"}
    @Param {value:"req: An HTTP inbound request message"}
    @Return {value:"The inbound response message"}
    @Return {value:"Error occured during HTTP client invocation"}
    public function forward(string path, Request req) returns (Response|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.forward(path, req);
    }

    @Description {value:"Submits an HTTP request to a service with the specified HTTP verb."}
    @Param {value:"httpVerb: The HTTP verb value"}
    @Param {value:"path: The Resource path "}
    @Param {value:"req: An HTTP outbound request message"}
    @Return {value:"The Future for further interactions"}
    @Return {value:"The Error occured during HTTP client invocation"}
    public function submit(string httpVerb, string path, Request req) returns (HttpFuture|HttpConnectorError) {
        var details = prepareRequest(req, config);
        match details {
            () => {}
            HttpConnectorError err => return err;
        }
        return httpClient.submit(httpVerb, path, req);
    }

    @Description {value:"Retrieves response for a previously submitted request."}
    @Param {value:"httpFuture: The Future which relates to previous async invocation"}
    @Return {value:"The HTTP response message"}
    @Return {value:"The Error occured during HTTP client invocation"}
    public function getResponse (HttpFuture httpFuture) returns (Response|HttpConnectorError);

    @Description {value:"Checks whether server push exists for a previously submitted request."}
    @Param {value:"httpFuture: The Future which relates to previous async invocation"}
    @Return {value:"Whether push promise exists"}
    public function hasPromise (HttpFuture httpFuture) returns boolean;

    @Description {value:"Retrieves the next available push promise for a previously submitted request."}
    @Param {value:"httpFuture: The Future which relates to previous async invocation"}
    @Return {value:"The HTTP Push Promise message"}
    @Return {value:"The Error occured during HTTP client invocation"}
    public function getNextPromise (HttpFuture httpFuture) returns (PushPromise|HttpConnectorError);

    @Description {value:"Retrieves the promised server push response."}
    @Param {value:"promise: The related Push Promise message"}
    @Return {value:"HTTP The Push Response message"}
    @Return {value:"The Error occured during HTTP client invocation"}
    public function getPromisedResponse (PushPromise promise) returns (Response|HttpConnectorError);

    @Description {value:"Rejects a push promise."}
    @Param {value:"promise: The Push Promise need to be rejected"}
    public function rejectPromise (PushPromise promise);
};

@Description {value:"Creates an HTTP client capable of securing HTTP requests with authentication."}
public function createHttpSecureClient(string url, ClientEndpointConfig config) returns HttpClient {
    HttpSecureClient httpSecureClient = new(url, config);
    log:printDebug("Created HTTP secure client: " + io:sprintf("%r", [httpSecureClient]));
    return httpSecureClient;
}

@Description {value:"Prepare HTTP request with the required headers for authentication."}
@Param {value:"req: An HTTP outbound request message"}
@Param {value:"request:Client endpoint configurations"}
public function prepareRequest(Request req, ClientEndpointConfig config) returns (()|HttpConnectorError) {
    string scheme = config.authConfig.scheme;
    if (scheme == "basic"){
        string username = config.authConfig.username;
        string password = config.authConfig.password;
        var encodedStringVar = util:base64EncodeString(username + ":" + password);
        string encodedString;
        match encodedStringVar {
            string token => encodedString = token;
            util:Base64EncodeError err => {
                HttpConnectorError httpConnectorError = {};
                httpConnectorError.message = "Failed to encode the username or password with base64";
                return httpConnectorError;
            }
        }
        req.setHeader("Authorization", "Basic " + encodedString);
    } else if (scheme == "oauth"){
        if (config.authConfig.accessToken == "") {
            string refreshToken = config.authConfig.refreshToken;
            string clientId = config.authConfig.clientId;
            string clientSecret = config.authConfig.clientSecret;
            string refreshTokenUrl = config.authConfig.refreshTokenUrl;

            if (refreshToken != "" && clientId != "" && clientSecret != "") {
                var accessTokenValueResponse = getAccessTokenFromRefreshToken(config);
                match accessTokenValueResponse {
                    string accessTokenString => req.setHeader("Authorization", "Bearer " + accessTokenString);
                    HttpConnectorError err => return err;
                }
            } else {
                HttpConnectorError httpConnectorError = {};
                httpConnectorError.message = "Valid accessToken or refreshToken is not available to process the request";
                return httpConnectorError;
            }
        } else {
            req.setHeader("Authorization", "Bearer " + config.authConfig.accessToken);
        }
    } else if (scheme == "jwt"){
        string authToken = runtime:getInvocationContext().authenticationContext.authToken;
        req.setHeader("Authorization", "Bearer " + authToken);
    }
    return ();
}

@Description {value:"Request an access token from authorization server using the provided refresh token"}
@Param {value:"request:Client endpoint configurations"}
@Return {value:"AccessToken received from the authorization server"}
@Return {value:"Error occured during HTTP client invocation"}
function getAccessTokenFromRefreshToken(ClientEndpointConfig config) returns (string|HttpConnectorError) {
    string refreshToken = config.authConfig.refreshToken;
    string clientId = config.authConfig.clientId;
    string clientSecret = config.authConfig.clientSecret;
    string refreshTokenUrl = config.authConfig.refreshTokenUrl;
    HttpClient refreshTokenClient = createHttpClient(refreshTokenUrl, {});
    string refreshTokenRequestPath = "/oauth2/v3/token";
    string requestParams = "refresh_token=" + refreshToken + "&grant_type=refresh_token&client_secret=" + clientSecret + "&client_id=" + clientId;
    string base64ClientIdSecret;
    string clientIdSecret = clientId + ":" + clientSecret;
    match (util:base64EncodeString(clientIdSecret)){
        string encodeString => base64ClientIdSecret = encodeString;
        util:Base64EncodeError err => {
            HttpConnectorError httpConnectorError = {};
            httpConnectorError.message = err.message;
            return httpConnectorError;
        }
    }
    Request refreshTokenRequest;
    refreshTokenRequest.addHeader("Authorization", "Basic " + base64ClientIdSecret);
    refreshTokenRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
    refreshTokenRequest.setStringPayload("grant_type=refresh_token&refresh_token=" + refreshToken);
    refreshTokenRequest.setStringPayload(requestParams);
    refreshTokenRequestPath = refreshTokenRequestPath + "?" + requestParams;
    var refreshTokenResponse = refreshTokenClient.post(refreshTokenRequestPath, refreshTokenRequest);
    Response tokenResponse;
    match refreshTokenResponse {
        Response httpResponse => tokenResponse = httpResponse;
        HttpConnectorError err => return err;
    }
    var requestAccessTokenJson = tokenResponse.getJsonPayload();
    json generatedToken = check requestAccessTokenJson;

    if (tokenResponse.statusCode == 200) {
        return generatedToken.access_token.toString() but { () => "" };
    }
    return "";
}

