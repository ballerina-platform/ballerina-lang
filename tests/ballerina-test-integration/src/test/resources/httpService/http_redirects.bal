import ballerina/io;
import ballerina/http;
import ballerina/mime;

endpoint http:Listener serviceEndpoint {
    port:9090
};

endpoint http:Listener serviceEndpoint2 {
    port:9093
};

endpoint http:Client endPoint {
   url: "http://localhost:9090",
   followRedirects : { enabled : true, maxCount : 5 }
};

@http:ServiceConfig {
    basePath:"/service1"
}
service<http:Service> testRedirect bind serviceEndpoint {

    @http:ResourceConfig {
        path:"/"
    }
    redirectClient (endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint -> get("/redirect1");
        match response {
            error connectorErr => {io:println("Connector error!");}
            http:Response httpResponse => {
                    httpResponse.setPayload(httpResponse.resolvedRequestedURI);
                  _ = client -> respond(httpResponse);
            }
        }
    }
}

@http:ServiceConfig {
      basePath:"/redirect1"
}
service<http:Service> redirect1 bind serviceEndpoint {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    redirect1 (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["http://localhost:9093/redirect2"]);
    }
}

@http:ServiceConfig {
    basePath:"/redirect2"
}
service<http:Service> redirect2 bind serviceEndpoint2 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    redirect2 (endpoint client, http:Request req) {
        http:Response res = new;
        res. setPayload("hello world");
        _ = client -> respond( res);
    }
}

