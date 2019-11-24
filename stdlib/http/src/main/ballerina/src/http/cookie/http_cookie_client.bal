// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Provides cookie functionality across HTTP client actions.
#
# + url - Target service url
# + config - HTTP ClientConfiguration to be used for HTTP client invocation
# + cookieConfig - Configurations associated with cookies
# + httpClient - HTTP client for outbound HTTP requests
# + cookieStore - Store to keep cookies of the client
public type CookieClient object {

    public string url;
    public ClientConfiguration config;
    public CookieConfig cookieConfig;
    public HttpClient httpClient;
    public CookieStore cookieStore;

    # Creates a cookie client with the given configurations.
    #
    # + url - Target service url
    # + config - HTTP ClientConfiguration to be used for HTTP client invocation
    # + cookieConfig - Configurations associated with cookies
    # + httpClient - HTTP client for outbound HTTP requests
    # + cookieStore - Store to keep cookies of the client
     public function __init(string url, ClientConfiguration config, CookieConfig cookieConfig, HttpClient httpClient, CookieStore cookieStore) {
         self.url = url;
         self.config = config;
         self.cookieConfig = cookieConfig;
         self.httpClient = httpClient;
         self.cookieStore = cookieStore;
    }

    # The `CookieClient.get()` function wraps the underlying HTTP remote functions in a way to provide
    # cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public function get(string path, public RequestMessage message = ()) returns @tainted Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->get(path, message = request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.post()` function wraps the underlying HTTP remote functions in a way to provide
    # cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public function post(string path, RequestMessage message) returns Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->post(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.head()` function wraps the underlying HTTP remote functions in a way to provide
    # cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public function head(string path, public RequestMessage message = ()) returns @tainted Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->head(path, message = request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.put()` function wraps the underlying HTTP remote functions in a way to provide
    # cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public function put(string path, RequestMessage message) returns Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->put(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.forward()` function wraps the underlying HTTP remote functions in a way to provide
    # cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + request - An HTTP inbound request message
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public function forward(string path, Request request) returns Response|ClientError{
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->forward(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.execute()` function wraps the underlying HTTP remote functions in a way to provide
    # cookie functionality for a given endpoint.
    #
    # + httpVerb - HTTP verb value
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public function execute(string httpVerb, string path, RequestMessage message) returns Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->execute(httpVerb, path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.patch()` function wraps the underlying HTTP remote functions in a way to provide
    # cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public function patch(string path, RequestMessage message) returns Response|ClientError  {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse =  self.httpClient->patch(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.delete()` function wraps the underlying HTTP remote functions in a way to provide
    # cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public function delete(string path, public RequestMessage message = ()) returns Response|ClientError  {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse =  self.httpClient->delete(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.options()` function wraps the underlying HTTP remote functions in a way to provide
    # cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public function options(string path, public RequestMessage message = ()) returns Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse =  self.httpClient->options(path, message = request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
    # The `CookieClient.submit()` function does not give out a `Response` as the result,
    # rather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an `ClientError` if the submission fails
    public function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|ClientError {
        Request request = <Request>message;
        return self.httpClient->submit(httpVerb, path, request);
    }

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message, or an `ClientError` if the invocation fails
    public function getResponse(HttpFuture httpFuture) returns Response|ClientError {
        return self.httpClient->getResponse(httpFuture);
    }

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public function hasPromise(HttpFuture httpFuture) returns boolean {
        return self.httpClient->hasPromise(httpFuture);
    }

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an `ClientError` if the invocation fails
    public function getNextPromise(HttpFuture httpFuture) returns PushPromise|ClientError{
        return self.httpClient->getNextPromise(httpFuture);
    }

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an `ClientError` if the invocation fails
    public function getPromisedResponse(PushPromise promise) returns Response|ClientError {
        return self.httpClient->getPromisedResponse(promise);
    }

    # Rejects a `PushPromise`. When a `PushPromise` is rejected, there is no chance of fetching a promised
    # response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public function rejectPromise(PushPromise promise) {
        self.httpClient->rejectPromise(promise);
    }
};

// Gets the relevant cookies from the cookieStore and adds them to the request.
function addStoredCookiesToRequest(string url, string path, CookieStore cookieStore, Request request) {
    Cookie[] cookiesToSend = cookieStore.getCookies(url, path);
    if (cookiesToSend.length() != 0) {
        // Has requested to this url before and has stored cookies.
        request.addCookies(cookiesToSend);
    }
}

// Gets the cookies from the inbound response ,adds them to the cookies store and returns the response.
function addCookiesInResponseToStore(Response|ClientError inboundResponse, @tainted CookieStore cookieStore, CookieConfig cookieConfig, string url, string path) returns Response|ClientError {
    if (inboundResponse is Response) {
        Cookie[] cookiesInResponse = inboundResponse.getCookies();
        cookieStore.addCookies(cookiesInResponse, cookieConfig, url, path );
    }
    return inboundResponse;
}
