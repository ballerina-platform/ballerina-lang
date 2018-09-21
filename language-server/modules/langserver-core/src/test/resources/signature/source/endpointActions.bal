import ballerina/http;

endpoint http:Listener listener {
    port: 9090
};

function testSignatureHelp () {
    listener->respond(
}