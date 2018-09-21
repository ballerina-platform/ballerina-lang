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


# The caller actions for responding to client requests.
public type Connection object {

    private ServiceEndpointConfiguration config;
    private FilterContext? filterContext;

    # Sends the outbound response to the caller.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - Returns an `error` if failed to respond
    public function respond(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message) returns error? {
        Response response = buildResponse(message);
        match filterContext {
            FilterContext filterCtx => {
                foreach filter in config.filters {
                    if (!filter.filterResponse(response, filterCtx)){
                        Response res;
                        res.statusCode = 500;
                        res.setTextPayload("Failure when invoking response filter/s");
                        return nativeRespond(self, res);
                    }
                }
            }
            () => {}
        }

        return nativeRespond(self, response);
    }

    # Pushes a promise to the caller.
    #
    # + promise - Push promise message
    # + return - An `error` in case of failures
    public extern function promise(PushPromise promise) returns error?;

    # Sends a promised push response to the caller.
    #
    # + promise - Push promise message
    # + response - The outbound response
    # + return - An `error` in case of failures while responding with the promised response
    public extern function pushPromisedResponse(PushPromise promise, Response response) returns error?;

    # Sends an upgrade request with custom headers.
    #
    # + headers - A `map` of custom headers for handshake
    # + return - WebSocket service endpoint
    public extern function acceptWebSocketUpgrade(map<string> headers) returns WebSocketListener;

    # Cancels the handshake.
    #
    # + status - Error Status code for cancelling the upgrade and closing the connection.
    #            This error status code need to be 4xx or 5xx else the default status code would be 400.
    # + reason - Reason for cancelling the upgrade
    # + return - An `error` if an error occurs during cancelling the upgrade or nil
    public extern function cancelWebSocketUpgrade(int status, string reason) returns error?;

    # Sends a `100-continue` response to the caller.
    #
    # + return - Returns an `error` if failed to send the `100-continue` response
    public function continue() returns error?;

    # Sends a redirect response to the user with the specified redirection status code.
    #
    # + response - Response to be sent to the caller
    # + code - The redirect status code to be sent
    # + locations - An array of URLs to which the caller can redirect to
    # + return - Returns an `error` if failed to send the redirect response
    public function redirect(Response response, RedirectCode code, string[] locations) returns error?;

    # Sends the outbound response to the caller with the status 200 OK.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`
    # + return - Returns an `error` if failed to respond
    public function ok(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message) returns error?;

    # Sends the outbound response to the caller with the status 201 Created.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`. This message is optional.
    # + return - Returns an `error` if failed to respond
    public function created(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message = ())
                                                                                            returns error?;

    # Sends the outbound response to the caller with the status 202 Accepted.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`. This message is optional.
    # + return - Returns an `error` if failed to respond
    public function accepted(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message = ())
                                                                                            returns error?;

    # Sends the outbound response to the caller with the status 204 No Content. If the given response contains a body
    # that will be removed.
    #
    # + message - Outbound response is optional
    # + return - Returns an `error` if failed to respond
    public function noContent(Response|() message = ()) returns error?;

    # Sends the outbound response to the caller with the status 400 Bad Request.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`. This message is optional.
    # + return - Returns an `error` if failed to respond
    public function badRequest(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message = ())
                                                                                                returns error?;

    # Sends the outbound response to the caller with the status 500 Internal Server Error.
    #
    # + message - The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
    #             or `mime:Entity[]`. This message is optional.
    # + return - Returns an `error` if failed to respond
    public function internalServerError(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message = ())
                                                                                                        returns error?;
};

extern function nativeRespond(Connection connection, Response response) returns error?;

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////
# Defines the HTTP redirect codes as a type.
public type RedirectCode 300|301|302|303|304|305|307|308;

