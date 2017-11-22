import ballerina.net.uri;

function testEncode(string url)(string){
    return uri:encode(url);
}
