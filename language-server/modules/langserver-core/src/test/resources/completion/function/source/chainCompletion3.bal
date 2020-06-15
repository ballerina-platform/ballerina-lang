function testFunction1() {
    string testString = "Hello Ballerina!";
    var result = getTestObject().rec.
}

function getTestObject() returns TestObject {
	TestRecord testRec = {
            recField1: 0,
            recField2: ""
        };

        TestObject testObj = new("f1", 12, testRec);

        return testObj;
}

type TestObject object {
    string field1;
    int field2;
    TestRecord rec;

    public function init(string f1, int f2, TestRecord recF) {
         self.field1 = f1;
         self.field2 = f2;
         self.rec = recF;
    }
};

public type TestRecord record {
    int recField1;
    string recField2;
};
