import ballerina/module1;

function testNewFunction() {
    TestObject2 obj2 = new TestObject2(m)
    int testVar = 12;
}

class TestObject1 {
	int field1;
	int field2;
	public function init(int field1, int field2) {
	    self.field1 = field1;
	    self.field2 = field2;
    }
}

class TestObject2 {
	int field1;
	int field2;
	public function init() {
	    self.field1 = 10;
	    self.field2 = 20;
    }
}
