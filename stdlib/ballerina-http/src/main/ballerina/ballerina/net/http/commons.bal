package ballerina.net.http;

import ballerina.mime;

function getHeadersFromEntity (mime:Entity entity) (map) {
    return entity.headers;
}

function getFirstHeaderFromEntity (mime:Entity entity, string headerName) (string) {
    var headerValue, _ = (string[]) entity.headers[headerName];
    return headerValue == null ? null : headerValue[0];
}

function getHeadersFromEntity (mime:Entity entity, string headerName) (string[]) {
    var headerValue, _ = (string[]) entity.headers[headerName];
    return headerValue;
}

function getContentLengthIntValue (string strContentLength) (int) {
    var contentLength, conversionErr = <int>strContentLength;
    if (conversionErr != null) {
        contentLength = -1;
        throw conversionErr;
    }
    return contentLength;
}

function addHeaderToEntity (mime:Entity entity, string headerName, string headerValue){
    var existingValues = entity.headers[headerName];
    if (existingValues == null) {
        setHeaderToEntity(entity, headerName, headerValue);
    } else {
        var valueArray, _ = (string[]) existingValues;
        valueArray[lengthof valueArray] = headerValue;
        entity.headers[headerName] = valueArray;
    }
}

function setHeaderToEntity (mime:Entity entity, string headerName, string headerValue) {
    string[] valueArray = [headerValue];
    entity.headers[headerName] = valueArray;
}
