import ballerina.net.http;
import ballerina.io;

endpoint<http:Client> clientEP {
    serviceUri: "https://localhost:9095",
    ssl: {
        keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
        keyStorePassword:"ballerina",
        trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
        trustStorePassword:"ballerina",
        ciphers:"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
        sslEnabledProtocols:"TLSv1.2,TLSv1.1"
    }
}

function main (string[] args) {
    http:Request req = {};
    http:Response resp = {};
    resp, _ = clientEP -> get("/echo/", req);
    var payload, _ = resp.getStringPayload();
    io:println(payload);
}