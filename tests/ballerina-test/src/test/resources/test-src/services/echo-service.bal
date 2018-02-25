import ballerina.net.http;

const string constPath = getConstPath();

struct Person {
    string name;
    int age;
}

@http:configuration {basePath:"/echo"}
service<http> echo {

    string serviceLevelStr;

    string serviceLevelStringVar = "sample value";

    @http:resourceConfig {
        methods:["GET"],
        path:"/message"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.respond(res);
    }
    
    @http:resourceConfig {
        methods:["GET"],
        path:"/message_worker"
    }
    resource echo_worker (http:Connection conn, http:InRequest req) {
        worker w1 {
            http:OutResponse res = {};
            _ = conn.respond(res);
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
    resource setString (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        serviceLevelStr = req.getStringPayload();
        //res.setStringPayload(res, serviceLevelStr);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getString"
    }
    resource getString (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload(serviceLevelStr);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource removeHeaders (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setHeader("header1", "wso2");
        res.setHeader("header2", "ballerina");
        res.setHeader("header3", "hello");
        res.removeAllHeaders();
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/getServiceLevelString"
    }
    resource getServiceLevelString (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload(serviceLevelStringVar);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:constPath
    }
    resource connstValueAsAttributeValue (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("constant path test");
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/testEmptyResourceBody"
    }
    resource testEmptyResourceBody (http:Connection conn, http:InRequest req) {
    }

    @http:resourceConfig {
        methods:["POST"]
    }
    resource getFormParams (http:Connection conn, http:InRequest req) {
        map params = req.getFormParams();
        string name;
        name,_ = (string)params.firstName;
        string team;
        team,_ = (string)params.team;
        json responseJson = {"Name":name , "Team":team};

        http:OutResponse res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["PATCH"],
        path:"/modify"
    }
    resource modify11 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.statusCode = 204;
        _ = conn.respond(res);
    }
}

function getConstPath() (string) {
    return "/constantPath";
}
