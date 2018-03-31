import ballerina/http;
import ballerina/http.swagger;

@swagger:ServiceInfo {
title: "Service1",
serviceVersion: "1.0.0"
}
@swagger:Swagger {
swaggerVersion: "2.0"
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
    resource Resource1 (http:Request req,http:Response res) {
    }
}
