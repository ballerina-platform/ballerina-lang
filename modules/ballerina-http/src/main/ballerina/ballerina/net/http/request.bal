package ballerina.net.http;

import ballerina.net.mime;

const string HEADER_VAL_100_CONTINUE = "100-continue";
const string HEADER_KEY_EXPECT = "Expect";

@Description { value:"Gets a transport header from the request"}
@Param { value:"req: A request message" }
@Param { value:"headerName: The header name" }
@Return { value:"The first header value struct for the provided header name. Returns null if the header does not exist." }
public function <Request req> getHeader (string headerName)(mime:HeaderValue) {
    mime:Entity entity = req.getEntity();
    if (entity.headers == null) {
        return null;
    }
    var headerValues = entity.headers[headerName];
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
    mime:Entity entity = req.getEntity();
    if (entity.headers == null) {
        entity.headers = {};
    }
    var headerValues = entity.headers[headerName];
    if (headerValues == null) {
        mime:HeaderValue[] headers = [{value:headerValue}];
        entity.headers[headerName] = headers;
    } else {
        var valueArray =  getHeaderValueArray(headerValues, headerName);
        valueArray[lengthof valueArray] = {value:headerValue};
    }
}

@Description { value:"Gets transport headers from the request"}
@Param { value:"req: A request message" }
@Param { value:"headerName: The header name" }
@Return { value:"The header values struct array for a given header name" }
public function <Request req> getHeaders (string headerName) (mime:HeaderValue[]) {
    mime:Entity entity = req.getEntity();
    if (entity.headers == null) {
        return null;
    }
    var headerValues = entity.headers[headerName];
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
    mime:Entity entity = req.getEntity();
    if (entity.headers == null) {
        entity.headers = {};
    }
    mime:HeaderValue[] header = [{value:headerValue}];
    entity.headers[headerName] = header;
}

@Description { value:"Removes a transport header from the request"}
@Param { value:"req: A request message" }
@Param { value:"key: The header name" }
public function <Request req> removeHeader (string key) {
    mime:Entity entity = req.getEntity();
    if (entity.headers == null) {
        return;
    }
    entity.headers.remove(key);
}

@Description { value:"Removes all transport headers from the message"}
@Param { value:"req: A request message" }
public function <Request req> removeAllHeaders () {
    mime:Entity entity = req.getEntity();
    entity.headers = {};
}

@Description { value:"Checks whether the client expects a 100-continue response."}
@Param { value:"req: A request struct" }
@Return { value:"Returns true if the client expects a 100-continue response. If not, returns false." }
public function <Request req> expects100Continue() (boolean) {
    mime:Entity entity = req.getEntity();
    var expectHeader = req.getHeader(HEADER_KEY_EXPECT);
    if (expectHeader != null && expectHeader.value == HEADER_VAL_100_CONTINUE) {
        return true;
    }
    return false;
}
