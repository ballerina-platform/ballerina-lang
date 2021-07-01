import ballerina/config;
import ballerina/http;
import ballerina/log;
import ballerina/kubernetes;

//Add the `@kubernetes:Service` to a listener endpoint to expose the endpoint as a Kubernetes Service.
@kubernetes:Service {
    //Service type is `NodePort`.
    serviceType: "NodePort"
}
//Add the `@kubernetes:Ingress` to a listener endpoint to expose the endpoint as a Kubernetes Ingress.
@kubernetes:Ingress {
    //Hostname of the service is `abc.com`.
    hostname: "abc.com"
}
listener http:Listener helloWorldEP = new(9090, config = {
    //Ballerina automatically creates Kubernetes secrets for the keystore and truststore when the `@kubernetes:Service`
    //annotation is added to the endpoint.
    secureSocket: {
        keyStore: {
            path: "./security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "./security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

//Add the `@kubernetes:ConfigMap` annotation to a Ballerina service to mount configs to the container.
@kubernetes:ConfigMap {
    //Path to the ballerina.conf file.
    //If a releative path is provided, the path should be releative to where the `ballerina build` command is executed.
    conf: "./ballerina.conf"
}
//Add the `@kubernetes:Deployment` annotation to a Ballerna service to generate Kuberenetes Deployment for a Ballerina module.
@kubernetes:Deployment {
    //Enable Kubernetes liveness probe to this service.
    livenessProbe: true,
    //Generate a Docker image with the name `kubernetes:v1.0`.
    image: "kubernetes:v.1.0"
    //If you are using minikube, uncomment and change the following values accordingly.
    ////dockerHost:"tcp://<minikube IP>:2376",
    ////dockerCertPath:"<HOME_DIRECTORY>/.minikube/certs"
}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloWorldEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/config/{user}"
    }
    resource function getConfig(http:Caller outboundEP, http:Request request, string user) {
        string userId = getConfigValue(user, "userid");
        string groups = getConfigValue(user, "groups");
        json payload = {
            userId: userId,
            groups: groups
        };
        var responseResult = outboundEP->respond(payload);
        if (responseResult is error) {
            error err = responseResult;
            log:printError("Error sending response", err);
        }
    }
}

function getConfigValue(string instanceId, string property) returns (string) {
    string key = <@untainted string> (instanceId + "." + property);
    return config:getAsString(key, "Invalid User");
}
