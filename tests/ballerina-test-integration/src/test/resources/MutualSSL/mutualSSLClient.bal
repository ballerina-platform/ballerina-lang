import ballerina.net.http;
import ballerina.io;
import ballerina.log;

function main (string[] args) {
    endpoint<http:HttpClient> connectorEP {
                              create http:HttpClient("https://localhost:9095", getConnectorConfigs());
    }
    http:OutRequest req = {};
    http:InResponse resp = {};
    log:printInfo("Sending request");
    resp, _ = connectorEP.get("/echo/", req);
    log:printInfo("response received");
    io:println(resp.getStringPayload());
}

function getConnectorConfigs() (http:Options) {
    http:Options option = {
              ssl: {
                   keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
                   keyStorePassword:"ballerina",
                   trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
                   trustStorePassword:"ballerina",
                   ciphers:"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                   sslEnabledProtocols:"TLSv1.2,TLSv1.1"
                   },
              followRedirects: {}
    };
    return option;
}