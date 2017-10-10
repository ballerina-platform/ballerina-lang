import ballerina.net.http;
import ballerina.net.http.response;
import ballerina.lang.system;

@doc:Description {value:"Ballerina client connector can be used to connect to the created https server. You have to run the server.bal before running this. As this is a mutual ssl connection, client also needs to provide keyStoreFile, keyStorePassword, trustStoreFile and trustStorePassword."}
function main (string[] args) {
    //create a client connector providing the uri and the struct we created.
    http:ClientConnector clientConnector = create http:ClientConnector("https://localhost:9095", getConnectorConfigs());
    //creates a request
    http:Request req = {};
    http:Response resp = clientConnector.get("/hello/", req);
    system:println("Response code: " + response:getStatusCode(resp));
    system:println("Response: " + response:getStringPayload(resp));
}

function getConnectorConfigs() (http:Options) {
    http:Options option = {
    //Set key store location
    keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePassword:"wso2carbon",
    //Set trust store location
    trustStoreFile:"${ballerina.home}/bre/security/client-truststore.jks",
    trustStorePassword:"wso2carbon"
    };
    return option;
}