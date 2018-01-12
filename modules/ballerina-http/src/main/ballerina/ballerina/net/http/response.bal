package ballerina.net.http;

@Description { value:"Gets the named HTTP header from the response"}
@Param { value:"res: The response message" }
@Param { value:"headerName: The header name" }
@Return { value:"The first header value struct for the provided header name. Returns null if the header does not exist." }
public function <Response res> getHeader (string headerName) (HeaderValue) {
    if (res.headers == null) {
        return null;
    }
    var headerValues = res.headers[headerName];
    if (headerValues == null) {
        return null;
    }
    return getHeaderValueArray(headerValues, headerName)[0];
}

@Description { value:"Adds the specified key/value pair as an HTTP header to the response"}
@Param { value:"res: The response message" }
@Param { value:"headerName: The header name" }
@Param { value:"headerValue: The header value" }
public function <Response res> addHeader (string headerName, string headerValue) {
    if (res.headers == null) {
        res.headers = {};
    }
    var headerValues = res.headers[headerName];
    if (headerValues == null) {
        HeaderValue[] headers = [{value:headerValue}];
        res.headers[headerName] = headers;
    } else {
        var valueArray =  getHeaderValueArray(headerValues, headerName);
        valueArray[lengthof valueArray] = {value:headerValue};
    }
}

@Description { value:"Gets the HTTP headers from the response"}
@Param { value:"res: The response message" }
@Param { value:"headerName: The header name" }
@Return { value:"The header values struct array for a given header name" }
public function <Response res> getHeaders (string headerName) (HeaderValue[]) {
    if (res.headers == null) {
        return null;
    }
    var headerValues = res.headers[headerName];
    if (headerValues == null) {
        return null;
    }
    return getHeaderValueArray(headerValues, headerName);
}

@Description { value:"Sets the value of a transport header"}
@Param { value:"res: The response message" }
@Param { value:"key: The header name" }
@Param { value:"value: The header value" }
public function <Response res> setHeader (string key, string value) {
    if (res.headers == null) {
        res.headers = {};
    }
    HeaderValue[] header = [{value:value}];
    res.headers[key] = header;
}

@Description { value:"Removes a transport header from the response"}
@Param { value:"res: The response message" }
@Param { value:"key: The header name" }
public function <Response res> removeHeader (string key) {
    if (res.headers == null) {
        return;
    }
    res.headers.remove(key);
}

@Description { value:"Removes all transport headers from the response"}
@Param { value:"res: The response message" }
public function <Response res> removeAllHeaders () {
    res.headers = {};
}

@Description { value:"Sends a 100-continue response to the client."}
@Param { value:"res: A response struct" }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
public function <Response res> send100Continue() (HttpConnectorError) {
    res.setStatusCode(100);
    HttpConnectorError err = res.send();
    return err;
}
