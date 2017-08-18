import ballerina.net.http;
import ballerina.net.http.swagger;

@swagger:ServiceInfo {
title: "Service5",
version: "1.0.0",
description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
termsOfService: "http://swagger.io/terms/"
}
@swagger:Swagger {
version: "2.0"
}
@swagger:ServiceConfig {
host: "http://:localhost:4545"
}
@http:configuration {
basePath: "/api",
host: "localhost",
port: 4545
}
service<http> Service5 {

    @http:resourceConfig {
    methods: [ "GET", "POST"],
path: "/pets/{id}",
produces: [ "application/json"]
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
    resource Resource1 (message m, @http:PathParam{value:"id"} string id) {
        reply m;
    }

    @http:resourceConfig {
    methods: [ "PUT", "POST", "DELETE"],
path: "/pets/firstDoggy",
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
    resource Resource2 (message m) {
        reply m;
    }

    @http:resourceConfig {
    methods: [ "GET"],
path: "/pets/secondDoggy",
produces: [ "application/json"]
}
    @swagger:ResourceConfig {}
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
