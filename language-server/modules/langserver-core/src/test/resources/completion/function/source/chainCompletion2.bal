  
function testFunction1() {
    TestRecord testRec = {
        recField1: 0,
        recField2: ""
    };

    TestObject testObj = new("f1", 12, testRec);
    testObj.rec.
    string testString = "Hello Ballerina!";
}

type TestObject object {
    string field1;
    int field2;
    TestRecord rec;

    public function __init(string f1, int f2, TestRecord recF) {
         self.field1 = f1;
         self.field2 = f2;
         self.rec = recF;
    }
};

public type TestRecord record {
    int recField1;
    string recField2;
};
