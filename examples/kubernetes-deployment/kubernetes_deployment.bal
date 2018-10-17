import ballerina/config;
import ballerina/http;
import ballerina/log;
import ballerinax/kubernetes;

//Add @kubernetes:Service to a listner endpoint to expose the endpoint as Kubernetes Service.
@kubernetes:Service {
    //Service type is `NodePort`.
    serviceType: "NodePort"
}
//Add @kubernetes:Ingress to a listner endpoint to expose the endpoint as Kubernetes Ingress.
@kubernetes:Ingress {
    //Hostname of the service is `abc.com`.
    hostname: "abc.com"
}
endpoint http:Listener helloWorldEP {
    port: 9090,
    //Ballerina will automatically create Kubernetes secrets for the keystore and trustore when @kubernetes:Service
    //annotation is added to the endpoint.
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

//Add @kubernetes:ConfigMap annotation to a Ballerna service to mount configs to the container.
@kubernetes:ConfigMap {
    //Path to the ballerina.conf file.
    //If providing releative path, then the path should be releative to where `ballerina build` command executed.
    ballerinaConf: "./ballerina.conf"
}
//Add @kubernetes:Deployment annotation to a Ballerna service generate Kuberenetes Deployment for a Ballerina module.
@kubernetes:Deployment {
    //Enable Kubernetes liveness probe to this service.
    enableLiveness: true,
    //Generate a single yaml file.
    singleYAML: true,
    //Genrate Docker image with name `kubernetes:v1.0`.
    image: "kubernetes:v.1.0"
    //Uncomment and change the following values accordingly if you are using minikube.
    ////,dockerHost:"tcp://<minikube IP>:2376",
    ////dockerCertPath:"<HOME_DIRECTORY>/.minikube/certs"

}
@http:ServiceConfig {
    basePath: "/helloWorld"
}
service<http:Service> helloWorld bind helloWorldEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/config/{user}"
    }
    getConfig(endpoint outboundEP, http:Request request, string user) {
        string userId = getConfigValue(user, "userid");
        string groups = getConfigValue(user, "groups");
        string payload = "{userId: " + userId + ", groups: " + groups + "} \n";
        outboundEP->respond(payload) but {
            error err => log:printError(err.message, err = err)
        };
    }
}

function getConfigValue(string instanceId, string property) returns (string) {
    string key = untaint instanceId + "." + untaint property;
    return config:getAsString(key, default = "Invalid User");
}
