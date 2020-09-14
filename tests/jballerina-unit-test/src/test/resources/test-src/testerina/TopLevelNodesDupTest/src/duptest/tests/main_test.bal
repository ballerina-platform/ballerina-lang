import ballerina/io;
import ballerina/test;

public function testDuplicate() {
  io:println("test");
}

public type Person record {|
    string name;
    int age;
|};

string testString = "test";

public class Company {
    string name;
    string address;

    public function init() {
    	 self.name = "test";
    	 self.address = "test";
    }
}

@test:Config {
    before: "beforeFunc",
    after: "afterFunc"
}
function testFunction() {
  testDuplicate();
    main();
}

function beforeFunc() {
}

function afterFunc() {
}
