package ballerina.net.http;

import ballerina.mime;

function getHeadersFromEntity (mime:Entity entity, string headerName)(mime:HeaderValue[]) {
    if (entity.headers == null) {
        return null;
    }
    var headerValues = entity.headers[headerName];
    if (headerValues == null) {
        return null;
    }
    return getHeaderValueArray(headerValues, headerName);
}

function getHeaderValueArray (any headerValues, string headerName)(mime:HeaderValue[]) {
    var valueArray, err = (mime:HeaderValue[]) headerValues;
    if (err != null) {
        error errMsg = {msg:"expect 'ballerina.net.mime:HeaderValue[]' as header value type of : " + headerName};
        throw errMsg;
    }
    return valueArray;
}



