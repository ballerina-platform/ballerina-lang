import ballerina/http;
import ballerina/log;
import ballerinax/docker;

//Adding the `@docker:Expose{}` annotation to a listner endpoint exposes the endpoint port.
@docker:Expose {}
endpoint http:Listener helloWorldEP {
    port: 9090
};

//Adding the `@docker:Confing{}` annotation to a service modifies the generated Docker image and Dockerfile.
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
service<http:Service> helloWorld bind helloWorldEP {
    sayHello(endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello World from Docker ! \n");

        var result = outboundEP->respond(response);
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}
