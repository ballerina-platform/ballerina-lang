import ballerina/http;
import ballerina/mime;

listener http:MockListener testEP = new(9090);

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

service echo on testEP {

    @http:ResourceConfig {
        body: "person"
    }
    resource function body1(http:Caller caller, http:Request req, string person) {
        json responseJson = { "Person": person };
        _ = caller->respond(crypto:unsafeMarkUntainted(responseJson));
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/body2/{key}",
        body: "person"
    }
    resource function body2(http:Caller caller, http:Request req, string key, string person) {
        json responseJson = { Key: key, Person: person };
        _ = caller->respond(crypto:unsafeMarkUntainted(responseJson));
    }

    @http:ResourceConfig {
        methods: ["GET", "POST"],
        body: "person"
    }
    resource function body3(http:Caller caller, http:Request req, json person) {
        json name = crypto:unsafeMarkUntainted(person.name);
        json team = crypto:unsafeMarkUntainted(person.team);
        _ = caller->respond({ Key: name, Team: team });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "person"
    }
    resource function body4(http:Caller caller, http:Request req, xml person) {
        string name = crypto:unsafeMarkUntainted(person.getElementName());
        string team = crypto:unsafeMarkUntainted(person.getTextValue());
        _ = caller->respond({ Key: name, Team: team });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "person"
    }
    resource function body5(http:Caller caller, http:Request req, byte[] person) {
        string name = crypto:unsafeMarkUntainted(mime:byteArrayToString(person), "UTF-8");
        _ = caller->respond({ Key: name });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "person"
    }
    resource function body6(http:Caller caller, http:Request req, Person person) {
        string name = crypto:unsafeMarkUntainted(person.name);
        int age = crypto:unsafeMarkUntainted(person.age);
        _ = caller->respond({ Key: name, Age: age });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "person"
    }
    resource function body7(http:Caller caller, http:Request req, Stock person) {
        _ = caller->respond(());
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "persons"
    }
    resource function body8(http:Caller caller, http:Request req, Person[] persons) {
        var jsonPayload = json.convert(persons);
        if (jsonPayload is json) {
            _ = caller->respond(crypto:unsafeMarkUntainted(jsonPayload));
        } else if (jsonPayload is error) {
            _ = caller->respond(crypto:unsafeMarkUntainted(string.convert(jsonPayload.detail().message)));
        }
    }
}
