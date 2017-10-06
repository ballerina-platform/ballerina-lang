import ballerina.net.http;
import ballerina.net.http.response;

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
    resource info1 (http:Request req, http:Response res) {
        json responseJson = {"echo":"resCors"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
         methods:["GET"],
         path : "/test2"
    }
    resource info2 (http:Request req, http:Response res) {
        json responseJson = {"echo":"serCors"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path : "/test3",
        allowOrigins :["http://www.wso2.com", "http://facebook.com", "http://www.amazon.com"],
        allowCredentials : true
    }
    resource info3 (http:Request req, http:Response res) {
        json responseJson = {"echo":"moreOrigins"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}

service<http> hello2 {

    @http:resourceConfig {
         methods:["POST"],
         path : "/test1",
         allowOrigins :["http://www.hello.com"," http://www.facebook.com  "],
         exposeHeaders: ["X-Content-Type-Options","X-PINGOTHER"]
    }
    resource info1 (http:Request req, http:Response res) {
        json responseJson = {"echo":"resOnlyCors"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["PUT"],
        path : "/test2",
        allowMethods :["HEAD", "PUT"],
        allowOrigins :["http://www.bbc.com"," http://www.amazon.com  "],
        exposeHeaders: ["X-Content-Type-Options","X-PINGOTHER"]
    }
    resource info2 (http:Request req, http:Response res) {
        json responseJson = {"echo":"optionsOnly"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
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
    resource info1 (http:Request req, http:Response res) {
        json responseJson = {"echo":"cors"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}

service<http> echo4 {
    @http:resourceConfig {
        methods:["POST"]
    }
    resource info1 (http:Request req, http:Response res) {
        json responseJson = {"echo":"noCors"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["OPTIONS"]
    }
    resource info2 (http:Request req, http:Response res) {
        json responseJson = {"echo":"noCorsOPTIONS"};
        response:setJsonPayload(res, responseJson);
        response:send(res);
    }
}