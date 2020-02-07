import ballerina/http;

public function testFunction(string args) {
    TestObject testObject; 
    testObject = new 
}

type TestObject object {
	int field1;
	int field2;
	public function __init(int field1, int field2) {
	    self.field1 = field1;
	    self.field2 = field2;
    }
};