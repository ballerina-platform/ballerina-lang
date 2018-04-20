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
    Represents HTTP connection which can be used to comminicate either with client or with other service.
}
public type Connection object {

    @Description {value:"Sends outbound response to the caller"}
    @Param {value:"conn: The server connector connection"}
    @Param {value:"res: The outbound response message"}
    @Return {value:"Error occured during HTTP server connector respond"}
    @Return {value:"Returns null if any error does not exist."}
    public native function respond(Response res) returns HttpConnectorError|();

    @Description { value:"Pushes a promise to the caller."}
    @Param { value:"conn: The server connector connection" }
    @Param { value:"promise: Push promise message" }
    @Return { value:"Error occured during HTTP server connector promise function invocation" }
    @Return {value:"Returns null if any error does not exist."}
    public native function promise(PushPromise promise) returns HttpConnectorError|();

    @Description { value:"Sends a promised push response to the caller."}
    @Param { value:"conn: The server connector connection" }
    @Param { value:"promise: Push promise message" }
    @Param { value:"res: The outbound response message" }
    @Return { value:"Error occured during HTTP server connector pushPromisedResponse function invocation" }
    @Return {value:"Returns null if any error does not exist."}
    public native function pushPromisedResponse(PushPromise promise, Response res) returns HttpConnectorError|();

    @Description {value:"Sends a upgrade request with custom headers"}
    @Param {value:"headers: a map of custom headers for handshake."}
    public native function acceptWebSocketUpgrade(map headers) returns WebSocketListener;

    @Description {value:"Cancels the handshake"}
    @Param {value:"statusCode: Status code for closing the connection"}
    @Param {value:"reason: Reason for closing the connection"}
    public native function cancelWebSocketUpgrade(int status, string reason);

    public function continue() returns HttpConnectorError|();

    public function redirect(Response response, RedirectCode code, string[] locations) returns HttpConnectorError|();
};

/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////
@Description { value:"Status codes for HTTP redirect"}
@Field { value:"MULTIPLE_CHOICES_300: Represents status code 300 - Multiple Choices."}
@Field { value:"MOVED_PERMANENTLY_301: Represents status code 301 - Moved Permanently."}
@Field { value:"FOUND_302: Represents status code 302 - Found."}
@Field { value:"SEE_OTHER_303: Represents status code 303 - See Other."}
@Field { value:"NOT_MODIFIED_304: Represents status code 304 - Not Modified."}
@Field { value:"USE_PROXY_305: Represents status code 305 - Use Proxy."}
@Field { value:"TEMPORARY_REDIRECT_307: Represents status code 307 - Temporary Redirect."}
@Field { value:"PERMANENT_REDIRECT_308: Represents status code 308 - Permanent Redirect."}
public type RedirectCode 300 | 301 | 302 | 303 | 304 | 305 | 307 | 308;

@final public RedirectCode REDIRECT_MULTIPLE_CHOICES_300 = 300;
@final public RedirectCode REDIRECT_MOVED_PERMANENTLY_301 = 301;
@final public RedirectCode REDIRECT_FOUND_302 = 302;
@final public RedirectCode REDIRECT_SEE_OTHER_303 = 303;
@final public RedirectCode REDIRECT_NOT_MODIFIED_304 = 304;
@final public RedirectCode REDIRECT_USE_PROXY_305 = 305;
@final public RedirectCode REDIRECT_TEMPORARY_REDIRECT_307 = 307;
@final public RedirectCode REDIRECT_PERMANENT_REDIRECT_308 = 308;

@Description { value:"Sends a 100-continue response to the client."}
@Param { value:"conn: The server connector connection" }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
@Return {value:"Returns null if any error does not exist."}
public function Connection::continue() returns HttpConnectorError|() {
    Response res = new;
    res.statusCode = CONTINUE_100;
    return self.respond(res);
}

@Description { value:"Sends a redirect response to the user with given redirection status code." }
@Param { value:"conn: The server connector connection" }
@Param { value:"response: Response to be sent to client." }
@Param { value:"redirectCode: Status code of the specific redirect." }
@Param { value:"locations: Array of locations where the redirection can happen." }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
@Return { value:"Returns null if any error does not exist." }
public function Connection::redirect(Response response, RedirectCode code, string[] locations) returns HttpConnectorError|() {
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
    locationsStr = locationsStr.subString(0, (lengthof locationsStr) - 1);

    response.setHeader(LOCATION, locationsStr);
    return self.respond(response);
}
