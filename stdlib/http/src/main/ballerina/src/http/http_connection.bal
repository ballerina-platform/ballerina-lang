// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerinax/java;

# The caller actions for responding to client requests.
#
# + remoteAddress - The remote address
# + localAddress - The local address
# + protocol - The protocol associated with the service endpoint
public type Caller client object {

    private ListenerConfiguration config = {};
    private FilterContext? filterContext = ();

    //TODO:Make these readonly
    public Remote remoteAddress = {};
    public Local localAddress = {};
    public string protocol = "";

    # Sends the outbound response to the caller.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - Returns an `http:ListenerError` if failed to respond
    public remote function respond(ResponseMessage message = ()) returns ListenerError? {
        Response response = buildResponse(message);
        FilterContext? filterContext = self.filterContext;
        (RequestFilter | ResponseFilter)[] filters = self.config.filters;
        int i = filters.length() - 1;
        if (filterContext is FilterContext) {
            while (i >= 0) {
                var filter = filters[i];
                if (filter is ResponseFilter && !filter.filterResponse(response, filterContext)) {
                    Response res = new;
                    res.statusCode = 500;
                    res.setTextPayload("Failure when invoking response filter/s");
                    return nativeRespond(self, res);
                }
                i -= 1;
            }
        }
        return nativeRespond(self, response);
    }

    # Pushes a promise to the caller.
    #
    # + promise - Push promise message
    # + return - An `http:ListenerError` in case of failures
    public remote function promise(PushPromise promise) returns ListenerError? {
        return externPromise(self, promise);
    }

    # Sends a promised push response to the caller.
    #
    # + promise - Push promise message
    # + response - The outbound response
    # + return - An `http:ListenerError` in case of failures while responding with the promised response
    public remote function pushPromisedResponse(PushPromise promise, Response response)
                                                                returns ListenerError? {
        return externPushPromisedResponse(self, promise, response);
    }

    # Sends an upgrade request with custom headers.
    #
    # + headers - A `map` of custom headers for handshake
    # + return - `WebSocketCaller` or error on failure to upgrade
    public remote function acceptWebSocketUpgrade(map<string> headers) 
                                                returns WebSocketCaller | WebSocketError {
        return externAcceptWebSocketUpgrade(self, headers);
    }

    # Cancels the handshake.
    #
    # + status - Error Status code for cancelling the upgrade and closing the connection.
    #            This error status code need to be 4xx or 5xx else the default status code would be 400.
    # + reason - Reason for cancelling the upgrade
    # + return - An `error` if an error occurs during cancelling the upgrade or nil
    public remote function cancelWebSocketUpgrade(int status, string reason) returns WebSocketError? {
        return externCancelWebSocketUpgrade(self, status, java:fromString(reason));
    }

    # Sends a `100-continue` response to the caller.
    #
    # + return - Returns an `http:ListenerError` if failed to send the `100-continue` response
    public remote function continue() returns ListenerError? {
        Response res = new;
        res.statusCode = STATUS_CONTINUE;
        return self->respond(res);
    }

    # Sends a redirect response to the user with the specified redirection status code.
    #
    # + response - Response to be sent to the caller
    # + code - The redirect status code to be sent
    # + locations - An array of URLs to which the caller can redirect to
    # + return - Returns an `http:ListenerError` if failed to send the redirect response
    public remote function redirect(Response response, RedirectCode code, string[] locations) returns ListenerError? {
        if (code == REDIRECT_MULTIPLE_CHOICES_300) {
            response.statusCode = STATUS_MULTIPLE_CHOICES;
        } else if (code == REDIRECT_MOVED_PERMANENTLY_301) {
            response.statusCode = STATUS_MOVED_PERMANENTLY;
        } else if (code == REDIRECT_FOUND_302) {
            response.statusCode = STATUS_FOUND;
        } else if (code == REDIRECT_SEE_OTHER_303) {
            response.statusCode = STATUS_SEE_OTHER;
        } else if (code == REDIRECT_NOT_MODIFIED_304) {
            response.statusCode = STATUS_NOT_MODIFIED;
        } else if (code == REDIRECT_USE_PROXY_305) {
            response.statusCode = STATUS_USE_PROXY;
        } else if (code == REDIRECT_TEMPORARY_REDIRECT_307) {
            response.statusCode = STATUS_TEMPORARY_REDIRECT;
        } else if (code == REDIRECT_PERMANENT_REDIRECT_308) {
            response.statusCode = STATUS_PERMANENT_REDIRECT;
        }
        string locationsStr = "";
        foreach var location in locations {
            locationsStr = locationsStr + location + ",";
        }
        locationsStr = locationsStr.substring(0, (locationsStr.length()) - 1);

        response.setHeader(LOCATION, locationsStr);
        return self->respond(response);
    }

    # Sends the outbound response to the caller with the status 200 OK.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`
    # + return - Returns an `http:ListenerError` if failed to respond
    public remote function ok(ResponseMessage message = ()) returns ListenerError? {
        Response response = buildResponse(message);
        response.statusCode = STATUS_OK;
        return self->respond(response);
    }

    # Sends the outbound response to the caller with the status 201 Created.
    #
    # + uri - Represents the most specific URI for the newly created resource
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`. This message is optional.
    # + return - Returns an `http:ListenerError` if failed to respond
    public remote function created(string uri, ResponseMessage message = ()) returns ListenerError? {
        Response response = buildResponse(message);
        response.statusCode = STATUS_CREATED;
        if (uri.length() > 0) {
            response.setHeader(LOCATION, uri);
        }
        return self->respond(response);
    }

    # Sends the outbound response to the caller with the status 202 Accepted.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ReadableByteChannel`
    #             or `mime:Entity[]`. This message is optional.
    # + return - Returns an `http:ListenerError` if failed to respond
    public remote function accepted(ResponseMessage message = ()) returns ListenerError? {
        Response response = buildResponse(message);
        response.statusCode = STATUS_ACCEPTED;
        return self->respond(response);
    }
};

