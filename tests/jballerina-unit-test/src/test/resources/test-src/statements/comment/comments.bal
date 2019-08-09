// Year 2017
import ballerina/io;
import ballerina/http;

function testComments () {
    // defining start name
    string startName = "foo";

    // defining end name
    string endName = "bar"; // defining end  name inline

    xml x = //initi xml
        xml `<foo>hello</foo>`;

    io:println(x);
    fooFunc("hello","world");
    
    Day day = MONDAY;
    if (day == TUESDAY) {
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

type Person record { // Person type
    // name field
    string name = "";

    // only one field
};

type Day "MONDAY" | "TUESDAY"; // enum Day

final Day MONDAY = "MONDAY"; // enumerator Monday
final Day TUESDAY = "TUESDAY"; // enumerator Tuesday


@http:ServiceConfig {basePath:"/FooService"} // http config annotation
service FooService on new http:MockListener(12909) {

@http:ResourceConfig{ methods: ["POST"] } // http method post
    resource function fooResource (http:Caller caller, http:Request req) {
        io:println(req);
    }
}

//transformer <Person p,string s> {
  // send the name of the person
//  s = p.name;
//}

// end of file
