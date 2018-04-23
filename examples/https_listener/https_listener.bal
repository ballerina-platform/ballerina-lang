import ballerina/http;
import ballerina/log;

// The Ballerina listener can be used to connect to a https client. To verify the server authenticity
// when establishing the connection, provide a `keyStore filePath` and `keyStore password
endpoint http:Listener helloWorldEP {
    port:9095,
    secureSocket:{
        keyStore:{
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        }
    }
};

@http:ServiceConfig {
    endpoints:[helloWorldEP],
    basePath:"/hello"
}
service helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }

    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Successful");
        caller->respond(res) but { error e => log:printError("Error in responding ", err = e) };
    }
}
