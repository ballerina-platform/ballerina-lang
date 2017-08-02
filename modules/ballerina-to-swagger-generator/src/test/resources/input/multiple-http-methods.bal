import ballerina.net.http;
import ballerina.net.http.swagger;

@http:configuration {
    basePath:"/api"
}
@swagger:ServiceInfo {
    version: "1.0.0",
    description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification"
}
service<http> Service9 {

    @http:GET{}
    @http:Path{value: "/pets/{id}"}
    @http:Produces {value: ["application/json"]}
    resource Resource1 (message m, @http:PathParam{value: "id"} string id) {
        reply m;
    }

    @http:POST{}
    @http:Path{value: "/pets/{id}"}
    @http:Produces {value: ["application/json"]}
    @http:Produces {value: ["application/json"]}
    resource Resource2 (message m, @http:PathParam{value: "id"} string id) {
        reply m;
    }

    @http:PUT{}
    @http:Path{value: "/pets"}
    @http:Produces {value: ["application/json"]}
    @http:Produces {value: ["application/json"]}
    resource Resource3 (message m) {
        reply m;
    }

    @http:DELETE{}
    @http:Path{value: "/pets/{id}"}
    @http:Produces {value: ["application/json"]}
    resource Resource4 (message m) {
        reply m;
    }
}