@test:Config
function ${testServiceFunctionName} () {
    http:WebSocketClient ${endpointName} = new(
        ${serviceUriStrName},
        config = {
            callbackService: ${callbackServiceName},
            secureSocket: {
                trustStore: {
                    path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                    password: "ballerina"
                }
            },
            readyOnConnect: false
    });
    //Send a message
    _ = ${endpointName}->pushText("hey");
    //Wait some time for a reply
    runtime:sleep(1000);
    // Test reply
    //test:assertEquals(${callbackServiceName}.text, "hey", msg = "Received message should be equal to the expected message");
}

service ${callbackServiceName} = @http:WebSocketServiceConfig {} service {
    string wsReply = "";
    resource function onText(http:WebSocketClient ${callbackServiceName}Ep, string text) {
        //Test received message
        self.wsReply = untaint text;
    }
};
