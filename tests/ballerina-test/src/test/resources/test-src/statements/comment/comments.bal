// Year 2017
import ballerina/io;


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


@Description {value:"/FooService"} // http config annotation
service<DummyService> FooService {

    @Description{ value:"POST" // http method post
                          // http resource path 

                        }
        fooResource (string s) {
            io:println(s);
        }
}

//transformer <Person p,string s> {
  // send the name of the person
//  s = p.name;
//}

// end of file

struct DummyEndpoint {}

function <DummyEndpoint s> init (struct {} conf)  {
}

struct DummyService {}

function <DummyService s> getEndpoint() returns (DummyEndpoint) {
    DummyEndpoint myDummyEndpoint = {};
    return myDummyEndpoint ;
}
