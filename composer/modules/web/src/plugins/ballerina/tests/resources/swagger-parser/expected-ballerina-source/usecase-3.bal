import ballerina/http;
import ballerina/http.swagger;
@swagger:ServiceInfo {
    title:"Swagger Petstore",
    license:
    @swagger:License {
        name:"MIT"
    },
    contact:
    @swagger:Contact {
        name:"Swagger API Team"
    },
    termsOfService:"http://swagger.io/terms/",
    description:"A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
    serviceVersion:"1.0.0"
}
@swagger:Swagger {
    swaggerVersion:"2.0"
}
@swagger:ServiceConfig {
    schemes:["http"]
}
@http:configuration {
    basePath:"/api"
}
service<http> SwaggerPetstore {
    @http:resourceConfig {
        methods:["GET"],
        produces:["application/json"],
        path:"/pets"
    }
    @swagger:ResourceConfig {
    }
    @swagger:ResourceInfo {
        description:"Returns all pets from the system that the user has access to"
    }
    @swagger:Responses {
        value:[
        @swagger:Response {
            code:"200",
            description:"A list of pets."
        }]
    }
    resource Resource1 (http:Request req,http:Response res) {
    }
}