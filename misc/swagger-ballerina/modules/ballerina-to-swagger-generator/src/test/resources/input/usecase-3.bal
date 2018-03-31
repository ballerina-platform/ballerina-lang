import ballerina/http;
import ballerina/http.swagger;

@http:ServiceConfig {basePath:"/api"}
@swagger:ServiceInfo {
    serviceVersion: "1.0.0",
    title: "Swagger Petstore",
    description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
    termsOfService: "http://swagger.io/terms/",
    contact: {
        name: "Swagger API Team"
    },
    license: {
        name: "MIT"
    }
}
service<http:Service> Service3 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets",
        produces:["application/json"]
    }
    @swagger:ResourceInfo {
        description: "Returns all pets from the system that the user has access to"
    }
    listPets (endpoint outboundEp, http:Request req) {
        //stub code - fill as necessary
        http:Response resp = {};
        string payload = "Sample listPets Response";
        resp.setStringPayload(payload);
        _ = outboundEp -> respond(resp);
    }
}