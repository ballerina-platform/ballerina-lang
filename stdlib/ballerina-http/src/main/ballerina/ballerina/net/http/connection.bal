package ballerina.net.http;

const string HEADER_KEY_LOCATION = "Location";

enum redirectCode {
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

@Description { value:"Sends a redirect response code to the user with given redirection status code." }
@Param { value:"conn: The server connector connection" }
@Param { value:"response: Response which user wants to send." }
@Param { value:"redirectCode: Status code of the specific redirect." }
@Param { value:"locations: Array of locations where the redirection can happen." }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
public function <Connection conn> redirect(OutResponse response, redirectCode code, string[] locations) (HttpConnectorError) {
    if (code == redirectCode.MULTIPLE_CHOICES_300) {
        response.statusCode = 300;
    } else if (code == redirectCode.MOVED_PERMANENTLY_301) {
        response.statusCode = 301;
    } else if (code == redirectCode.FOUND_302) {
        response.statusCode = 302;
    } else if (code == redirectCode.SEE_OTHER_303) {
        response.statusCode = 303;
    } else if (code == redirectCode.NOT_MODIFIED_304) {
        response.statusCode = 304;
    } else if (code == redirectCode.USE_PROXY_305) {
        response.statusCode = 305;
    } else if (code == redirectCode.TEMPORARY_REDIRECT_307) {
        response.statusCode = 307;
    }

    string locationsStr = "";
    foreach location in locations {
        locationsStr = locationsStr + location + ",";
    }
    locationsStr = locationsStr.substring(0, (lengthof locationsStr) - 1);

    response.setHeader(HEADER_KEY_LOCATION, locationsStr);
    var e = conn.respond(response);
    return e;
}