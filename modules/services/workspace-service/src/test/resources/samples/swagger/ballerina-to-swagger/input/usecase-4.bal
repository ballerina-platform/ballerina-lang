import ballerina.net.http;
import ballerina.net.http.swagger;

@http:config {basePath:"/api"}
@swagger:ServiceInfo {
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
@http:Consumes {value: ["application/json", "application/xml"]}
@http:Produces {value: ["application/json"]}
@swagger:ServiceConfig {
    schemes:[
        "http"
    ]
}
service<http> Service4 {

    @http:GET{}
    @http:Path{value: "/pets/{id}"}
    @http:Produces {value: ["application/json"]}
    @swagger:Responses {
        value:[
            @swagger:Response {
                code:"200",
                description:"A list of pets."
            }
        ]
    }
    @swagger:ResourceInfo {
        description: "Returns all pets from the system that the user has access to"
    }
    resource Resource1 (message m, @http:PathParam{value: "id"} string id, @http:QueryParam{value: "tag"} string tag) {
        reply m;
    }
}