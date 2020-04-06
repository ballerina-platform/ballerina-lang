import ballerina/http;

listener http:MockListener echoEP = new(9090);

@http:ServiceConfig {
    basePath:"/signature"
}
service echo on echoEP {
    resource function noRequestParam(http:Caller caller) {
        //
    }

    resource function noCallerParam(http:Request req, http:Response res) {
        //
    }

    resource function noRequestButCallerParam(http:Caller caller, http:Response res) {
        //
    }

    resource function someParamIntheMiddle(http:Caller conn, int key, http:Request req) {
        //
    }

    resource function someRandomParam(http:Caller conn, http:Request req, boolean key) {
        //
    }

    resource function pathParamWithoutPathConfig(http:Caller conn, http:Request req, @http:PathParam boolean key) {
        //
    }

    resource function invalidBodyParamType(http:Caller caller, http:Request req,  @http:BodyParam int person) {
        //
    }

    @http:ResourceConfig {
        path:"/{person}"
    }
    resource function mismatchedBodyParam(http:Caller caller, http:Request req, @http:BodyParam json ballerina) {
        // A compile time warning for unused path expression
    }

    resource function missedAnnotation(http:Caller caller, http:Request req, @tainted int p, @http:BodyParam json j) {
        //
    }

    @http:ResourceConfig {
        path:"/{person}"
    }
    resource function invalidPathParamtype(http:Caller caller, http:Request req, @http:PathParam json person) {
        //
    }

    resource function invalidQueryParamtype(http:Caller caller, http:Request req, @http:QueryParam json q) {
        //
    }

    resource function invalidQueryParamtypes(http:Caller caller, http:Request req, @http:QueryParam int[] q) {
        //
    }

    resource function multipleBody(http:Caller c, http:Request r, @http:BodyParam xml x, @http:BodyParam json j) {
        //
    }

    @http:ResourceConfig {
        path: "/{name}{hi}/{age}"
    }
    resource function sayHello2(http:Caller caller, http:Request req, @http:BodyParam json j,
                                @http:PathParam string name, @http:QueryParam string[] q) {
        // A compile time warning for unused path expression
    }
}
