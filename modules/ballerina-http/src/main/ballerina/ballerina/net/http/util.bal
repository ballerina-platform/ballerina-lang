package ballerina.net.http;

function getHeaderValueArray (any headerValues, string headerName)(HeaderValue[]) {
    var valueArray, err = (HeaderValue[]) headerValues;
    if (err != null) {
        error errMsg = {msg:"expect 'ballerina.net.http:HeaderValue[]' as header value type of : " + headerName};
        throw errMsg;
    }
    return valueArray;
}