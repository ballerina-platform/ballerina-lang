import ballerina.net.http;

function testGetMethod(message msg)(string){
    return http:getMethod(msg);
}

function testConvertToResponse(message m){
    http:convertToResponse(m);
}
