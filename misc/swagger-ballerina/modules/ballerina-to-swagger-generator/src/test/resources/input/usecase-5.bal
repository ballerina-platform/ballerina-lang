import ballerina/http;
import ballerina/http.swagger;

@http:ServiceConfig {basePath:"/api"}
@swagger:ServiceInfo {
    serviceVersion: "1.0.0",
    description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
    termsOfService: "http://swagger.io/terms/",
    contact: {
        name: "Swagger API Team",
        email: "foo@example.com",
        url: "http://madskristensen.net"
    },
    license: {
        name: "MIT",
        url: "http://github.com/gruntjs/grunt/blob/master/LICENSE-MIT"
    },
    externalDocs: {
        description: "find more info here",
        url: "https://swagger.io/about"
    },
    tags: [
        {
            name: "tag-1",
            description: "first tag"
        },
        {
            name: "tag-2",
            description: "second tag"
        }
    ]
}

service<http:Service> Service5 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/pets/{id}",
        produces:["application/json"]
    }
    @swagger:ResourceInfo {
        description: "Returns all pets from the system that the user has access to",
        parameters:[
                   {
                       name: "limit",
                       description: "The limit of the records."
                   },
                   {
                       name: "isEmpty",
                       description: "Allow Empty values."
                   }
                   ]
    }
    showPetById (endpoint outboundEp, http:Request req, string petId, string tag, int limit, boolean isEmpty) {
        http:Response resp = {};
        string payload = "Sample showPetById Response";
        resp.setStringPayload(payload);
        _ = outboundEp -> respond(resp);
    }
}
