import ballerina/module1;

function testNewFunction() {
    TestObject1 obj1;
    fail e
    int testVar = 12;
}

type TestObject1 object {
	int field1;
	int field2;
	public function init(int field1, int field2) {
	    self.field1 = field1;
	    self.field2 = field2;
    }
};

type TestObject2 object {
	int field1;
	int field2;
	public function init() {
	    self.field1 = 10;
	    self.field2 = 20;
    }
};
