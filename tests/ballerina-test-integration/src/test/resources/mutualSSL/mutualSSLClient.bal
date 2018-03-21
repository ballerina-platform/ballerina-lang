import ballerina.net.http;
import ballerina.io;

endpoint http:ClientEndpoint clientEP {
    targets: [{
        uri: "https://localhost:9095",
        secureSocket: {
            keyStore: {
                filePath: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: "ballerina"
            },
            trustStore: {
                filePath: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            protocols: {
                protocolName: "TLSv1.2",
                versions: "TLSv1.2,TLSv1.1"
            },
            ciphers:"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"
        }
    }]
};

function main (string[] args) {
    http:Request req = {};
    http:Response resp = {};
    resp, _ = clientEP -> get("/echo/", req);
    var payload, _ = resp.getStringPayload();
    io:println(payload);
}