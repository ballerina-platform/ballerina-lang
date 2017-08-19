import ballerina.net.http;
import ballerina.net.http.swagger;

@swagger:ServiceInfo {
title: "Service9",
version: "1.0.0",
description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification"
}
@swagger:Swagger {
version: "2.0"
}
@swagger:ServiceConfig {}
@http:configuration {
basePath: "/api"
}
service<http> Service9 {

    @http:resourceConfig {
    methods: [ "GET"],
path: "/pets/{id}",
produces: [ "application/json"]
}
    @swagger:ResourceConfig {}
    @swagger:ParametersInfo {
    value: [ @swagger:ParameterInfo {
in: "path",
name: "id",
required: true,
parameterType: "string"
}]
}
    @swagger:ResourceInfo {}
    @swagger:Responses {
    value: [ @swagger:Response {
code: "200",
description: "Successful"
}]
}
    resource Resource1 (message m, @http:PathParam{value:"id"} string id) {
        reply m;
    }

    @http:resourceConfig {
    methods: [ "POST"],
path: "/pets/{id}",
produces: [ "application/json"],
consumes: [ "application/json"]
}
    @swagger:ResourceConfig {}
    @swagger:ParametersInfo {
    value: [ @swagger:ParameterInfo {
in: "body",
name: "m",
required: false
}, @swagger:ParameterInfo {
in: "path",
name: "id",
required: true,
parameterType: "string"
}]
}
    @swagger:ResourceInfo {}
    @swagger:Responses {
    value: [ @swagger:Response {
code: "200",
description: "Successful"
}]
}
    resource Resource2 (message m, @http:PathParam{value:"id"} string id) {
        reply m;
    }

    @http:resourceConfig {
    methods: [ "DELETE"],
path: "/pets/{id}",
produces: [ "application/json"]
}
    @swagger:ResourceConfig {}
    @swagger:ParametersInfo {
    value: [ @swagger:ParameterInfo {
in: "body",
name: "m",
required: false
}]
}
    @swagger:ResourceInfo {}
    @swagger:Responses {
    value: [ @swagger:Response {
code: "200",
description: "Successful"
}]
}
    resource Resource4 (message m) {
        reply m;
    }

    @http:resourceConfig {
    methods: [ "PUT"],
path: "/pets",
consumes: [ "application/json"]
}
    @swagger:ResourceConfig {}
    @swagger:ParametersInfo {
    value: [ @swagger:ParameterInfo {
in: "body",
name: "m",
required: false
}]
}
    @swagger:ResourceInfo {}
    @swagger:Responses {
    value: [ @swagger:Response {
code: "200",
description: "Successful"
}]
}
    resource Resource3 (message m) {
        reply m;
    }
}
