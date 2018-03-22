import ballerina/net.uri;

function testEncode(string url)(string){
    return uri:encode(url);
}

function testGetQueryParam(message m, string queryParam)(string){
    return uri:getQueryParam(m, queryParam);
}
