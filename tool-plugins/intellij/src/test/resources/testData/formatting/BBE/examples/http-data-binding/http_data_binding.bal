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
        // Perform data validation for orderDetails.
        if (!check orderDetails.Details.ID.toString().matches("\\d+")) {
            res.statusCode = 400;
            res.setPayload("Order Details ID containts invalid data");
        } else if (!check orderDetails.Details.Name.toString().matches("[a-zA-Z]+")) {
            res.statusCode = 400;
            res.setPayload("Order Details Name containts invalid data");
        } else {
            // Since there is no validation error, mark the details as trusted data and set to the response.
            res.setPayload(untaint details);
        }
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
        // Perform data validation for input data.
        if (!check city.getTextValue().matches("\\d+")) {
            res.statusCode = 400;
            res.setPayload("City containts invalid data");
        } else {
            // Since there is no validation error, mark the city as trusted data and set to the response.
            res.setPayload(untaint city);
        }
        caller->respond(res) but { error e => log:printError(respErr, err = e) };
    }

    //Bind the JSON payload to a custom record. The payload's content should match the record.
    @http:ResourceConfig {
        methods: ["POST"],
        body: "student",
        consumes: ["application/json"]
    }
    bindStruct(endpoint caller, http:Request req, Student student) {
        //Access the fields of the record 'Student'.
        string name = student.Name;
        int grade = student.Grade;

        http:Response res = new;
        // Perform data validation for input data.
        if (!check student.Name.matches("[a-zA-Z]+")) {
            res.statusCode = 400;
            res.setPayload("Student name contains invalid data");
        } else {
            // Since there is no validation error, mark the inputs as trusted data and set to the response.
            res.setPayload({ Name: untaint name, Grade: untaint grade });
        }
        caller->respond(res) but { error e => log:printError(respErr, err = e) };
    }
}
