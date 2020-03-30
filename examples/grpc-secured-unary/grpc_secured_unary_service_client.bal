// This is the client implementation of the secured connection (HTTPS) scenario.
import ballerina/config;
import ballerina/grpc;
import ballerina/io;

public function main() {
    // Client endpoint configuration with SSL configurations.
    HelloWorldBlockingClient helloWorldBlockingEp = new ("https://localhost:9090", {
            secureSocket: {
                trustStore: {
                    path: config:getAsString("b7a.home") + "/bre/security/ballerinaTruststore.p12",
                    password: "ballerina"
                }
            }
    });

    // Executes unary blocking secured call.
    var unionResp = helloWorldBlockingEp->hello("WSO2");
    if (unionResp is grpc:Error) {
        io:println("Error from Connector: " + unionResp.reason() + " - "
                                         + <string>unionResp.detail()["message"]);
    } else {
        string result;
        [result, _] = unionResp;
        io:println("Client Got Response : " + result);
    }
}
