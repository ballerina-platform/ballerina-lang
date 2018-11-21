@test:Config
function ${testServiceFunctionName} () {
    endpoint http:WebSocketClient wsEndpoint {
        url: ${serviceUriStrName},
        secureSocket: {
            trustStore: {
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            }
        },
        callbackService: ${callbackServiceName}
    };
    //Send a message
    _ = wsEndpoint->pushText("hey");
}

service<http:WebSocketClientService> ${callbackServiceName} {
    onText(endpoint caller, string text, boolean final) {
        //Test received message
        test:assertEquals(text, "hey", msg = "Received message should be equal to the expected message");
    }
}
