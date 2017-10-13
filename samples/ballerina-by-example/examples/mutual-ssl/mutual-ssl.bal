import ballerina.net.http;
import ballerina.net.http.response;
import ballerina.lang.system;

@http:configuration {
    basePath:"/hello",
    //Defines a https port.
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePassword:"wso2carbon",
    certPassword:"wso2carbon",
    //To enable mutual ssl, have to configure the server by giving verifyClient = "require".
    sslVerifyClient:"require",
    //Set trustStore location.
    trustStoreFile:"${ballerina.home}/bre/security/client-truststore.jks",
    //Set trustStore password
    trustStorePassword:"wso2carbon"
}

service<http> helloWorld {
    @http:resourceConfig {
    //Send a get request to the specified endpoint.
        methods:["GET"],
        path:"/"
    }

    resource sayHello (http:Request req, http:Response res) {
        //Set response payload
        response:setStringPayload(res, "Successful");
        //Send response to client
        response:send(res);
    }
}

@doc:Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the service before running this main function. As this is a mutual ssl connection, client also needs to provide keyStoreFile, keyStorePassword, trustStoreFile and trustStorePassword."}
function main (string[] args) {
    //create a client connector providing the uri and the struct containing relevant files and passwords.
    http:ClientConnector clientConnector = create http:ClientConnector("https://localhost:9095", getConnectorConfigs());
    //creates a request
    http:Request req = {};
    http:Response resp = clientConnector.get("/hello/", req);
    system:println("Response code: " + response:getStatusCode(resp));
    system:println("Response: " + response:getStringPayload(resp));
}

function getConnectorConfigs() (http:Options) {
    http:Options option = {ssl: {keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
                                 keyStorePassword:"wso2carbon",
                                 trustStoreFile:"${ballerina.home}/bre/security/client-truststore.jks",
                                 trustStorePassword:"wso2carbon"
                                },
                           followRedirects: {}
                          };
    return option;
}
