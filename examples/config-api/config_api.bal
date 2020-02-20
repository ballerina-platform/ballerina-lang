import ballerina/config;
import ballerina/http;
import ballerina/log;

// The Ballerina Config API allows you to look up values from configuration
// files, CLI parameters and environment variables. The precedence order for
// configuration resolution is as follows:
//
// * CLI parameters
//
// * Environment variables
//
// * Configuration files
//
//
// If a specific configuration defined in the file is also defined as an
// environment variable, the environment variable takes precedence. Similarly,
// if the same is set as a CLI parameter, it replaces the environment
// variable value.
//
// The configuration file is in TOML(v0.4) format.
//
// The configuration API is particularly useful for configuring services.
// In this example, the port and keystore password are read through the
// configuration API instead of hard coding it in the source file. The
// configuration APIs accept a key and an optional default value. If a
// mapping does not exist for the specified key, the default value
// is returned as the configuration value. The default values of these
// optional configurations are the default values of the return types of
// the functions.
listener http:Listener helloWorldEP
    = new (config:getAsInt("hello.http.port", 9095), config = {
        secureSocket: {
            keyStore: {
                path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: config:getAsString("hello.keystore.password")
            }
        }
});

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorld on helloWorldEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        var result = caller->respond("Hello World!");
        if (result is error) {
            log:printError("Failed to respond to the caller", result);
        }
    }
}
