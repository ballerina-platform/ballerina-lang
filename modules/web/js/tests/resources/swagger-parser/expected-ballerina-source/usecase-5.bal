import ballerina.net.http;
import ballerina.net.http.swagger;

@swagger:ServiceInfo {
title: "Service5",
version: "1.0.0",
description: "A sample API that uses a petstore as an example to demonstrate features in the swagger-2.0 specification",
termsOfService: "http://swagger.io/terms/",
contact: @swagger:Contact {
name: "Swagger API Team",
url: "http://madskristensen.net",
email: "foo@example.com"
},
license: @swagger:License {
name: "MIT",
url: "http://github.com/gruntjs/grunt/blob/master/LICENSE-MIT"
},
externalDocs: @swagger:ExternalDoc {
description: "find more info here",
url: "https://swagger.io/about"
},
organization: @swagger:Organization {
name: "ABC",
url: "http://wwww.abc.com"
},
developers: [ @swagger:Developer {
name: "John",
email: "john@abc.com"
}, @swagger:Developer {
name: "Jane",
email: "jane@abc.com"
}],
tags: [ @swagger:Tag {
name: "tag-1",
description: "first tag"
}, @swagger:Tag {
name: "tag-2",
description: "second tag"
}]
}
@swagger:Swagger {
version: "2.0"
}
@swagger:ServiceConfig {
schemes: [ "http", "https"]
}
@http:configuration {
basePath: "/api"
}
service<http> Service5 {

    @http:resourceConfig {
    methods: [ "GET"],
path: "/pets/{id}",
produces: [ "application/json"]
}
    @swagger:ResourceConfig {}
    @swagger:ParametersInfo {
    value: [ @swagger:ParameterInfo {
in: "path",
name: "id",
required: true,
parameterType: "string"
}, @swagger:ParameterInfo {
in: "query",
name: "tag",
required: false,
parameterType: "string"
}, @swagger:ParameterInfo {
in: "query",
name: "limit",
description: "The limit of the records.",
required: false,
parameterType: "integer"
}, @swagger:ParameterInfo {
in: "query",
name: "isEmpty",
description: "Allow Empty values.",
required: false,
parameterType: "boolean"
}]
}
    @swagger:ResourceInfo {
    description: "Returns all pets from the system that the user has access to"
}
    @swagger:Responses {
    value: [ @swagger:Response {
code: "200",
description: "A list of pets."
}, @swagger:Response {
code: "404",
description: "No pets found."
}, @swagger:Response {
code: "default",
description: "unexpected error."
}]
}
    resource Resource1 (message m, @http:PathParam{value:"id"} string id, @http:QueryParam{value:"tag"} string tag, @http:QueryParam{value:"limit"} int limit, @http:QueryParam{value:"isEmpty"} boolean isEmpty) {
        reply m;
    }
}
