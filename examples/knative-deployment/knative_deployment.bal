import ballerina/http;
import ballerina/knative;
import ballerina/log;

//Add the `@knative:Service` to a Ballerina service to generate a Knative Service artifact and push the Docker image to Docker Hub.
@knative:Service {
    //Enable pushing the Docker image.
    push: true,
    //Set the name of the Docker image.
    name: "hello-world-knative",
    //Sets the username credential to push the Docker image using the `DOCKER_USERNAME` environment  variable.
    username: "$env{DOCKER_USERNAME}",
    //Sets the password credential to push the Docker image using the `DOCKER_PASSWORD` environment  variable.
    password: "$env{DOCKER_PASSWORD}",
    //Setting the registry URL.
    registry: "index.docker.io/$env{DOCKER_USERNAME}"
}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on new http:Listener(8080) {
    resource function sayHello(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! \n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}
