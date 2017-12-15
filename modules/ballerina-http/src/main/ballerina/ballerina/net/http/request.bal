package ballerina.net.http;

const string HEADER_VAL_100_CONTINUE = "100-continue";
const string HEADER_KEY_EXPECT = "Expect";

@Description { value:"Gets a transport header from the request"}
@Param { value:"req: A request message" }
@Param { value:"headerName: The header name" }
@Return { value:"The first header value struct for the provided header name. Returns null if the header does not exist." }
public function <Request req> getHeader (string headerName)(HeaderValue) {
    if (req.headers == null) {
        return null;
    }
    var headerValues = req.headers[headerName];
    if (headerValues == null) {
        return null;
    }
    return getHeaderValueArray(headerValues, headerName)[0];
}

@Description { value:"Adds the specified key/value pair as an HTTP header to the request"}
@Param { value:"req: A request message" }
@Param { value:"headerName: The header name" }
@Param { value:"headerValue: The header value" }
public function <Request req> addHeader (string headerName, string headerValue) {
    if (req.headers == null) {
        req.headers = {};
    }
    var headerValues = req.headers[headerName];
    if (headerValues == null) {
        HeaderValue[] headers = [{value:headerValue}];
        req.headers[headerName] = headers;
    } else {
        var valueArray =  getHeaderValueArray(headerValues, headerName);
        valueArray[lengthof valueArray] = {value:headerValue};
    }
}

@Description { value:"Gets transport headers from the request"}
@Param { value:"req: A request message" }
@Param { value:"headerName: The header name" }
@Return { value:"The header values struct array for a given header name" }
public function <Request req> getHeaders (string headerName) (HeaderValue[]) {
    if (req.headers == null) {
        return null;
    }
    var headerValues = req.headers[headerName];
    if (headerValues == null) {
        return null;
    }
    return getHeaderValueArray(headerValues, headerName);
}

@Description { value:"Sets the value of a transport header"}
@Param { value:"req: A request message" }
@Param { value:"headerName: The header name" }
@Param { value:"headerValue: The header value" }
public function <Request req> setHeader (string headerName, string headerValue) {
    if (req.headers == null) {
        req.headers = {};
    }
    HeaderValue[] header = [{value:headerValue}];
    req.headers[headerName] = header;
}

@Description { value:"Removes a transport header from the request"}
@Param { value:"req: A request message" }
@Param { value:"key: The header name" }
public function <Request req> removeHeader (string key) {
    if (req.headers == null) {
        return;
    }
    req.headers.remove(key);
}

@Description { value:"Removes all transport headers from the message"}
@Param { value:"req: A request message" }
public function <Request req> removeAllHeaders () {
    req.headers = {};
}

@Description { value:"Checks whether the client expects a 100-continue response."}
@Param { value:"req: A request struct" }
@Return { value:"Returns true if the client expects a 100-continue response. If not, returns false." }
public function <Request req> expects100Continue() (boolean) {
    var expectHeader = req.getHeader(HEADER_KEY_EXPECT);
    if (expectHeader != null && expectHeader.value == HEADER_VAL_100_CONTINUE) {
        return true;
    }
    return false;
}
