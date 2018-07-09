// This is the server implementation for the secured connection (HTTPS) scenario.
import ballerina/io;

function main(string... args) {
    // Client endpoint configuration with SSL configurations.
    endpoint HelloWorldBlockingClient helloWorldBlockingEp {
        url: "https://localhost:9090",
        secureSocket: {
            trustStore: {
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            }
        }
    };

    // Executes unary blocking secured call.
    var unionResp = helloWorldBlockingEp->hello("WSO2");

    match unionResp {
        (string, grpc:Headers) payload => {
            string result;
            (result, _) = payload;
            io:println("Client Got Response : ");
            io:println(result);
        }
        error err => {
            io:println("Error from Connector: " + err.message);
        }
    }
}

