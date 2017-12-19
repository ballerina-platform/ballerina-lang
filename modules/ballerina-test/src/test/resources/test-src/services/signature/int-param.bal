import ballerina.net.http;

@http:configuration {basePath:"/signature"}
service<http> echo {
    resource echo1 (http:Connection con, int key, http:Request req ) {
        http:Response res = {};
    }
}
