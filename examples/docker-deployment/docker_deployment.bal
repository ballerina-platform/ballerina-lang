import ballerina/http;
import ballerina/log;
import ballerinax/docker;

//Adding the @docker:Expose{} annotation to a listner endpoint will expose the endpoint port.
@docker:Expose {}
endpoint http:Listener helloWorldEP {
    port: 9090
};

//Adding the @docker:Confing{} annotation to a service modifies the generated Docker image and Dockerfile.
//This sample will generate a docker image as `helloworld:v1.0.0`.
@docker:Config {
    //Docker image name should be helloworld.
    name: "helloworld",
    //Docker image version should be v1.0.
    tag: "v1.0"
}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service<http:Service> helloWorld bind helloWorldEP {
    sayHello(endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello World from Docker ! \n");
        outboundEP->respond(response) but {
            error e => log:printError(
                           "Error sending response", err = e)
        };
    }
}
