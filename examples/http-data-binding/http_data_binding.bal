import ballerina/http;
import ballerina/log;

type Student record {
    string Name;
    int Grade;
    map<any> Marks;
};

@http:ServiceConfig{}
service hello on new http:Listener(9090) {

    string respErr = "Failed to respond to the caller";

    //The `body` annotation represents the entity body of the inbound request.
    @http:ResourceConfig {
        methods: ["POST"],
        body: "orderDetails"
    }
    resource function bindJson(http:Caller caller, http:Request req,
                               json orderDetails) {
        //Accesses the JSON field values.
        var details = orderDetails.Details;
        http:Response res = new;
        if (details is json) {
            res.setPayload(<@untainted> details);
        } else {
            res.statusCode = 400;
            res.setPayload("Order Details unavailable");
        }
        var result = caller->respond(res);
        if (result is error) {
           log:printError(result.reason(), err = result);
        }
    }

    //Binds the XML payload of the inbound request to the `store` variable.
    @http:ResourceConfig {
        methods: ["POST"],
        body: "store",
        consumes: ["application/xml"]
    }
    resource function bindXML(http:Caller caller, http:Request req, xml store) {
        //Accesses the XML content.
        xml city = store.selectDescendants("{http://www.test.com}city");
        http:Response res = new;
        res.setPayload(<@untainted> city);

        var result = caller->respond(res);
        if (result is error) {
           log:printError(result.reason(), err = result);
        }
    }

    //Binds the JSON payload to a custom record. The payload's content should
    //match the record.
    @http:ResourceConfig {
        methods: ["POST"],
        body: "student",
        consumes: ["application/json"]
    }
    resource function bindStruct(http:Caller caller, http:Request req,
                                 Student student) {
        //Accesses the fields of the record `Student`.
        string name = student.Name;
        int grade = student.Grade;
        http:Response res = new;
        res.setPayload({ Name: <@untainted> name, Grade: <@untainted> grade });

        var result = caller->respond(res);
        if (result is error) {
           log:printError(result.reason(), err = result);
        }
    }
}