function nativeRespond(Caller caller, Response response) returns ListenerError? = @java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.connection.Respond",
    name: "nativeRespond"
} external;


/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////
# Defines the HTTP redirect codes as a type.
public type RedirectCode REDIRECT_MULTIPLE_CHOICES_300|REDIRECT_MOVED_PERMANENTLY_301|REDIRECT_FOUND_302|REDIRECT_SEE_OTHER_303|
REDIRECT_NOT_MODIFIED_304|REDIRECT_USE_PROXY_305|REDIRECT_TEMPORARY_REDIRECT_307|REDIRECT_PERMANENT_REDIRECT_308;

# Represents the HTTP redirect status code `300 - Multiple Choices`.
public const REDIRECT_MULTIPLE_CHOICES_300 = 300;
# Represents the HTTP redirect status code `301 - Moved Permanently`.
public const REDIRECT_MOVED_PERMANENTLY_301 = 301;
# Represents the HTTP redirect status code `302 - Found`.
public const REDIRECT_FOUND_302 = 302;
# Represents the HTTP redirect status code `303 - See Other`.
public const REDIRECT_SEE_OTHER_303 = 303;
# Represents the HTTP redirect status code `304 - Not Modified`.
public const REDIRECT_NOT_MODIFIED_304 = 304;
# Represents the HTTP redirect status code `305 - Use Proxy`.
public const REDIRECT_USE_PROXY_305 = 305;
# Represents the HTTP redirect status code `307 - Temporary Redirect`.
public const REDIRECT_TEMPORARY_REDIRECT_307 = 307;
# Represents the HTTP redirect status code `308 - Permanent Redirect`.
public const REDIRECT_PERMANENT_REDIRECT_308 = 308;

function externPromise(Caller caller, PushPromise promise) returns ListenerError? =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.connection.Promise",
    name: "promise"
} external;

function externPushPromisedResponse(Caller caller, PushPromise promise, Response response) returns ListenerError? =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.connection.PushPromisedResponse",
    name: "pushPromisedResponse"
} external;

function externAcceptWebSocketUpgrade(Caller caller, map<string> headers) returns WebSocketCaller | WebSocketError =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.connection.AcceptWebSocketUpgrade",
    name: "acceptWebSocketUpgrade"
} external;

function externCancelWebSocketUpgrade(Caller caller, int status, handle reason) returns WebSocketError? =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.connection.CancelWebSocketUpgrade",
    name: "cancelWebSocketUpgrade",
    paramTypes: ["org.ballerinalang.jvm.values.ObjectValue", "long", "java.lang.String"]
} external;