# Represents the HTTP redirect status code `300 - Multiple Choices`.
@final public RedirectCode REDIRECT_MULTIPLE_CHOICES_300 = 300;
# Represents the HTTP redirect status code `301 - Moved Permanently`.
@final public RedirectCode REDIRECT_MOVED_PERMANENTLY_301 = 301;
# Represents the HTTP redirect status code `302 - Found`.
@final public RedirectCode REDIRECT_FOUND_302 = 302;
# Represents the HTTP redirect status code `303 - See Other`.
@final public RedirectCode REDIRECT_SEE_OTHER_303 = 303;
# Represents the HTTP redirect status code `304 - Not Modified`.
@final public RedirectCode REDIRECT_NOT_MODIFIED_304 = 304;
# Represents the HTTP redirect status code `305 - Use Proxy`.
@final public RedirectCode REDIRECT_USE_PROXY_305 = 305;
# Represents the HTTP redirect status code `307 - Temporary Redirect`.
@final public RedirectCode REDIRECT_TEMPORARY_REDIRECT_307 = 307;
# Represents the HTTP redirect status code `308 - Permanent Redirect`.
@final public RedirectCode REDIRECT_PERMANENT_REDIRECT_308 = 308;

function Connection::continue() returns error? {
    Response res = new;
    res.statusCode = CONTINUE_100;
    return self.respond(res);
}

function Connection::redirect(Response response, RedirectCode code, string[] locations) returns error? {
    if (code == REDIRECT_MULTIPLE_CHOICES_300) {
        response.statusCode = MULTIPLE_CHOICES_300;
    } else if (code == REDIRECT_MOVED_PERMANENTLY_301) {
        response.statusCode = MOVED_PERMANENTLY_301;
    } else if (code == REDIRECT_FOUND_302) {
        response.statusCode = FOUND_302;
    } else if (code == REDIRECT_SEE_OTHER_303) {
        response.statusCode = SEE_OTHER_303;
    } else if (code == REDIRECT_NOT_MODIFIED_304) {
        response.statusCode = NOT_MODIFIED_304;
    } else if (code == REDIRECT_USE_PROXY_305) {
        response.statusCode = USE_PROXY_305;
    } else if (code == REDIRECT_TEMPORARY_REDIRECT_307) {
        response.statusCode = TEMPORARY_REDIRECT_307;
    } else if (code == REDIRECT_PERMANENT_REDIRECT_308) {
        response.statusCode = PERMANENT_REDIRECT_308;
    }
    string locationsStr = "";
    foreach location in locations {
        locationsStr = locationsStr + location + ",";
    }
    locationsStr = locationsStr.substring(0, (lengthof locationsStr) - 1);

    response.setHeader(LOCATION, locationsStr);
    return self.respond(response);
}

function Connection::ok(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message) returns error? {
    Response response = buildResponse(message);
    response.statusCode = OK_200;
    return self.respond(response);
}

function Connection::created(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message = ())
                                                                                            returns error? {
    Response response = buildResponse(message);
    response.statusCode = CREATED_201;
    return self.respond(response);
}

function Connection::accepted(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message = ())
                                                                                            returns error? {
    Response response = buildResponse(message);
    response.statusCode = ACCEPTED_202;
    return self.respond(response);
}

function Connection::noContent(Response|() message = ()) returns error? {
    Response newResponse = new;
    match message {
        () => {}
        Response response => {newResponse = response;}
    }
    newResponse.statusCode = NO_CONTENT_204;
    return self.respond(newResponse);
}

function Connection::badRequest(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message = ())
                                                                                            returns error? {
    Response response = buildResponse(message);
    response.statusCode = BAD_REQUEST_400;
    return self.respond(response);
}

function Connection::internalServerError(Response|string|xml|json|byte[]|io:ByteChannel|mime:Entity[]|() message = ())
                                                                                            returns error? {
    Response response = buildResponse(message);
    response.statusCode = INTERNAL_SERVER_ERROR_500;
    return self.respond(response);
}
