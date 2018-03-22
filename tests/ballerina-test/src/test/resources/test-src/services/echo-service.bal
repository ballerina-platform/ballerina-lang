import ballerina/net.http;
import ballerina/net.http.mock;

const string constPath = getConstPath();

struct Person {
    string name;
    int age;
}
endpoint<mock:NonListeningService> echoEP {
    port:9090
}

@http:serviceConfig {basePath:"/echo", endpoints:[echoEP]}
service<http:Service> echo {

    string serviceLevelStr;

    string serviceLevelStringVar = "sample value";

    @http:resourceConfig {
        methods:["GET"],
        path:"/message"
    }
    resource echo (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/message_worker"
    }
    resource echo_worker (http:ServerConnector conn, http:Request req) {
        worker w1 {
            http:Response res = {};
            _ = conn -> respond(res);
        }
        worker w2 {
            int x = 0;
            int a = x + 1;
        }
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/setString"
    }
    resource setString (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        string payloadData;
        payloadData, _ = req.getStringPayload();
        serviceLevelStr = untaint payloadData;
        //res.setStringPayload(res, serviceLevelStr);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getString"
    }
    resource getString (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload(serviceLevelStr);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource removeHeaders (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setHeader("header1", "wso2");
        res.setHeader("header2", "ballerina");
        res.setHeader("header3", "hello");
        res.removeAllHeaders();
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getServiceLevelString"
    }
    resource getServiceLevelString (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload(serviceLevelStringVar);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:constPath
    }
    resource connstValueAsAttributeValue (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("constant path test");
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/testEmptyResourceBody"
    }
    resource testEmptyResourceBody (http:ServerConnector conn, http:Request req) {
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/getFormParams"
    }
    resource getFormParams (http:ServerConnector conn, http:Request req) {
        var params, _ = req.getFormParams();
        string name;
        name,_ = (string)params.firstName;
        string team;
        team,_ = (string)params.team;
        json responseJson = {"Name":name , "Team":team};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["PATCH"],
        path:"/modify"
    }
    resource modify11 (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.statusCode = 204;
        _ = conn -> respond(res);
    }
}

function getConstPath() (string) {
    return "/constantPath";
}
