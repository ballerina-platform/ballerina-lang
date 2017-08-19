import ballerina.net.http;
import ballerina.net.http.swagger;

@swagger:ServiceInfo {
title: "Swagger Petstore",
version: "1.0.0",
description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
termsOfService: "http://swagger.io/terms/",
contact: @swagger:Contact {
name: "Swagger API Team"
},
license: @swagger:License {
name: "MIT"
}
}
@swagger:Swagger {
version: "2.0"
}
@swagger:ServiceConfig {
schemes: [ "http"]
}
@http:configuration {
basePath: "/api"
}
service<http> SwaggerPetstore {

    @http:resourceConfig {
    methods: [ "GET"],
path: "/pets",
produces: [ "application/json"]
}
    @swagger:ResourceConfig {}
    @swagger:ResourceInfo {
    description: "Returns all pets from the system that the user has access to"
}
    @swagger:Responses {
    value: [ @swagger:Response {
code: "200",
description: "A list of pets."
}]
}
    resource Resource1 (message m) {
        reply m;
    }
}
