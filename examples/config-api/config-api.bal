import ballerina/config;
import ballerina/http;
import ballerina/io;
import ballerina/mime;

// Using the Ballerina config API, you can look up values from config files, CLI parameters and
// environment variables. The precedence order for config resolution is as follows: <br>
// * CLI parameters <br>
// * Environment variables <br>
// * Config files <br><br>
// If a particular config defined in the file is also defined as an environment variable, the environment
// variable takes precedence. Similarly, if the same is set as a CLI parameter, it replaces the environment
// variable value. <br>
// The configs are simply arbitrary key/value pairs with slight structure to it.
endpoint http:Listener helloWorldEP {
    // The config API is particularly useful for configuring services. In this example, the port and keystore password
    // are read through the config API, instead of hard coding it in the source file. The configuration APIs accept
    // a key and an optional default value. In case there isn't a mapping for the specified key, the default value
    // is returned as the config value. The default values of these optional default configs are the respective
    // default values of the types the functions return.
    port:config:getAsInt("hello.http.port", default = 9095),
    secureSocket:{
        keyStore:{
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:config:getAsString("hello.keystore.password")
        }
    }
};

@http:ServiceConfig {
    basePath:"/hello"
}
service helloWorld bind helloWorldEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Hello World!");
        _ = caller->respond(res);
    }
}
