package ballerina.net.http;

import ballerina.net.mime;

function getHeaderValueArray (any headerValues, string headerName)(mime:HeaderValue[]) {
    var valueArray, err = (mime:HeaderValue[]) headerValues;
    if (err != null) {
        error errMsg = {msg:"expect 'ballerina.net.http:HeaderValue[]' as header value type of : " + headerName};
        throw errMsg;
    }
    return valueArray;
}