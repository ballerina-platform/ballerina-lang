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

    resource sayHello (http:Request req, http:Response res) {
        //Set response payload.
        res.setStringPayload("Hello World!");
        //Send response to client.
        _ = res.send();
    }
}

@Description {value:"The HTTP client connector can be used to invoke the given HTTPS service. The service needs to be up and running before running this main function. As this is a mutual SSL connection, the client also needs to provide configurations for both, a key store and a trust store."}
function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient("https://localhost:9095", getConnectorConfigs());
    }
    //Creates a request.
    http:Request req = {};
    http:Response resp = {};
    resp, _ = httpEndpoint.get("/hello/", req);
    println("Response code: " + resp.getStatusCode());
    println("Response: " + resp.getStringPayload());
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
