import ballerina/http;
import ballerina/http.swagger;

@http:ServiceConfig {basePath:"/api"}
@swagger:ServiceInfo {
    serviceVersion: "1.0.0",
    description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
    termsOfService: "http://swagger.io/terms/",
    contact: {
        name: "Swagger API Team"
    },
    license: {
        name: "MIT"
    }
}

service<http:Service> Service4 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets/{id}",
        produces:["application/json"]
    }
    @swagger:ResourceInfo {
        description: "Returns all pets from the system that the user has access to"
    }
    showPetById (endpoint outboundEp, http:Request req, string petId) {
        http:Response resp = {};
        string payload = "Sample showPetById Response";
        resp.setStringPayload(payload);
        _ = outboundEp -> respond(resp);
    }
}