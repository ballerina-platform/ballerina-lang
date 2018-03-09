import ballerina.net.http;

@http:configuration {
    basePath:"/hello1",
    allowOrigins :["http://www.m3.com", "http://www.hello.com"],
    allowCredentials : true,
    allowHeaders : ["CORELATION_ID"],
    exposeHeaders : ["CORELATION_ID"],
    maxAge : 1
}
service<http> echo1 {

    @http:resourceConfig {
         methods:["POST"],
         path : "/test1",
         allowOrigins :["http://www.wso2.com", "http://www.facebook.com"],
         allowCredentials : true,
         allowHeaders: ["X-Content-Type-Options", "X-PINGOTHER"]
    }
    resource info1 (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"resCors"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
         methods:["GET"],
         path : "/test2"
    }
    resource info2 (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"serCors"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path : "/test3",
        allowOrigins :["http://www.wso2.com", "http://facebook.com", "http://www.amazon.com"],
        allowCredentials : true
    }
    resource info3 (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"moreOrigins"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}

service<http> hello2 {

    @http:resourceConfig {
         methods:["POST"],
         path : "/test1",
         allowOrigins :["http://www.hello.com"," http://www.facebook.com  "],
         exposeHeaders: ["X-Content-Type-Options","X-PINGOTHER"]
    }
    resource info1 (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"resOnlyCors"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["PUT"],
        path : "/test2",
        allowMethods :["HEAD", "PUT"],
        allowOrigins :["http://www.bbc.com"," http://www.amazon.com  "],
        exposeHeaders: ["X-Content-Type-Options","X-PINGOTHER"]
    }
    resource info2 (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"optionsOnly"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}

@http:configuration {
    basePath:"/hello3",
    allowCredentials : true,
    allowMethods :["GET", "PUT"],
    allowOrigins :["http://www.m3.com","http://www.facebook.com"],
    allowHeaders: ["X-Content-Type-Options","X-PINGOTHER"],
    maxAge : 1
}
service<http> echo3 {

    @http:resourceConfig {
        methods:["POST", "PUT"]
    }
    resource info1 (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"cors"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}

service<http> echo4 {
    @http:resourceConfig {
        methods:["POST"]
    }
    resource info1 (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"noCors"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["OPTIONS"]
    }
    resource info2 (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"echo":"noCorsOPTIONS"};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }
}