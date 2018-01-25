import ballerina.net.http;

@http:configuration {
    basePath:"/hello",
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
    keyStorePassword:"ballerina",
    certPassword:"ballerina"
}

service<http> helloWorld {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }

    resource sayHello (http:Request req, http:Response res) {
        res.setStringPayload("Hello World!");
        _ = res.send();
    }
}

@Description {value:"The HTTP client connector can be used to connect to HTTPS services. You have to run the given service before running this main function. As this is a 1-way SSL connection, client needs to provide a trust store and its password."}
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
                 trustStoreFile:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
                 trustStorePassword:"ballerina"
               },
          followRedirects: {}
    };
    return option;
}
