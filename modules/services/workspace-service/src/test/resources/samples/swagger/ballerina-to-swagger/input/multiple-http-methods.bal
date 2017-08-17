import ballerina.net.http;
import ballerina.net.http.swagger;

@http:configuration {
    basePath:"/api",
    host: "localhost",
    port: 4545
}
@swagger:ServiceInfo {
    version: "1.0.0",
    description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
    termsOfService: "http://swagger.io/terms/"
}
service<http> Service5 {
    @http:resourceConfig{
        methods: ["GET", "POST"],
        path: "/pets/{id}",
        produces: ["application/json"]
    }
    resource Resource1 (message m, @http:PathParam{value: "id"} string id) {
        reply m;
    }

    @http:resourceConfig{
        methods: ["PUT", "POST", "DELETE"],
        path: "/pets/firstDoggy",
        produces: ["application/json"]
    }
    resource Resource2 (message m) {
        reply m;
    }

    @http:resourceConfig{
        methods: ["GET"],
        path: "/pets/secondDoggy",
        produces: ["application/json"]
    }
    resource Resource3 (message m) {
        reply m;
    }
}