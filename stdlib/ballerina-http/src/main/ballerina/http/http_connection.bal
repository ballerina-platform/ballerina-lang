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

package ballerina.http;

documentation {
    Represents HTTP connection which can be used to comminicate either with client or with other service.
}
public type Connection object {

    @Description {value:"Sends outbound response to the caller"}
    @Param {value:"conn: The server connector connection"}
    @Param {value:"res: The outbound response message"}
    @Return {value:"Error occured during HTTP server connector respond"}
    @Return {value:"Returns null if any error does not exist."}
    public native function respond(Response res) returns (HttpConnectorError | ());

    @Description {value:"Forwards inbound response to the caller"}
    @Param {value:"conn: The server connector connection"}
    @Param {value:"res: The inbound response message"}
    @Return {value:"Error occured during HTTP server connector forward"}
    @Return {value:"Returns null if any error does not exist."}
    public native function forward(Response res) returns (HttpConnectorError | ());

    @Description { value:"Sends a push promise to the caller."}
    @Param { value:"conn: The server connector connection" }
    @Param { value:"promise: Push promise message" }
    @Return { value:"Error occured during HTTP server connector forward" }
    @Return {value:"Returns null if any error does not exist."}
    public native function promise(PushPromise promise) returns (HttpConnectorError | ());

    @Description { value:"Sends a promised push response to the caller."}
    @Param { value:"conn: The server connector connection" }
    @Param { value:"promise: Push promise message" }
    @Param { value:"res: The outbound response message" }
    @Return { value:"Error occured during HTTP server connector forward" }
    @Return {value:"Returns null if any error does not exist."}
    public native function pushPromisedResponse(PushPromise promise, Response res) returns (HttpConnectorError | ());

    @Description {value:"Sends a upgrade request with custom headers"}
    @Param {value:"headers: a map of custom headers for handshake."}
    public native function upgradeToWebSocket(map headers) returns WebSocketEndpoint;

    @Description {value:"Cancels the handshake"}
    @Param {value:"statusCode: Status code for closing the connection"}
    @Param {value:"reason: Reason for closing the connection"}
    public native function cancelUpgradeToWebSocket(int status, string reason);

    public function respondContinue() returns (HttpConnectorError | ());

    public function redirect(Response response, RedirectCode code, string[] locations) returns (HttpConnectorError | ());
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

public type MULTIPLE_CHOICES_300 "MULTIPLE_CHOICES_300";
public type MOVED_PERMANENTLY_301 "MOVED_PERMANENTLY_301";
public type FOUND_302 "FOUND_302";
public type SEE_OTHER_303 "SEE_OTHER_303";
public type NOT_MODIFIED_304 "NOT_MODIFIED_304";
public type USE_PROXY_305 "USE_PROXY_305";
public type TEMPORARY_REDIRECT_307 "TEMPORARY_REDIRECT_307";

public type RedirectCode MULTIPLE_CHOICES_300|MOVED_PERMANENTLY_301|FOUND_302|SEE_OTHER_303|NOT_MODIFIED_304
    |USE_PROXY_305|TEMPORARY_REDIRECT_307;

@Description { value:"Sends a 100-continue response to the client."}
@Param { value:"conn: The server connector connection" }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
@Return {value:"Returns null if any error does not exist."}
public function Connection::respondContinue () returns (HttpConnectorError | ()) {
    Response res;
    res.statusCode = 100;
    return self.respond(res);
}

@Description { value:"Sends a redirect response to the user with given redirection status code." }
@Param { value:"conn: The server connector connection" }
@Param { value:"response: Response to be sent to client." }
@Param { value:"redirectCode: Status code of the specific redirect." }
@Param { value:"locations: Array of locations where the redirection can happen." }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
@Return { value:"Returns null if any error does not exist." }
public function Connection::redirect (Response response, RedirectCode code, string[] locations) returns (HttpConnectorError | ()) {
    match code {
        MULTIPLE_CHOICES_300 => response.statusCode = 300;
        MOVED_PERMANENTLY_301 => response.statusCode = 301;
        FOUND_302 => response.statusCode = 302;
        SEE_OTHER_303 => response.statusCode = 303;
        NOT_MODIFIED_304 => response.statusCode = 304;
        USE_PROXY_305 => response.statusCode = 305;
        TEMPORARY_REDIRECT_307 => response.statusCode = 307;
    }
    string locationsStr = "";
    foreach location in locations {
        locationsStr = locationsStr + location + ",";
    }
    locationsStr = locationsStr.subString(0, (lengthof locationsStr) - 1);

    response.setHeader(LOCATION, locationsStr);
    return self.respond(response);
}