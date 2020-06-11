import ballerina/io;

public function main() {
  testDuplicate();
}

public function testDuplicate() {
  io:println("module");
}

string testString = "test";

public type Company object {
    string name;
    string address;

    public function init() {
    	 self.name = "test";
    	 self.address = "test";
    }
};

public type Person record {|
    string name;
    int age;
|};
