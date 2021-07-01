import ballerina/http;
import ballerina/log;
import ballerina/kubernetes;
import ballerina/openshift;

//Add the `@kubernetes:Service` to a listener endpoint to expose the endpoint as a Kubernetes Service.
@kubernetes:Service {}
//Add the `@openshift:Route` to expose the Kubernetes Service through an OpenShift Route.
@openshift:Route {
    host: "www.oc-example.com"
}
listener http:Listener helloEP = new(9090);

//Add the `@kubernetes:Deployment` annotation to a Ballerina service to generate a Kuberenetes Deployment for a Ballerina module.
@kubernetes:Deployment {
    //OpenShift project name.
    namespace: "hello-api",
    //IP and port of the OpenShift docker registry. If you are using minishift, use the `minishift openshift registry` to find the Docker registry.
    registry: "172.30.1.1:5000",
    //Generate a Docker image with the name `172.30.1.1:5000/hello-api/hello-service:v1.0`.
    image: "hello-service:v1.0",
    //Disable the image being built by default so that the OpenShift BuildConfig can build it.
    buildImage: false,
    //Generate the OpenShift BuildConfig for building the Docker image.
    buildExtension: openshift:BUILD_EXTENSION_OPENSHIFT
}
@http:ServiceConfig {
    basePath: "/hello"
}
service hello on helloEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{user}"
    }
    resource function sayHello(http:Caller caller, http:Request request, string user) {
        string payload = string `Hello ${<@untainted string> user}!`;
        var responseResult = caller->respond(payload);
        if (responseResult is error) {
            error err = responseResult;
            log:printError("Error sending response", err);
        }
    }
}
