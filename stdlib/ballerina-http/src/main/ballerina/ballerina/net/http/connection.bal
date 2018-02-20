package ballerina.net.http;

const string HEADER_KEY_LOCATION = "Location";

@Description { value:"Status codes for HTTP redirect"}
@Field { value:"MULTIPLE_CHOICES_300: Represents status code 300 - Multiple Choices."}
@Field { value:"MOVED_PERMANENTLY_301: Represents status code 301 - Moved Permanently."}
@Field { value:"FOUND_302: Represents status code 302 - Found."}
@Field { value:"SEE_OTHER_303: Represents status code 303 - See Other."}
@Field { value:"NOT_MODIFIED_304: Represents status code 304 - Not Modified."}
@Field { value:"USE_PROXY_305: Represents status code 305 - Use Proxy."}
@Field { value:"TEMPORARY_REDIRECT_307: Represents status code 307 - Temporary Redirect."}
public enum RedirectCode {
    MULTIPLE_CHOICES_300,
    MOVED_PERMANENTLY_301,
    FOUND_302,
    SEE_OTHER_303,
    NOT_MODIFIED_304,
    USE_PROXY_305,
    TEMPORARY_REDIRECT_307
}

@Description { value:"Sends a 100-continue response to the client."}
@Param { value:"conn: The server connector connection" }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
public function <Connection conn> respondContinue () (HttpConnectorError) {
    OutResponse res = {};
    res.statusCode = 100;
    HttpConnectorError err = conn.respond(res);
    return err;
}

@Description { value:"Sends a redirect response to the user with given redirection status code." }
@Param { value:"conn: The server connector connection" }
@Param { value:"response: Response to be sent to client." }
@Param { value:"redirectCode: Status code of the specific redirect." }
@Param { value:"locations: Array of locations where the redirection can happen." }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
documentation {
Sends a redirect response to the user with given redirection status code.
- #conn The server connector connection.
- #response Response to be sent to client.
- #code Status code of the specific redirect.
- #locations Array of locations where the redirection can happen.
}
public function <Connection conn> redirect (OutResponse response, RedirectCode code, string[] locations) (HttpConnectorError) {
    if (code == RedirectCode.MULTIPLE_CHOICES_300) {
        response.statusCode = 300;
    } else if (code == RedirectCode.MOVED_PERMANENTLY_301) {
        response.statusCode = 301;
    } else if (code == RedirectCode.FOUND_302) {
        response.statusCode = 302;
    } else if (code == RedirectCode.SEE_OTHER_303) {
        response.statusCode = 303;
    } else if (code == RedirectCode.NOT_MODIFIED_304) {
        response.statusCode = 304;
    } else if (code == RedirectCode.USE_PROXY_305) {
        response.statusCode = 305;
    } else if (code == RedirectCode.TEMPORARY_REDIRECT_307) {
        response.statusCode = 307;
    }

    string locationsStr = "";
    foreach location in locations {
        locationsStr = locationsStr + location + ",";
    }
    locationsStr = locationsStr.subString(0, (lengthof locationsStr) - 1);

    response.setHeader(HEADER_KEY_LOCATION, locationsStr);
    return conn.respond(response);
}
