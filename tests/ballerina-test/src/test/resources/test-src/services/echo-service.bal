import ballerina/mime;
import ballerina/http;
import ballerina/io;

@final string constPath = getConstPath();

type Person {
    string name,
    int age,
};

endpoint http:NonListener echoEP {
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
        string payloadData;
        var payload = req.getStringPayload();
        match payload {
            http:PayloadError err => {
                done;
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
        http:Response res = new;
        res.setStringPayload(serviceLevelStr);
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
        res.setStringPayload(serviceLevelStringVar);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:constPath
    }
    connstValueAsAttributeValue (endpoint conn, http:Request req) {
        http:Response res = new;
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
        http:Response res = new;
        match params {
            map<string> p => {
                name = p.firstName;
                team = p.team;
                json responseJson = {"Name":name , "Team":team};
                res.setJsonPayload(responseJson);
            }
            http:PayloadError err => {
                res.setStringPayload(err.message);
            }
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
