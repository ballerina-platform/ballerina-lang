// This is the server implementation for the secured connection (HTTPS) scenario.
import ballerina/io;

function main(string... args) {
    // Client endpoint configuration with SSL configurations.
    endpoint HelloWorldBlockingClient helloWorldBlockingEp {
        url: "https://localhost:9090",
        secureSocket: {
            keyStore: {
                path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: "ballerina"
            },
            trustStore: {
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            protocol: {
                name: "TLSv1.2",
                versions: ["TLSv1.2", "TLSv1.1"]
            },
            ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
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

