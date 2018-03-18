package ballerina.net.http;

import ballerina.mime;

public enum Chunking {
    AUTO, ALWAYS, NEVER
}

public enum Compression {
    AUTO, ALWAYS, NEVER
}

public enum TransferEncoding {
    CHUNKING
}


//////////////////////////////
/// Native implementations ///
//////////////////////////////

@Description { value:"Parse headerValue and return value with parameter map"}
@Param { value:"headerValue: The header value" }
@Return { value:"The header value" }
@Return { value:"The header value parameter map" }
@Return { value:"Error occured during header parsing" }
public native function parseHeader (string headerValue)(string, map, error);


/////////////////////////////////
/// Ballerina Implementations ///
/////////////////////////////////

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
