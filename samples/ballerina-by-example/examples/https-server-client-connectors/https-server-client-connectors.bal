import ballerina.net.http;

@http:configuration {
    basePath:"/hello",
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePassword:"wso2carbon",
    certPassword:"wso2carbon"
}

service<http> helloWorld {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }

    resource sayHello (http:Request req, http:Response res) {
        res.setStringPayload("Successful");
        res.send();
    }
}

@Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the service before running this main function. As this is a 1-way ssl connection, client needs to provide trustStoreFile and trustStorePassword."}
function main (string[] args) {
    http:ClientConnector clientConnector = create
                 http:ClientConnector("https://localhost:9095", getConnectorConfigs());
    //creates a request
    http:Request req = {};
    http:Response resp = clientConnector.get("/hello/", req);
    println("Response code: " + resp.getStatusCode());
    println("Response: " + resp.getStringPayload());
}

function getConnectorConfigs() (http:Options) {
    http:Options option = {
          ssl: {
                 trustStoreFile:"${ballerina.home}/bre/security/client-truststore.jks",
                 trustStorePassword:"wso2carbon"
               },
          followRedirects: {}
    };
    return option;
}
