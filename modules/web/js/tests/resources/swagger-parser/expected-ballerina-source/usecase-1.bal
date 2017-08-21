import ballerina.net.http;
import ballerina.net.http.swagger;

@swagger:ServiceInfo {
title: "Service1",
version: "1.0.0"
}
@swagger:Swagger {
version: "2.0"
}
@swagger:ServiceConfig {}
@http:configuration {
basePath: "/Service1"
}
service<http> Service1 {

    @http:resourceConfig {
    methods: [ "GET"],
path: "/Resource1"
}
    @swagger:ResourceConfig {}
    @swagger:ResourceInfo {}
    @swagger:Responses {
    value: [ @swagger:Response {
code: "200",
description: "Successful"
}]
}
    resource Resource1 (message m) {
        reply m;
    }
}
