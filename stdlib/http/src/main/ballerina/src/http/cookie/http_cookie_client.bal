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

# Provides the cookie functionality across HTTP client actions.
#
# + url - Target service URL
# + config - HTTP Client Configuration to be used for the HTTP client invocation
# + cookieConfig - Configurations associated with the cookies
# + httpClient - HTTP client for outbound HTTP requests
# + cookieStore - Stores the cookies of the client
public client class CookieClient {

    public string url;
    public ClientConfiguration config;
    public CookieConfig cookieConfig;
    public HttpClient httpClient;
    public CookieStore? cookieStore = ();

    # Creates a cookie client with the given configurations.
    #
    # + url - Target service URL
    # + config - HTTP Client Configuration to be used for the HTTP client invocation
    # + cookieConfig - Configurations associated with the cookies
    # + httpClient - HTTP client for outbound HTTP requests
    # + cookieStore - Stores the cookies of the client
     public function init(string url, ClientConfiguration config, CookieConfig cookieConfig, HttpClient httpClient, CookieStore? cookieStore) {
         self.url = url;
         self.config = config;
         self.cookieConfig = cookieConfig;
         self.httpClient = httpClient;
         if (cookieStore is CookieStore) {
             self.cookieStore = cookieStore;
         }
    }

    # The `CookieClient.get()` function wraps the underlying HTTP remote functions in a way to provide
    # the cookie functionality for a given endpoint.
    #
    # + path - Request path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function get(string path, RequestMessage message = ()) returns @tainted Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->get(path, message = request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.post()` function wraps the underlying HTTP remote functions in a way to provide
    # the cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function post(string path, RequestMessage message) returns Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->post(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.head()` function wraps the underlying HTTP remote functions in a way to provide
    # the cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function head(string path, RequestMessage message = ()) returns @tainted Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->head(path, message = request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.put()` function wraps the underlying HTTP remote functions in a way to provide
    # the cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function put(string path, RequestMessage message) returns Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->put(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.forward()` function wraps the underlying HTTP remote functions in a way to provide
    # the cookie functionality for a given endpoint.
    #
    # + path - Request path
    # + request - An HTTP inbound request message
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function forward(string path, Request request) returns Response|ClientError{
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->forward(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.execute()` function wraps the underlying HTTP remote functions in a way to provide
    # the cookie functionality for a given endpoint.
    #
    # + httpVerb - HTTP verb value
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function execute(string httpVerb, string path, RequestMessage message) returns Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse = self.httpClient->execute(httpVerb, path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.patch()` function wraps the underlying HTTP remote functions in a way to provide
    # the cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function patch(string path, RequestMessage message) returns Response|ClientError  {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse =  self.httpClient->patch(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.delete()` function wraps the underlying HTTP remote functions in a way to provide
    # the cookie functionality for a given endpoint.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function delete(string path, RequestMessage message = ()) returns Response|ClientError  {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse =  self.httpClient->delete(path, request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # The `CookieClient.options()` function wraps the underlying HTTP remote functions in a way to provide
    # the cookie functionality for a given endpoint.
    #
    # + path - Request path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
    public remote function options(string path, RequestMessage message = ()) returns Response|ClientError {
        Request request = <Request>message;
        addStoredCookiesToRequest(self.url, path, self.cookieStore, request);
        var inboundResponse =  self.httpClient->options(path, message = request);
        return addCookiesInResponseToStore(inboundResponse, self.cookieStore, self.cookieConfig, self.url, path);
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
    # The `CookieClient.submit()` function does not produce a `Response` as the result.
    # Rather, it returns an `HttpFuture`, which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture`, which represents an asynchronous service invocation or else an `http:ClientError` if the submission fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|ClientError {
        Request request = <Request>message;
        return self.httpClient->submit(httpVerb, path, request);
    }

    # Retrieves the `http:Response` for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message or else an `http:ClientError` if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|ClientError {
        return self.httpClient->getResponse(httpFuture);
    }

    # Checks whether an `http:PushPromise` exists for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - A `boolean`, which represents whether an `http:PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns boolean {
        return self.httpClient->hasPromise(httpFuture);
    }

    # Retrieves the next available `http:PushPromise` for a previously-submitted request.
    #
    # + httpFuture - The `http:HttpFuture` related to a previous asynchronous invocation
    # + return - An HTTP Push Promise message or else an `http:ClientError` if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|ClientError{
        return self.httpClient->getNextPromise(httpFuture);
    }

    # Retrieves the promised server push `http:Response` message.
    #
    # + promise - The related `http:PushPromise`
    # + return - A promised HTTP `http:Response` message or else an `http:ClientError` if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|ClientError {
        return self.httpClient->getPromisedResponse(promise);
    }

    # Rejects an `http:PushPromise`. When an `http:PushPromise` is rejected, there is no chance of fetching a promised
    # response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise) {
        self.httpClient->rejectPromise(promise);
    }
}

// Gets the relevant cookies from the cookieStore and adds them to the request.
function addStoredCookiesToRequest(string url, string path, CookieStore? cookieStore, Request request) {
    Cookie[] cookiesToSend = [];
    if (cookieStore is CookieStore) {
        cookiesToSend = cookieStore.getCookies(url, path);
    }
    if (cookiesToSend.length() != 0) {
        // The client has requested to this url before and has stored cookies.
        request.addCookies(cookiesToSend);
    }
}

// Gets the cookies from the inbound response, adds them to the cookies store, and returns the response.
function addCookiesInResponseToStore(Response|ClientError inboundResponse, @tainted CookieStore? cookieStore, CookieConfig cookieConfig, string url, string path) returns Response|ClientError {
    if (cookieStore is CookieStore && inboundResponse is Response) {
        Cookie[] cookiesInResponse = inboundResponse.getCookies();
        cookieStore.addCookies(cookiesInResponse, cookieConfig, url, path );
    }
    return inboundResponse;
}
