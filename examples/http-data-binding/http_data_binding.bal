import ballerina/http;
import ballerina/io;

type Student {
    string Name;
    int Grade;
    map Marks;
};

endpoint http:Listener helloEP {
    port: 9090
};

@http:ServiceConfig
service<http:Service> hello bind helloEP {

    @Description {value: "The 'body' annotation represents the entity body of the inbound request."}
    @http:ResourceConfig {
        methods: ["POST"],
        body: "orderDetails"
    }
    bindJson(endpoint caller, http:Request req, json orderDetails) {
        //Access the JSON field values.
        json details = orderDetails.Details;
        io:println(details);

        http:Response res = new;
        res.setJsonPayload(details);
        _ = caller->respond(res);
    }

    @Description {value: "Bind the XML payload of the inbound request to variable store."}
    @http:ResourceConfig {
        methods: ["POST"],
        body: "store",
        consumes: ["application/xml"]
    }
    bindXML(endpoint caller, http:Request req, xml store) {
        //Access the XML content.
        xml city = store.selectDescendants("city");
        io:println(city);

        http:Response res = new;
        res.setXmlPayload(city);
        _ = caller->respond(res);
    }

    @Description {value: "Bind the JSON payload to a custom struct. The payload's content should match the struct."}
    @http:ResourceConfig {
        methods: ["POST"],
        body: "student",
        consumes: ["application/json"]
    }
    bindStruct(endpoint caller, http:Request req, Student student) {
        //Access the fields of the struct 'Student'.
        string name = student.Name;
        io:println(name);

        int grade = student.Grade;
        io:println(grade);

        map marks = student.Marks;
        io:println(marks);

        http:Response res = new;
        res.setJsonPayload({Name: name, Grade: grade});
        _ = caller->respond(res);
    }
}
