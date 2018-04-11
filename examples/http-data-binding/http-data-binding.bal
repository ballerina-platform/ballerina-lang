import ballerina/http;
import ballerina/io;

type Student {
    string Name;
    int Grade;
    map Marks;
};

endpoint http:Listener helloEP {
    port:9090
};

@http:ServiceConfig
service<http:Service> hello bind helloEP {

    @Description {value:"Body annotation represents the entity body of the inbound request."}
    @http:ResourceConfig {
        methods:["POST"],
        body:"orderDetails"
    }
    bindJson (endpoint outboundEP, http:Request req, json orderDetails) {
        //Access JSON field values
        json details = orderDetails.Details;
        io:println(details);

        http:Response res = new;
        res.setJsonPayload(details);
        _ = outboundEP -> respond(res);
    }

    @Description {value:"Bind xml payload of the inbound request to variable store."}
    @http:ResourceConfig {
        methods:["POST"],
        body:"store",
        consumes:["application/xml"]
    }
    bindXML (endpoint outboundEP, http:Request req, xml store) {
        //Access XML content.
        xml city = store.selectDescendants("city");
        io:println(city);

        http:Response res = new;
        res.setXmlPayload(city);
        _ = outboundEP -> respond(res);
    }

    @Description {value:"Bind JSON payload into a custom struct. Payload content should match with struct."}
    @http:ResourceConfig {
        methods:["POST"],
        body:"student",
        consumes:["application/json"]
    }
    bindStruct (endpoint outboundEP, http:Request req, Student student) {
        //Access Student struct fields
        string name = student.Name;
        io:println(name);

        int grade = student.Grade;
        io:println(grade);

        map marks = student.Marks;
        io:println(marks);

        http:Response res = new;
        res.setJsonPayload({Name:name, Grade:grade});
        _ = outboundEP -> respond(res);
    }
}
