import ballerina/io;
import ballerina/runtime;
import ballerina/http;


endpoint http:Listener echoEP1 {
    port:9090
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo1 bind echoEP1 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo1 (endpoint caller, http:Request req) {
        addExitHooks();
        var payload = req.getTextPayload();
        match payload {
            string payloadValue => {
                http:Response resp = new;
                resp.setTextPayload(untaint payloadValue);
                _ = caller -> respond(resp);
            }
            any | () => {
                io:println("Error while fetching string payload");
            }
        }
    }
}


public function addExitHooks() {
    int a = 4;

    (function() returns ()) fn1 = () => {
        io:println("Exit hook one invoked");
    };
    runtime:addExitHook(fn1);

    var fn2 = () => {
        io:println("Exit hook two invoked with var : ", a);
    };

    runtime:addExitHook(fn2);
}

