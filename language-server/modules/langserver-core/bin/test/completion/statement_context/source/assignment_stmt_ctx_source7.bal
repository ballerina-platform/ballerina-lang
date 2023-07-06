
function testNewFunction() {
    TestObject2 obj2;
    obj2 = 
    int testVar = 12;

    string str = "test";
    str = 
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

function getInt() returns int {
    return 0;
}

function getString() returns string {
    return "sample";
}

function getObj2() returns TestObject2 {
    return new ();
}
