import ballerina.net.http;
import ballerina.net.http.response;

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

service<http> mutualSSL {
    @http:resourceConfig {
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

