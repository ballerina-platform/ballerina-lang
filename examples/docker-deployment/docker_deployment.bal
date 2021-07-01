import ballerina/http;
import ballerina/log;
import ballerina/docker;

//Adding the `@docker:Expose{}` annotation to a listener endpoint exposes the endpoint port.
@docker:Expose {}
listener http:Listener helloWorldEP = new(9090);

//Adding the `@docker:Config{}` annotation to a service modifies the generated Docker image and Dockerfile.
//This sample generates a Docker image as `helloworld:v1.0.0`.
@docker:Config {
    //Docker image name should be helloworld.
    name: "helloworld",
    //Docker image version should be v1.0.
    tag: "v1.0"
}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloWorldEP {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello World from Docker ! \n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            error err = responseResult;
            log:printError("Error sending response", err);
        }
    }
}
