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
// The configuration file format is a subset of the TOML file format. It
// supports string, int, float and boolean value types.
endpoint http:Listener helloWorldEP {
    // The configuration API is particularly useful for configuring services.
    // In this example, the port and keystore password are read through the
    // configuration API instead of hard coding it in the source file. The
    // configuration APIs accept a key and an optional default value. If a
    // mapping does not exist for the specified key, the default value
    // is returned as the configuration value. The default values of these
    // optional configurations are the default values of the return types of
    // the functions.
    port: config:getAsInt("hello.http.port", default = 9095),
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: config:getAsString("hello.keystore.password")
        }
    }
};

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorld bind helloWorldEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello World!");
        caller->respond(res)
        but { error e =>
        log:printError("Failed to respond to the caller", err = e) };
    }
}
