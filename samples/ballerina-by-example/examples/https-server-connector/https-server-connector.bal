import ballerina.net.http;

@Description {value:"Ballerina server connector can be used to connect to a https client. If client needs to verify server authenticity when establishing the connection, server needs to provide keyStoreFile, keyStorePassword and certificate password as given here."}
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
