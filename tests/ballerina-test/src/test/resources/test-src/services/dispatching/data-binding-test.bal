import ballerina/http;

endpoint http:NonListeningServiceEndpoint testEP {
    port:9090
};

type Person {
    string name,
    int age,
};

service<http:Service> echo bind testEP {

    @http:ResourceConfig {
        body:"person"
    }
     body1 (endpoint client, http:Request req, string person) {
        json responseJson = {"Person":person};
        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/body2/{key}",
        body:"person"
    }
     body2 (endpoint client, http:Request req, string key, string person) {
        json responseJson = {Key:key , Person:person};
        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET","POST"],
        body:"person"
    }
     body3 (endpoint client, http:Request req, json person) {
        json name = person.name;
        json team = person.team;
        http:Response res = new;
        res.setJsonPayload({Key:name , Team:team});
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
     body4 (endpoint client, http:Request req, xml person) {
        string name = person.getElementName();
        string team = person.getTextValue();
        http:Response res = new;
        res.setJsonPayload({Key:name , Team:team});
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
     body5 (endpoint client, http:Request req, blob person) {
        string name = person.toString("UTF-8");
        http:Response res = new;
        res.setJsonPayload({Key:name});
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
     body6 (endpoint client, http:Request req, Person person) {
        string name = person.name;
        int age = person.age;
        http:Response res = new;
        res.setJsonPayload({Key:name , Age:age});
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
     body7 (endpoint client, http:Request req, http:HttpConnectorError person) {
        _ = client -> respond(new);
    }
}
