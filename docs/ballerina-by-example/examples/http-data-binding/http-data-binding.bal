import ballerina.net.http;
import ballerina.io;

struct Student {
    string Name;
    int Grade;
    map Marks;
}

endpoint<http:Service> helloEP {
    port:9090
}

@http:serviceConfig { endpoints:[helloEP] }
service<http:Service> hello {

    @Description {value:"Body annotation represents the entity body of the inbound request."}
    @http:resourceConfig {
        methods:["POST"],
        body:"orderDetails"
    }
    resource bindJson (http:ServerConnector conn, http:Request req, json orderDetails) {
        //Access JSON field values
        json details = orderDetails.Details;
        io:println(details);

        http:Response res = {};
        res.setJsonPayload(details);
        _ = conn -> respond(res);
    }

    @Description {value:"Bind xml payload of the inbound request to variable store."}
    @http:resourceConfig {
        methods:["POST"],
        body:"store",
        consumes:["application/xml"]
    }
    resource bindXML (http:ServerConnector conn, http:Request req, xml store) {
        //Access XML content.
        xml city = store.selectChildren("city");
        io:println(city);

        http:Response res = {};
        res.setXmlPayload(city);
        _ = conn -> respond(res);
    }

    @Description {value:"Bind JSON payload into a custom struct. Payload content should match with struct."}
    @http:resourceConfig {
        methods:["POST"],
        body:"student",
        consumes:["application/json"]
    }
    resource bindStruct (http:ServerConnector conn, http:Request req, Student student) {
        //Access Student struct fields
        string name = student.Name;
        io:println(name);

        int grade = student.Grade;
        io:println(grade);

        map marks = student.Marks;
        io:println(marks);

        http:Response res = {};
        res.setJsonPayload({Name:name, Grade:grade});
        _ = conn -> respond(res);
    }
}
