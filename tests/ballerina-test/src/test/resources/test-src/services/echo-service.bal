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

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body1 (http:Connection conn, http:InRequest req, string person) {
        json responseJson = {"Person":person};
        http:OutResponse res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/body2/{key}",
        body:"person"
    }
    resource body2 (http:Connection conn, http:InRequest req, string key, string person) {
        json responseJson = {Key:key , Person:person};
        http:OutResponse res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body3 (http:Connection conn, http:InRequest req, json person) {
        json name = person.get;
        json team = person.team;
        http:OutResponse res = {};
        res.setJsonPayload({Key:name , Team:team});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body4 (http:Connection conn, http:InRequest req, xml person) {
        string name = person.getElementName();
        string team = person.getTextValue();
        http:OutResponse res = {};
        res.setJsonPayload({Key:name , Team:team});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body5 (http:Connection conn, http:InRequest req, Person person) {
        string name = person.name;
        int age = person.age;
        http:OutResponse res = {};
        res.setJsonPayload({Key:name , Age:age});
        _ = conn.respond(res);
    }
}

function getConstPath() (string) {
    return "/constantPath";
}
