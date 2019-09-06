import ballerina/http;
import ballerina/io;

# Struct for represent person's details
#
# + name - string value name of the person
# + id - int value id of the person 
# + age - int value age of the person
type Person record {
    string name;
    int id;
    int age;
};

# This is a simple Fruit
type Fruit object {
    string name;
    # Create a new Fruit
    public function __init(string name) {
	    self.name = name;
    }
};

# This is a Apple
type Apple object {
    # Create a new Apple
    public function __init() {
    }
};

# Test function to show current package works
#
# + s - string parameter 
# + sd - int parameter 
# + return - return an int
function test1 (string s, int sd) returns int{
    int a = 0;
    return a;
}

public function main (string... args) {
    string s = "mars";
    io:println(s);
    var df = s.indexOf("m");
    var x = test1("s",0);
    Person testPerson = {
                   id:1,
                   age: 21,
                   name:"mike"
               };
    string name = testPerson.name;
    if(testPerson.name == "mike"){

    }
    Fruit fruit = new("");
    Apple apple = new;
    Fruit fruit2 = new Fruit("");
}

service testService on new http:Listener(8080) {
       resource function testResource(http:Caller caller, http:Request request) {
              boolean hasHeader = request.hasHeader(http:EXPECT);
       }
}

function testAsyncSend() {
    http:Client remoteEndpoint = new("http://localhost:9090");
    future<http:Response|error> f = start remoteEndpoint->post("/abc", "request");
}
