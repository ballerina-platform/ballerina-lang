import ballerina/http;
import ballerina/config;

@http:ServiceConfig {
    endpoints: [],
    host: "",
    compression: {
        enable: "AUTO",
        contentTypes: []
    },
    chunking: "AUTO",
    cors: {},
    versioning: {},
    auth: {},
    a
}
service helloService on new http:Listener(8080) {
    resource function sayHello(http:Caller caller, http:Request request) {
        
    }
}