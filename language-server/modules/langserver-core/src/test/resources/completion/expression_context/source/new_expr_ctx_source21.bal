import ballerina/module1 as testMod;

function testNewFunction() {
    TestObject2 test = n
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

type TestStream1 stream<int>;

type TestStream2 TestStream1;
