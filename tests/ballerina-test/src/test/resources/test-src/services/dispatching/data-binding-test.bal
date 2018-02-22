import ballerina.net.http;

struct Person {
    string name;
    int age;
}

@http:configuration {basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
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
        methods:["GET","POST"],
        body:"person"
    }
    resource body3 (http:Connection conn, http:InRequest req, json person) {
        json name = person.name;
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
    resource body5 (http:Connection conn, http:InRequest req, blob person) {
        string name = person.toString("UTF-8");
        http:OutResponse res = {};
        res.setJsonPayload({Key:name});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body6 (http:Connection conn, http:InRequest req, Person person) {
        string name = person.name;
        int age = person.age;
        http:OutResponse res = {};
        res.setJsonPayload({Key:name , Age:age});
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body7 (http:Connection conn, http:InRequest req, http:HttpConnectorError person) {
        _ = conn.respond({});
    }
}
