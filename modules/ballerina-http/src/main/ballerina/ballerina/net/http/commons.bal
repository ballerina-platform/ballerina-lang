package ballerina.net.http;

import ballerina.mime;

function getHeadersFromEntity (mime:Entity entity, string headerName)(string) {
    if (entity.headers == null) {
        return null;
    }
    var headerValue, _ = (string)entity.headers[headerName];
    if (headerValue == null) {
        return null;
    }
    return headerValue;
}

function getFirstHeaderValue(string headerValue)(string) {
    if (headerValue == null) {
        return null;
    }
    //Multiple message-header fields with the same field-name MAY be present in a message if and only if the entire
    //field-value for that header field is defined as a comma-separated list.
    //(https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2)
    if (headerValue.contains(",")) {
        headerValue = headerValue.subString(0, headerValue.indexOf(","));
    }
    return headerValue;
}

function getContentLengthIntValue(string strContentLength)(int) {
    var contentLength, conversionErr = <int>strContentLength;
    if (conversionErr != null) {
        contentLength = -1;
        throw conversionErr;
    }
    return contentLength;
}

function addHeaderToEntity(mime:Entity entity, string headerName, string headerValue){
    if (entity.headers == null) {
        entity.headers = {};
    }
    var existingValues = entity.headers[headerName];
    if (existingValues == null) {
        entity.headers[headerName] = headerValue;
    } else {
        var values, err = (string) existingValues;
        entity.headers[headerName] = values + ", " + headerValue;
    }
}
