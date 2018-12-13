import ballerina/http;
import ballerina/log;

// helloWorldEP listener endpoint is configured to communicate through https.
// It is configured to listen on port 9095. As this is an https Listener,
// it is required to give the PKCS12 keystore file location and it's password.
http:ServiceEndpointConfiguration helloWorldEPConfig = {
secureSocket: {
keyStore: {
path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
password: "ballerina"
}
}
};

listener http:Listener helloWorldEP = new(9095, config = helloWorldEPConfig);

@http:ServiceConfig {
basePath: "/hello"
}
service helloWorld on helloWorldEP {

@http:ResourceConfig {
methods: ["GET"],
path: "/"
}
resource function sayHello(http:Caller caller, http:Request req) {
http:Response res = new;
res.setPayload("Hello World!");
var result = caller->respond(res);
if (result is error) {
log:printError("Failed to respond", err = result);
}
}
}

// This is a client endpoint configured to connect to the above https service.
// As this is a 1-way SSL connection, the client needs to provide
// trust store file path and it's password.
http:ClientEndpointConfig clientEPConfig = {
secureSocket: {
trustStore: {
path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
password: "ballerina"
}
}
};
// You have to run the service before running this main function.
public function main() {
http:Client clientEP = new("https://localhost:9095",
config = clientEPConfig);
// Sends an outbound request.
var resp = clientEP->get("/hello/");
if (resp is http:Response) {
var payload = resp.getTextPayload();
if (payload is string) {
log:printInfo(payload);
} else if (payload is error) {
log:printError(<string>payload.detail().message);
}
} else if (resp is error) {
log:printError(<string>resp.detail().message);
}
}
