import ballerina/mime;
import ballerina/http;
import ballerina/http;

@final string constPath = getConstPath();

struct Person {
    string name;
    int age;
}
endpoint http:NonListeningServiceEndpoint echoEP {
    port:9090
};

@http:ServiceConfig {basePath:"/echo"}
service<http:Service> echo bind echoEP {

    string serviceLevelStr;

    string serviceLevelStringVar = "sample value";

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message"
    }
    echo (endpoint conn, http:Request req) {
        http:Response res = {};
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message_worker"
    }
    echo_worker (endpoint conn, http:Request req) {
        worker w1 {
            http:Response res = {};
            _ = conn -> respond(res);
        }
        worker w2 {
            int x = 0;
            int a = x + 1;
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/setString"
    }
    setString (endpoint conn, http:Request req) {
        http:Response res = {};
        string payloadData;
        var payload = req.getStringPayload();
        match payload {
            http:PayloadError err => {
                return;
            }
            string s => {
                payloadData = s;
            }
        }
        serviceLevelStr = untaint payloadData;
        //res.setStringPayload(res, serviceLevelStr);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/getString"
    }
    getString (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload(serviceLevelStr);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"]
    }
    removeHeaders (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setHeader("header1", "wso2");
        res.setHeader("header2", "ballerina");
        res.setHeader("header3", "hello");
        res.removeAllHeaders();
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/getServiceLevelString"
    }
    getServiceLevelString (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload(serviceLevelStringVar);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:constPath
    }
    connstValueAsAttributeValue (endpoint conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("constant path test");
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/testEmptyResourceBody"
    }
    testEmptyResourceBody (endpoint conn, http:Request req) {
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/getFormParams"
    }
    getFormParams (endpoint conn, http:Request req) {
        var params = req.getFormParams();
        string name;
        string team;
        match params {
            map p => {
                name = <string>p.firstName;
                team = <string>p.team;
            }
            http:PayloadError err => {
                return;
            }
        }

        json responseJson = {"Name":name , "Team":team};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["PATCH"],
        path:"/modify"
    }
    modify11 (endpoint conn, http:Request req) {
        http:Response res = {};
        res.statusCode = 204;
        _ = conn -> respond(res);
    }
}

function getConstPath() returns (string) {
    return "/constantPath";
}
