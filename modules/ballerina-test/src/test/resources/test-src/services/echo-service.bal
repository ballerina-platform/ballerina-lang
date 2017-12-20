import ballerina.net.http;

const string constPath = getConstPath();

@http:configuration {basePath:"/echo"}
service<http> echo {

    string serviceLevelStr;

    string serviceLevelStringVar = "sample value";

    @http:resourceConfig {
        methods:["GET"],
        path:"/message"
    }
    resource echo (http:Request req, http:Response res) {
        _ = res.send();
    }
    
    @http:resourceConfig {
        methods:["GET"],
        path:"/message_worker"
    }
    resource echo_worker (http:Request req, http:Response res) {
        worker w1 {
            _ = res.send();
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
    resource setString (http:Request req, http:Response res) {
        serviceLevelStr = req.getStringPayload();
        //res.setStringPayload(res, serviceLevelStr);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getString"
    }
    resource getString (http:Request req, http:Response res) {
        res.setStringPayload(serviceLevelStr);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource removeHeaders (http:Request req, http:Response res) {
        req.removeAllHeaders();
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getServiceLevelString"
    }
    resource getServiceLevelString (http:Request req, http:Response res) {
        res.setStringPayload(serviceLevelStringVar);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:constPath
    }
    resource constValueAsAttributeValue (http:Request req, http:Response res) {
        res.setStringPayload("constant path test");
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/testEmptyResourceBody"
    }
    resource testEmptyResourceBody (http:Request req, http:Response res) {
    }

    @http:resourceConfig {
        methods:["POST"]
    }
    resource getFormParams (http:Request req, http:Response res) {
        map params = req.getFormParams();
        string name;
        name,_ = (string)params.firstName;
        string team;
        team,_ = (string)params.team;
        json responseJson = {"Name":name , "Team":team};
        res.setJsonPayload(responseJson);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["PATCH"],
        path:"/modify"
    }
    resource modify11 (http:Request req, http:Response res) {
        res.setStatusCode(204);
        _ = res.send();
    }
}

function getConstPath() (string) {
    return "/constantPath";
}
