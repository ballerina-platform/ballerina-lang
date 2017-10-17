import ballerina.doc;
import ballerina.lang.system;
import ballerina.net.http;

@doc:Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the server.bal before running this. As this is a mutual ssl connection, client also needs to provide keyStoreFile, keyStorePassword, trustStoreFile and trustStorePassword."}
function main (string[] args) {
    http:ClientConnector clientConnector;
    //create a client connector providing the uri and the struct we created.
    clientConnector = create http:ClientConnector("https://localhost:9095", getConnectorConfigs());
    //creates a request
    http:Request req = {};
    http:Response res = clientConnector.get("/hello/", req);
    system:println("Response code: " + res.getStatusCode());
    system:println("Response: " + res.getStringPayload());
}

function getConnectorConfigs () (http:Options) {
    http:Options option = {
                              ssl:{
                                      keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
                                      keyStorePassword:"wso2carbon",
                                      trustStoreFile:"${ballerina.home}/bre/security/client-truststore.jks",
                                      trustStorePassword:"wso2carbon"
                                  }, followRedirects:{}
                          };
    return option;
}
