import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningService testEP {
    port:9090
};

@http:serviceConfig {
    basePath:"/hello1",
    endpoints:[testEP],
    cors: {
        allowOrigins :["http://www.m3.com", "http://www.hello.com"],
        allowCredentials : true,
        allowHeaders :["CORELATION_ID"],
        exposeHeaders :["CORELATION_ID"],
        maxAge : 1
    }
}
service<http:Service> echo1 {

    @http:resourceConfig {
        methods:["POST"],
        path : "/test1",
        cors: {
            allowOrigins :["http://www.wso2.com", "http://www.facebook.com"],
            allowCredentials : true,
            allowHeaders:["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    info1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"resCors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
         methods:["GET"],
         path : "/test2"
    }
    info2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"serCors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path : "/test3",
        cors:{
            allowOrigins :["http://www.wso2.com", "http://facebook.com", "http://www.amazon.com"],
            allowCredentials:true
        }
    }
    info3 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"moreOrigins"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    endpoints:[testEP]
}
service<http:Service> hello2 {

    @http:resourceConfig {
         methods:["POST"],
         path : "/test1",
         cors: {
            allowOrigins :["http://www.hello.com", " http://www.facebook.com  "],
            exposeHeaders:["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    info1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"resOnlyCors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["PUT"],
        path : "/test2",
        cors:{
            allowMethods :["HEAD", "PUT"],
            allowOrigins:["http://www.bbc.com", " http://www.amazon.com  "],
            exposeHeaders:["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
    info2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"optionsOnly"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/hello3",
    endpoints:[testEP],
    cors:{
        allowCredentials : true,
        allowMethods:["GET", "PUT"],
        allowOrigins:["http://www.m3.com", "http://www.facebook.com"],
        allowHeaders:["X-Content-Type-Options", "X-PINGOTHER"],
        maxAge:1
    }
}
service<http:Service> echo3 {

    @http:resourceConfig {
        methods:["POST", "PUT"]
    }
    info1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"cors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    endpoints:[testEP]
}
service<http:Service> echo4 {
    @http:resourceConfig {
        methods:["POST"]
    }
    info1 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"noCors"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["OPTIONS"]
    }
    info2 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"noCorsOPTIONS"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}