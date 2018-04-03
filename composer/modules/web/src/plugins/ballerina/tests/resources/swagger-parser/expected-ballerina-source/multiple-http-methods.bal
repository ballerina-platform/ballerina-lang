import ballerina/http;
import ballerina/http.swagger;
@swagger:ServiceInfo {
    title:"Service5",
    termsOfService:"http://swagger.io/terms/",
    description:"A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
    serviceVersion:"1.0.0"
}
@swagger:Swagger {
    swaggerVersion:"2.0"
}
@swagger:ServiceConfig {
    host:"localhost:4545"
}
@http:configuration {
    basePath:"/api",
    port:4545,
    host:"localhost"
}
service<http> Service5 {
    @http:resourceConfig {
        methods:["GET","POST"],
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
    resource Resource1 (http:Request req,http:Response res,string id) {
    }
    @http:resourceConfig {
        methods:["PUT","POST","DELETE"],
        produces:["application/json"],
        path:"/pets/firstDoggy"
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
    resource Resource2 (http:Request req,http:Response res) {
    }
    @http:resourceConfig {
        methods:["GET"],
        produces:["application/json"],
        path:"/pets/secondDoggy"
    }
    @swagger:ResourceConfig {
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