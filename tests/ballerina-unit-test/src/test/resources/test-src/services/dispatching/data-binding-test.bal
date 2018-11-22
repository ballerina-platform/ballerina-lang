import ballerina/http;
import ballerina/mime;

endpoint http:NonListener testEP {
    port: 9090
};

type Person record {
    string name;
    int age;
    !...
};

type Stock record {
    int id;
    float price;
    !...
};

service<http:Service> echo bind testEP {

    @http:ResourceConfig {
        body: "person"
    }
    body1(endpoint caller, http:Request req, string person) {
        json responseJson = { "Person": person };
        _ = caller->respond(untaint responseJson);
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/body2/{key}",
        body: "person"
    }
    body2(endpoint caller, http:Request req, string key, string person) {
        json responseJson = { Key: key, Person: person };
        _ = caller->respond(untaint responseJson);
    }

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        body: "person"
    }
    body3(endpoint caller, http:Request req, json person) {
        json name = untaint person.name;
        json team = untaint person.team;
        _ = caller->respond({ Key: name, Team: team });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "person"
    }
    body4(endpoint caller, http:Request req, xml person) {
        string name = untaint person.getElementName();
        string team = untaint person.getTextValue();
        _ = caller->respond({ Key: name, Team: team });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "person"
    }
    body5(endpoint caller, http:Request req, byte[] person) {
        string name = untaint mime:byteArrayToString(person, "UTF-8");
        _ = caller->respond({ Key: name });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "person"
    }
    body6(endpoint caller, http:Request req, Person person) {
        string name = untaint person.name;
        int age = untaint person.age;
        _ = caller->respond({ Key: name, Age: age });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "person"
    }
    body7(endpoint caller, http:Request req, Stock person) {
        _ = caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "persons"
    }
    body8(endpoint caller, http:Request req, Person[] persons) {
        var jsonPayload = <json>persons;
        if (jsonPayload is json) {
            _ = caller->respond(untaint jsonPayload);
        } else {
            panic jsonPayload;
        }
    }
}
