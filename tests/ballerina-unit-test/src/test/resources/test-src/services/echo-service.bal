import ballerina/mime;
import ballerina/http;
import ballerina/io;

@final string constPath = getConstPath();

type Person record {
    string name;
    int age;
};

endpoint http:NonListener echoEP {
    port:9090
};

@http:ServiceConfig {basePath:"/echo"}
service<http:Service> echo bind echoEP {

    string serviceLevelStr = "";

    string serviceLevelStringVar = "sample value";

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message"
    }
    echo (endpoint conn, http:Request req) {
        http:Response res = new;
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/message_worker"
    }
    echo_worker (endpoint conn, http:Request req) {
        worker w1 {
            http:Response res = new;
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
        http:Response res = new;
        string payloadData = "";
        var payload = req.getTextPayload();
        if (payload is error) {
            done;
        } else if (payload is string) {
            payloadData = payload;
        }
        serviceLevelStr = untaint payloadData;
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/getString"
    }
    getString (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload(serviceLevelStr);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"]
    }
    removeHeaders (endpoint conn, http:Request req) {
        http:Response res = new;
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
        http:Response res = new;
        res.setTextPayload(serviceLevelStringVar);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:constPath
    }
    connstValueAsAttributeValue (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("constant path test");
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
        http:Response res = new;
        if (params is map<string>) {
            string name = "";
            string team = "";
            if (params.hasKey("firstName")) {
                name = params.firstName;
            }
            if (params.hasKey("team")) {
                team = params.team;
            }
            json responseJson = {"Name":name , "Team":team};
            res.setJsonPayload(untaint responseJson);
        } else if (params is error) {
            string errMsg = <string> params.detail().message;
            res.setTextPayload(errMsg);
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["PATCH"],
        path:"/modify"
    }
    modify11 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.statusCode = 204;
        _ = conn -> respond(res);
    }
}

function getConstPath() returns (string) {
    return "/constantPath";
}
