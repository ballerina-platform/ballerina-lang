import ballerina.net.http;

struct Student {
    string Name;
    int Grade;
    map Marks;
}

service<http> hello {

    @Description {value:"Annotation body represents the JSON payload of the inbound request."}
    @http:resourceConfig {
        methods:["POST"],
        body:"order"
    }
    resource bindJson (http:Connection conn, http:InRequest req, json order) {
        //Access JSON field values
        json details = order.Details;
        println(details);

        http:OutResponse res = {};
        res.setJsonPayload(details);
        _ = conn.respond(res);
    }

    @Description {value:"Bind xml payload of the inbound request to variable store."}
    @http:resourceConfig {
        methods:["POST"],
        body:"store",
        consumes:["application/xml"]
    }
    resource bindXML (http:Connection conn, http:InRequest req, xml store) {
        //Access XML content.
        xml city = store.selectChildren("city");
        println(city);

        http:OutResponse res = {};
        res.setXmlPayload(city);
        _ = conn.respond(res);
    }

    @Description {value:"Bind JSON payload into a custom struct. Payload content should match with struct."}
    @http:resourceConfig {
        methods:["POST"],
        body:"student",
        consumes:["application/json"]
    }
    resource bindStruct (http:Connection conn, http:InRequest req, Student student) {
        //Access Student struct fields
        string name = student.Name;
        println(name);

        int grade = student.Grade;
        println(grade);

        map marks = student.Marks;
        println(marks);

        http:OutResponse res = {};
        res.setJsonPayload({Name:name, Grade:grade});
        _ = conn.respond(res);
    }
}
