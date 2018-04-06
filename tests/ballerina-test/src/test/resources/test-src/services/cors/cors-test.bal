import ballerina/http;

endpoint http:NonListeningServiceEndpoint testEP {
    host: "localhost",
    port: 9090
};

@http:ServiceConfig {
    basePath:"/hello1",
    cors:{
        allowOrigins:["http://www.m3.com", "http://www.hello.com"],
        allowCredentials:true,
        allowHeaders:["CORELATION_ID"],
        exposeHeaders:["CORELATION_ID"],
        maxAge:1
    }
}
service<http:Service> echo1 bind testEP {

    @http:ResourceConfig {
        methods:["POST"],
        path : "/test1",
        cors: {
            allowOrigins :["http://www.wso2.com", "http://www.facebook.com"],
            allowCredentials : true,
            allowHeaders:["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    info1 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"resCors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
         methods:["GET"],
         path : "/test2"
    }
    info2 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"serCors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path : "/test3",
        cors:{
            allowOrigins :["http://www.wso2.com", "http://facebook.com", "http://www.amazon.com"],
            allowCredentials:true
        }
    }
    info3 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"moreOrigins"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

service<http:Service> hello2 bind testEP {

    @http:ResourceConfig {
         methods:["POST"],
         path : "/test1",
         cors: {
            allowOrigins :["http://www.hello.com", " http://www.facebook.com  "],
            exposeHeaders:["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    info1 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"resOnlyCors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["PUT"],
        path : "/test2",
        cors:{
            allowMethods :["HEAD", "PUT"],
            allowOrigins:["http://www.bbc.com", " http://www.amazon.com  "],
            exposeHeaders:["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    info2 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"optionsOnly"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/hello3",
    cors:{
        allowCredentials : true,
        allowMethods:["GET", "PUT"],
        allowOrigins:["http://www.m3.com", "http://www.facebook.com"],
        allowHeaders:["X-Content-Type-Options", "X-PINGOTHER"],
        maxAge:1
    }
}
service<http:Service> echo3 bind testEP {

    @http:ResourceConfig {
        methods:["POST", "PUT"]
    }
    info1 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"cors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

service<http:Service> echo4 bind testEP {
    @http:ResourceConfig {
        methods:["POST"]
    }
    info1 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"noCors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["OPTIONS"]
    }
    info2 (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"echo":"noCorsOPTIONS"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}