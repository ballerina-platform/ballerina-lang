import ballerina/io;
import ballerina/http;
import ballerina/mime;

endpoint http:Listener helloWorldEP {
    port:9095,
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};


@http:ServiceConfig {
    endpoints:[helloWorldEP],
    basePath:"/hello"
}

service<http:Service> helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }

    sayHello (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Successful");
        _ = caller -> respond(res);
    }
}

endpoint http:Client clientEP {
    url:"https://localhost:9095",
    secureSocket:{
        trustStore:{
            path:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password:"ballerina"
        }
    }
};
@Description {value:"The Ballerina client connector can be used to connect to the created HTTPS server. You have to run the service before running this main function. As this is a 1-way SSL connection, the client needs to provide values for ‘trustStoreFile’ and ‘trustStorePassword’."}
function main (string... args) {
    //Creates an outbound request.
    var resp = clientEP -> get("/hello/");
    match resp {
        http:HttpConnectorError err => io:println(err.message);
        http:Response response => {
            match (response.getStringPayload()) {
                http:PayloadError payloadError => io:println(payloadError.message);
                string res => io:println(res);
            }
        }
    }
}
