import ballerina/http;
import ballerina/io;
import ballerina/log;

type Student record {
    string Name;
    int Grade;
    map Marks;
};

@http:ServiceConfig
service<http:Service> hello bind { port: 9090 } {

    string respErr = "Failed to respond to the caller";

    //The 'body' annotation represents the entity body of the inbound request.
    @http:ResourceConfig {
        methods: ["POST"],
        body: "orderDetails"
    }
    bindJson(endpoint caller, http:Request req, json orderDetails) {
        //Access the JSON field values.
        json details = orderDetails.Details;

        http:Response res = new;
        res.setPayload(details);
        caller->respond(res) but { error e => log:printError(respErr, err = e) };
    }

    //Bind the XML payload of the inbound request to the `store` variable.
    @http:ResourceConfig {
        methods: ["POST"],
        body: "store",
        consumes: ["application/xml"]
    }
    bindXML(endpoint caller, http:Request req, xml store) {
        //Access the XML content.
        xml city = store.selectDescendants("{http://www.test.com}city");

        http:Response res = new;
        res.setPayload(city);
        caller->respond(res) but { error e => log:printError(respErr, err = e) };
    }

    //Bind the JSON payload to a custom struct. The payload's content should match the struct.
    @http:ResourceConfig {
        methods: ["POST"],
        body: "student",
        consumes: ["application/json"]
    }
    bindStruct(endpoint caller, http:Request req, Student student) {
        //Access the fields of the struct 'Student'.
        string name = student.Name;

        int grade = student.Grade;

        http:Response res = new;
        res.setPayload({ Name: name, Grade: grade });
        caller->respond(res) but { error e => log:printError(respErr, err = e) };
    }
}
