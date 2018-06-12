import ballerina/io;
import ballerina/http;
import ballerina/mime;

endpoint http:Listener serviceEndpoint {
    port:9090
};

endpoint http:Listener serviceEndpoint2 {
    port:9093
};

endpoint http:Client endPoint1 {
   url: "http://localhost:9090",
   followRedirects : { enabled : true, maxCount : 3 }
};

endpoint http:Client endPoint2 {
    url: "http://localhost:9090",
    followRedirects : { enabled : true, maxCount : 5 }
};

endpoint http:Client endPoint3 {
    url: "http://localhost:9093",
    followRedirects : { enabled : true}
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
        var response = endPoint1 -> get("/redirect1");
        http:Response finalResponse = new;
        match response {
            error connectorErr => {io:println("Connector error!");}
            http:Response httpResponse => {
                  finalResponse.setPayload(httpResponse.resolvedRequestedURI);
                  _ = client -> respond(finalResponse);
            }
        }
    }

    @http:ResourceConfig {
        path:"/maxRedirect"
    }
    maxRedirectClient (endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint1 -> get("/redirect1/round1");
        match response {
            error connectorErr => {io:println("Connector error!");}
            http:Response httpResponse => {
                string value;
                if (httpResponse.hasHeader(http:LOCATION)) {
                    value = httpResponse.getHeader(http:LOCATION);
                }
                value = value + ":" + httpResponse.resolvedRequestedURI;
                _ = client -> respond(value);
            }
        }
    }

    @http:ResourceConfig {
        path:"/crossDomain"
    }
    crossDomain (endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint2 -> get("/redirect1/round1");
        match response {
            error connectorErr => {io:println("Connector error!");}
            http:Response httpResponse => {
                string value = check httpResponse.getTextPayload();
                value = value + ":" + httpResponse.resolvedRequestedURI;
                _ = client -> respond(value);
            }
        }
    }

    @http:ResourceConfig {
        path:"/noRedirect"
    }
    NoRedirect (endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint3 -> get("/redirect2");
        match response {
            error connectorErr => {io:println("Connector error!");}
            http:Response httpResponse => {
                string value = check httpResponse.getTextPayload();
                value = value + ":" + httpResponse.resolvedRequestedURI;
                _ = client -> respond(value);
            }
        }
    }

    @http:ResourceConfig {
        path:"/qpWithRelativePath"
    }
    qpWithRelativePath (endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint2 -> get("/redirect1/qpWithRelativePath");
        match response {
            error connectorErr => {io:println("Connector error!");}
            http:Response httpResponse => {
                string value = check httpResponse.getTextPayload();
                value = value + ":" + httpResponse.resolvedRequestedURI;
                _ = client -> respond(value);
            }
        }
    }

    @http:ResourceConfig {
        path:"/qpWithAbsolutePath"
    }
    qpWithAbsolutePath (endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint2 -> get("/redirect1/qpWithAbsolutePath");
        match response {
            error connectorErr => {io:println("Connector error!");}
            http:Response httpResponse => {
                string value = check httpResponse.getTextPayload();
                value = value + ":" + httpResponse.resolvedRequestedURI;
                _ = client -> respond(value);
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

    @http:ResourceConfig {
        methods:["GET"],
        path:"/round1"
    }
    round1 (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["/redirect1/round2"]);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/round2"
    }
    round2 (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["/redirect1/round3"]);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/round3"
    }
    round3 (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["/redirect1/round4"]);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/round4"
    }
    round4 (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["/redirect1/round5"]);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/round5"
    }
    round5 (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["http://localhost:9093/redirect2"]);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/qpWithRelativePath"
    }
    qpWithRelativePath (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["/redirect1/processQP?key=value&lang=ballerina"]);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/qpWithAbsolutePath"
    }
    qpWithAbsolutePath (endpoint client, http:Request req) {
        http:Response res = new;
        _ = client -> redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["http://localhost:9090/redirect1/processQP?key=value&lang=ballerina"]);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/processQP"
    }
    processQP (endpoint client, http:Request req) {
        map<string> paramsMap = req.getQueryParams();
        string returnVal = paramsMap.key + ":" + paramsMap.lang;
        _ = client -> respond(returnVal);
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
