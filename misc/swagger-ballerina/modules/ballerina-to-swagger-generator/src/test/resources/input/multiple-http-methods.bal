import ballerina/http;
import ballerina/http.swagger;

@http:configuration {
    basePath:"/api"
}
@swagger:ServiceInfo {
    version: "1.0.0",
    description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification"
}
service<http> Service9 {

    @http:resourceConfig {
        methods:["GET"],
        path:"/pets/{id}",
        produces:["application/json"]
    }
    resource Resource1 (message m, @http:PathParam{value: "id"} string id) {
        reply m;
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/pets/{id}",
        produces:["application/json"]
    }
    resource Resource2 (message m, @http:PathParam{value: "id"} string id) {
        reply m;
    }

    @http:resourceConfig {
        methods:["PUT"],
        path:"/pets",
        produces:["application/json"]
    }
    resource Resource3 (message m) {
        reply m;
    }

    @http:resourceConfig {
        methods:["DELETE"],
        path:"/pets/{id}",
        produces:["application/json"]
    }
    resource Resource4 (message m) {
        reply m;
    }
}