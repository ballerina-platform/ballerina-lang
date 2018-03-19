package ballerina.net.http;

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


function getContentLengthIntValue (string strContentLength) (int) {
    var contentLength, conversionErr = <int>strContentLength;
    if (conversionErr != null) {
        contentLength = -1;
        throw conversionErr;
    }
    return contentLength;
}
