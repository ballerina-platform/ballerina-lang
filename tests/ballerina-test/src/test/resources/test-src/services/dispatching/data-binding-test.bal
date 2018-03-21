import ballerina/net.http;
import ballerina/net.http.mock;

endpoint<mock:NonListeningService> testEP {
    port:9090
}

struct Person {
    string name;
    int age;
}

@http:serviceConfig {
    endpoints:[testEP]
}
service<http:Service> echo {

    @http:resourceConfig {
        body:"person"
    }
    resource body1 (http:ServerConnector conn, http:Request req, string person) {
        json responseJson = {"Person":person};
        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/body2/{key}",
        body:"person"
    }
    resource body2 (http:ServerConnector conn, http:Request req, string key, string person) {
        json responseJson = {Key:key , Person:person};
        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET","POST"],
        body:"person"
    }
    resource body3 (http:ServerConnector conn, http:Request req, json person) {
        json name = person.name;
        json team = person.team;
        http:Response res = {};
        res.setJsonPayload({Key:name , Team:team});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body4 (http:ServerConnector conn, http:Request req, xml person) {
        string name = person.getElementName();
        string team = person.getTextValue();
        http:Response res = {};
        res.setJsonPayload({Key:name , Team:team});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body5 (http:ServerConnector conn, http:Request req, blob person) {
        string name = person.toString("UTF-8");
        http:Response res = {};
        res.setJsonPayload({Key:name});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body6 (http:ServerConnector conn, http:Request req, Person person) {
        string name = person.name;
        int age = person.age;
        http:Response res = {};
        res.setJsonPayload({Key:name , Age:age});
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource body7 (http:ServerConnector conn, http:Request req, http:HttpConnectorError person) {
        _ = conn -> respond({});
    }
}
