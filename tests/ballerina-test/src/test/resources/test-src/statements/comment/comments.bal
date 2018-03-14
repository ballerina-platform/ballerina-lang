// Year 2017
import ballerina.io;
import ballerina.net.http; // importing http package

function testComments () {
    // defining start name
    string startName = "foo";

    // defining end name
    string endName = "bar"; // defining end  name inline

    xml x = //initi xml
        xml `<{{startName}}>hello</{{startName}}>`;


    io:println(x);
    fooFunc("hello","world");
    
    Day day = Day.MONDAY;
    if (day == Day.TUESDAY) {
        io:println("day is wrong!");
    }
}

function fooFunc(string a, // foo function
    string b) {
    // printing a
    io:println(a);

    // printing b
    io:println(b);
    return;
}

struct Person { // Person type
    // name field
    string name;

    // only one field
}

enum Day { // enum Day
    // enumerator Monday
    MONDAY,
    TUESDAY
    // only two enumerators
} // end of enum


@http:serviceConfig {basePath:"/FooService"} // http config annotation
service<http:Service> FooService {

    @http:resourceConfig{ methods:["POST"], // http method post
                          // http resource path 
                          path:"/fooResource/" 
                        }
        resource fooResource (http:ServerConnector conn, http:Request req) {
            http:Response res = {};
            var xmlpayload, _ = req.getXmlPayload();
            res.setXmlPayload(xmlpayload);
            _ = conn -> respond(res);
    }
}

transformer <Person p,string s> {
  // send the name of the person
  s = p.name;
}

// end of file
