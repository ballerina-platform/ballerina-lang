import ballerina/http;
import ballerina/log;

type Student record {
    string Name;
    int Grade;
    map<any> Marks;
};

@http:ServiceConfig
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
        json details = orderDetails.Details;
        http:Response res = new;
        // Performs data validation for orderDetails.
        if (!isValid(orderDetails.Details.ID.toString().matches("\\d+"))) {
            res.statusCode = 400;
            res.setPayload("Order Details ID containts invalid data");
        } else if (!isValid(orderDetails.Details.Name.toString().
                            matches("[a-zA-Z]+"))) {
            res.statusCode = 400;
            res.setPayload("Order Details Name containts invalid data");
        } else {
            // Since there is no validation error, mark the details as trusted
            // data and set it to the response.
            res.setPayload(untaint details);
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
        // Performs data validation for input data.
        if (!isValid(city.getTextValue().matches("\\d+"))) {
            res.statusCode = 400;
            res.setPayload("City containts invalid data");
        } else {
            // Since there is no validation error, mark the city as trusted
            // data and set it to the response.
            res.setPayload(untaint city);
        }
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
        // Performs data validation for input data.
        if (!isValid(student.Name.matches("[a-zA-Z]+"))) {
            res.statusCode = 400;
            res.setPayload("Student name contains invalid data");
        } else {
            // Since there is no validation error, mark the inputs as trusted
            // data and set them to the response.
            res.setPayload({ Name: untaint name, Grade: untaint grade });
        }
        var result = caller->respond(res);
        if (result is error) {
           log:printError(result.reason(), err = result);
        }
    }
}

function isValid(boolean|error result) returns boolean {
    if (result is boolean) {
        return result;
    } if (result is error) {
        log:printError(result.reason(), err = result);
        return false;
    }
    return false;
}
