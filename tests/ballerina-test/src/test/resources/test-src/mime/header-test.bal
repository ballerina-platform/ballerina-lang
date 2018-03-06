import ballerina.mime;

function testAddHeader (string headerName, string headerValue, string headerNameToBeUsedForRetrieval) (string) {
    mime:Entity entity = {};
    entity.addHeader(headerName, headerValue);
    return entity.getHeader(headerNameToBeUsedForRetrieval);
}


