@test:Config
function ${testServiceFunctionName} () {
    endpoint http:WebSocketClient wsEndpoint { url: ${serviceUriStrName}, callbackService: wsClientService };
    //Send a message to mock service
    _ = wsEndpoint->pushText("${request}");
}
