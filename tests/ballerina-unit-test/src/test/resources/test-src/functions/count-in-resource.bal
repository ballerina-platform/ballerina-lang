import ballerina/http;
import ballerina/io;

endpoint http:NonListener testEP {
    port:9090
};

@http:ServiceConfig {
    basePath:"/test"
}
service<http:Service> TestService bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/resource"
    }
    testResource (endpoint caller, http:Request req) {
        json[] jsonArray = [];
        string[] strArray = ["foo", "bar"];
        foreach s in strArray {
            jsonArray[jsonArray.count()] = s;
        }

        http:Response res = new;
        res.setJsonPayload(untaint jsonArray);
        _ = caller -> respond(res);
    }
}
