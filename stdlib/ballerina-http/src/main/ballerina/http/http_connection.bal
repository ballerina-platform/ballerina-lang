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

    documentation {
        Sends the outbound response to the caller.
     
        P{{response}} The outbound response
        R{{}} Returns an `HttpConnectorError` if failed to respond
    }
    public native function respond(Response response) returns HttpConnectorError?;

    documentation {
        Pushes a promise to the caller.
        
        P{{promise}} Push promise message
        R{{}} Returns an `HttpConnectorError` if failed to push the promise
    }
    public native function promise(PushPromise promise) returns HttpConnectorError?;

    documentation {
        Sends a promised push response to the caller.
        
        P{{promise}} Push promise message
        P{{response}} The outbound response
        R{{}} Returns an `HttpConnectorError` if failed to respond with the promised response
    }
    public native function pushPromisedResponse(PushPromise promise, Response response) returns HttpConnectorError?;

    documentation {
        Sends an upgrade request with custom headers.
          
        P{{headers}} A `map` of custom headers for handshake
    }
    public native function acceptWebSocketUpgrade(map headers) returns WebSocketListener;

    documentation {
        Cancels the handshake.
        
        P{{statusCode}} Status code for closing the connection
        P{{reason}} Reason for closing the connection
    }
    public native function cancelWebSocketUpgrade(int status, string reason);

    documentation {
        Sends a `100-continue` response to the caller.

        R{{}} Returns an `HttpConnectorError` if failed to send the `100-continue` response
    }
    public function continue() returns HttpConnectorError?;

    documentation {
        Sends a redirect response to the user with the specified redirection status code.

        P{{response}} Response to be sent to the caller
        P{{redirectCode}} The redirect status code to be sent
        P{{locations}} An array of URLs to which the caller can redirect to
        R{{}} Returns an `HttpConnectorError` if failed to send the redirect response
    }
    public function redirect(Response response, RedirectCode code, string[] locations) returns HttpConnectorError?;
};

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////
documentation {
    Status codes for HTTP redirect
    
    F{{MULTIPLE_CHOICES_300}} Represents status code 300 - Multiple Choices
    F{{MOVED_PERMANENTLY_301}} Represents status code 301 - Moved Permanently
    F{{FOUND_302}} Represents status code 302 - Found
    F{{SEE_OTHER_303}} Represents status code 303 - See Other
    F{{NOT_MODIFIED_304}} Represents status code 304 - Not Modified
    F{{USE_PROXY_305}} Represents status code 305 - Use Proxy
    F{{TEMPORARY_REDIRECT_307}} Represents status code 307 - Temporary Redirect
    F{{PERMANENT_REDIRECT_308}} Represents status code 308 - Permanent Redirect
}
public type RedirectCode 300 | 301 | 302 | 303 | 304 | 305 | 307 | 308;

@final public RedirectCode REDIRECT_MULTIPLE_CHOICES_300 = 300;
@final public RedirectCode REDIRECT_MOVED_PERMANENTLY_301 = 301;
@final public RedirectCode REDIRECT_FOUND_302 = 302;
@final public RedirectCode REDIRECT_SEE_OTHER_303 = 303;
@final public RedirectCode REDIRECT_NOT_MODIFIED_304 = 304;
@final public RedirectCode REDIRECT_USE_PROXY_305 = 305;
@final public RedirectCode REDIRECT_TEMPORARY_REDIRECT_307 = 307;
@final public RedirectCode REDIRECT_PERMANENT_REDIRECT_308 = 308;

public function Connection::continue() returns HttpConnectorError? {
    Response res = new;
    res.statusCode = CONTINUE_100;
    return self.respond(res);
}

public function Connection::redirect(Response response, RedirectCode code, string[] locations) returns HttpConnectorError? {
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
