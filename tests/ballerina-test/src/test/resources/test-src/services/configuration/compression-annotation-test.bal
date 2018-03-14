import ballerina.net.http;
import ballerina.net.http.mock;

endpoint<mock:NonListeningService> mockEP {
    port:9090
}

@http:serviceConfig {endpoints:[mockEP]}
service<http:Service> defaultCompressionValue {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource test1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {endpoints:[mockEP], compressionEnabled : true}
service<http:Service> explicitlyCompressionEnabled {
     @http:resourceConfig {
        methods:["GET"],
        path:"/"
        }
    resource test2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {endpoints:[mockEP], compressionEnabled : false}
service<http:Service> explicitlyCompressionDisabled {
    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource test3 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello World!!!");
        _ = conn -> respond(res);
    }
}