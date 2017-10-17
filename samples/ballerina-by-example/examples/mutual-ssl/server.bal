import ballerina.net.http;

@http:configuration {
    basePath:"/hello",
    httpsPort:9095,
    keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePassword:"wso2carbon",
    certPassword:"wso2carbon",
    sslVerifyClient:"require",
    trustStoreFile:"${ballerina.home}/bre/security/client-truststore.jks",
    trustStorePassword:"wso2carbon"
}
service<http> mutualSSL {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Request req, http:Response res) {
        //Set response payload
        res.setStringPayload("Successful");
        //Send response to client
        res.send();
    }
}
