import ballerina/http;
import ballerina/http.swagger;
@swagger:ServiceInfo {
    title:"Service5",
    tags:[
    @swagger:Tag {
        name:"tag-1",
        description:"first tag"
    },
    @swagger:Tag {
        name:"tag-2",
        description:"second tag"
    }],
    developers:[
    @swagger:Developer {
        name:"John",
        email:"john@abc.com"
    },
    @swagger:Developer {
        name:"Jane",
        email:"jane@abc.com"
    }],
    organization:
    @swagger:Organization {
        name:"ABC",
        url:"http://wwww.abc.com"
    },
    externalDocs:
    @swagger:ExternalDoc {
        description:"find more info here",
        url:"https://swagger.io/about"
    },
    license:
    @swagger:License {
        name:"MIT",
        url:"http://github.com/gruntjs/grunt/blob/master/LICENSE-MIT"
    },
    contact:
    @swagger:Contact {
        name:"Swagger API Team",
        email:"foo@example.com",
        url:"http://madskristensen.net"
    },
    termsOfService:"http://swagger.io/terms/",
    description:"A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
    serviceVersion:"1.0.0"
}
@swagger:Swagger {
    swaggerVersion:"2.0"
}
@swagger:ServiceConfig {
    schemes:["http","https"]
}
@http:configuration {
    basePath:"/api"
}
service<http> Service5 {
    @http:resourceConfig {
        methods:["GET"],
        produces:["application/json"],
        path:"/pets/{id}"
    }
    @swagger:ResourceConfig {
    }
    @swagger:ParametersInfo {
        value:[
        @swagger:ParameterInfo {
            in:"path",
            parameterType:"string",
            required:true,
            name:"id"
        },
        @swagger:ParameterInfo {
            in:"query",
            parameterType:"string",
            required:false,
            name:"tag"
        },
        @swagger:ParameterInfo {
            in:"query",
            parameterType:"integer",
            required:false,
            description:"The limit of the records.",
            name:"limit"
        },
        @swagger:ParameterInfo {
            in:"query",
            parameterType:"boolean",
            required:false,
            description:"Allow Empty values.",
            name:"isEmpty"
        }]
    }
    @swagger:ResourceInfo {
        description:"Returns all pets from the system that the user has access to"
    }
    @swagger:Responses {
        value:[
        @swagger:Response {
            code:"200",
            description:"A list of pets."
        },
        @swagger:Response {
            code:"404",
            description:"No pets found."
        },
        @swagger:Response {
            code:"default",
            description:"unexpected error."
        }]
    }
    resource Resource1 (http:Request req,http:Response res,string id) {
    }
}