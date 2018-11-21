@test:Config
function ${testServiceFunctionName} () {
    endpoint http:WebSocketClient wsEndpoint { url: ${serviceUriStrName}, callbackService: ${mockServiceName} };
    //Send a message to mock service
    _ = wsEndpoint->pushText("${request}");
}

@http:WebSocketServiceConfig {
path: ${mockServicePath}
}
service< http:WebSocketService > ${mockServiceName} bind { port: ${mockServicePort} } {
    // This resource is triggered when a new text frame is received from a client.
    onText(endpoint caller, string text, boolean final) {
        caller->pushText("${mockResponse}") but {
            error e => log:printError("Error occurred when sending text", err = e)
        };
    }
}
