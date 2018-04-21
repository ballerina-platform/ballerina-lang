import ballerina/http;
import ballerina/http.swagger;
@swagger:ServiceInfo {
    title:"Service9",
    description:"A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
    serviceVersion:"1.0.0"
}
@swagger:Swagger {
    swaggerVersion:"2.0"
}
@swagger:ServiceConfig {
}
@http:configuration {
    basePath:"/api"
}
service<http> Service9 {
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
        }]
    }
    @swagger:ResourceInfo {
    }
    @swagger:Responses {
        value:[
        @swagger:Response {
            code:"200",
            description:"Successful"
        }]
    }
    resource Resource1 (http:Request req,http:Response res,string id) {
    }
    @http:resourceConfig {
        methods:["POST"],
        consumes:["application/json"],
        produces:["application/json"],
        path:"/pets/{id}"
    }
    @swagger:ResourceConfig {
    }
    @swagger:ParametersInfo {
        value:[
        @swagger:ParameterInfo {
            in:"body",
            required:false,
            name:"req"
        },
        @swagger:ParameterInfo {
            in:"path",
            parameterType:"string",
            required:true,
            name:"id"
        }]
    }
    @swagger:ResourceInfo {
    }
    @swagger:Responses {
        value:[
        @swagger:Response {
            code:"200",
            description:"Successful"
        }]
    }
    resource Resource2 (http:Request req,http:Response res,string id) {
    }
    @http:resourceConfig {
        methods:["DELETE"],
        produces:["application/json"],
        path:"/pets/{id}"
    }
    @swagger:ResourceConfig {
    }
    @swagger:ParametersInfo {
        value:[
        @swagger:ParameterInfo {
            in:"body",
            required:false,
            name:"req"
        }]
    }
    @swagger:ResourceInfo {
    }
    @swagger:Responses {
        value:[
        @swagger:Response {
            code:"200",
            description:"Successful"
        }]
    }
    resource Resource4 (http:Request req,http:Response res) {
    }
    @http:resourceConfig {
        methods:["PUT"],
        consumes:["application/json"],
        path:"/pets"
    }
    @swagger:ResourceConfig {
    }
    @swagger:ParametersInfo {
        value:[
        @swagger:ParameterInfo {
            in:"body",
            required:false,
            name:"req"
        }]
    }
    @swagger:ResourceInfo {
    }
    @swagger:Responses {
        value:[
        @swagger:Response {
            code:"200",
            description:"Successful"
        }]
    }
    resource Resource3 (http:Request req,http:Response res) {
    }
}