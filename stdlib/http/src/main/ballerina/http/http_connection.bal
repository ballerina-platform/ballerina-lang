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


documentation {
    The caller actions for responding to client requests.
}
public type Connection object {

    private ServiceEndpointConfiguration config;
    private FilterContext? filterContext;

    documentation {
        Sends the outbound response to the caller.

        P{{message}} The outbound response or any payload of type `string`, `xml`, `json`, `byte[]`, `io:ByteChannel`
                     or `mime:Entity[]`
        R{{}} Returns an `error` if failed to respond
    }
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

    documentation {
        Pushes a promise to the caller.

        P{{promise}} Push promise message
        R{{}} An `error` in case of failures
    }
    public extern function promise(PushPromise promise) returns error?;

    documentation {
        Sends a promised push response to the caller.

        P{{promise}} Push promise message
        P{{response}} The outbound response
        R{{}} An `error` in case of failures while responding with the promised response
    }
    public extern function pushPromisedResponse(PushPromise promise, Response response) returns error?;

    documentation {
        Sends an upgrade request with custom headers.

        P{{headers}} A `map` of custom headers for handshake
    }
    public extern function acceptWebSocketUpgrade(map headers) returns WebSocketListener;

    documentation {
        Cancels the handshake.

        P{{status}} Error Status code for cancelling the upgrade and closing the connection.
        This error status code need to be 4xx or 5xx else the default status code would be 400.
        P{{reason}} Reason for cancelling the upgrade
        R{{}} An `error` if an error occurs during cancelling the upgrade or nil
    }
    public extern function cancelWebSocketUpgrade(int status, string reason) returns error|();

    documentation {
        Sends a `100-continue` response to the caller.

        R{{}} Returns an `error` if failed to send the `100-continue` response
    }
    public function continue() returns error?;

    documentation {
        Sends a redirect response to the user with the specified redirection status code.

        P{{response}} Response to be sent to the caller
        P{{code}} The redirect status code to be sent
        P{{locations}} An array of URLs to which the caller can redirect to
        R{{}} Returns an `error` if failed to send the redirect response
    }
    public function redirect(Response response, RedirectCode code, string[] locations) returns error?;
};

extern function nativeRespond(Connection connection, Response response) returns error?;

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////
documentation {
    Defines the HTTP redirect codes as a type.
}
public type RedirectCode 300|301|302|303|304|305|307|308;

documentation { Represents the HTTP redirect status code `300 - Multiple Choices`. }
@final public RedirectCode REDIRECT_MULTIPLE_CHOICES_300 = 300;
documentation { Represents the HTTP redirect status code `301 - Moved Permanently`. }
@final public RedirectCode REDIRECT_MOVED_PERMANENTLY_301 = 301;
documentation { Represents the HTTP redirect status code `302 - Found`. }
@final public RedirectCode REDIRECT_FOUND_302 = 302;
documentation { Represents the HTTP redirect status code `303 - See Other`. }
@final public RedirectCode REDIRECT_SEE_OTHER_303 = 303;
documentation { Represents the HTTP redirect status code `304 - Not Modified`. }
@final public RedirectCode REDIRECT_NOT_MODIFIED_304 = 304;
documentation { Represents the HTTP redirect status code `305 - Use Proxy`. }
@final public RedirectCode REDIRECT_USE_PROXY_305 = 305;
documentation { Represents the HTTP redirect status code `307 - Temporary Redirect`. }
@final public RedirectCode REDIRECT_TEMPORARY_REDIRECT_307 = 307;
documentation { Represents the HTTP redirect status code `308 - Permanent Redirect`. }
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
