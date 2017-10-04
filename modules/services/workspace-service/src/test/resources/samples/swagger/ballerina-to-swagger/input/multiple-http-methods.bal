import ballerina.net.http;
import ballerina.net.http.response;
import ballerina.net.http.swagger;

@http:configuration {
    basePath:"/api",
    host: "localhost",
    port: 4545
}
@swagger:ServiceInfo {
    serviceVersion: "1.0.0",
    description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
    termsOfService: "http://swagger.io/terms/"
}
service<http> Service5 {
    @http:resourceConfig{
        methods: ["GET", "POST"],
        path: "/pets/{id}",
        produces: ["application/json"]
    }
    resource Resource1 (http:Request req, http:Response res, @http:PathParam{value: "id"} string id) {
        response:send(res);
    }

    @http:resourceConfig{
        methods: ["PUT", "POST", "DELETE"],
        path: "/pets/firstDoggy",
        produces: ["application/json"]
    }
    resource Resource2 (http:Request req, http:Response res) {
        response:send(res);
    }

    @http:resourceConfig{
        methods: ["GET"],
        path: "/pets/secondDoggy",
        produces: ["application/json"]
    }
    resource Resource3 (http:Request req, http:Response res) {
        response:send(res);
    }
}