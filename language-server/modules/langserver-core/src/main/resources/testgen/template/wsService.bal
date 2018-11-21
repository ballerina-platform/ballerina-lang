@test:Config
function ${testServiceFunctionName} () {
    endpoint http:WebSocketClient wsEndpoint { url: ${serviceUriStrName}, callbackService: ${callbackServiceName} };
    //Send a message
    _ = wsEndpoint->pushText("hey");
}

service<http:WebSocketClientService> ${callbackServiceName} {
    onText(endpoint caller, string text, boolean final) {
        //Test received message
        test:assertEquals(text, "hey", msg = "Received message should be equal to the expected message");
    }
}
