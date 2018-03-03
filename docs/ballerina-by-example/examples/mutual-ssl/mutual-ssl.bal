import ballerina.io;
import ballerina.net.http;

@http:configuration {
    basePath:"/hello",
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
    keyStorePassword:"ballerina",
    certPassword:"ballerina",
    sslVerifyClient:"require",
    trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
    trustStorePassword:"ballerina",
    ciphers:"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
    sslEnabledProtocols:"TLSv1.2,TLSv1.1"
}

service<http> helloWorld {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }

    resource sayHello (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        //Set response payload.
        res.setStringPayload("Successful");
        //Send response to client.
        _ = conn.respond(res);
    }
}

@Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the service before running this main function. As this is a mutual ssl connection, client also needs to provide keyStoreFile, keyStorePassword, trustStoreFile and trustStorePassword."}
function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient("https://localhost:9095", getConnectorConfigs());
    }
    //Creates a request.
    http:OutRequest req = {};
    http:InResponse resp = {};
    resp, _ = httpEndpoint.get("/hello/", req);
    io:println("Response code: " + resp.statusCode);
    io:println("Response: " + resp.getStringPayload());
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
