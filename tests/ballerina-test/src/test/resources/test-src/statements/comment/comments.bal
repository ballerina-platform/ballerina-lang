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

type Person { // Person type
    // name field
    string name,

    // only one field
};

type Day "MONDAY" | "TUESDAY"; // enum Day

@final Day MONDAY = "MONDAY"; // enumerator Monday
@final Day TUESDAY = "TUESDAY"; // enumerator Tuesday


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

type Config {
    string name,
};

type DummyEndpoint object {

    function init (Config conf)  {
    }
};


type DummyService object {

    function getEndpoint() returns (DummyEndpoint) {
        DummyEndpoint myDummyEndpoint = new;
        return myDummyEndpoint ;
    }
};
