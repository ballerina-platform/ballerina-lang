package ballerina.net.http;

import ballerina.mime;

public enum Chunking {
    AUTO, ALWAYS, NEVER
}

public enum TransferEncoding {
    CHUNKING
}

@Description {value:"Represent 'content-length' header name"}
public const string CONTENT_LENGTH = "content-length";

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
