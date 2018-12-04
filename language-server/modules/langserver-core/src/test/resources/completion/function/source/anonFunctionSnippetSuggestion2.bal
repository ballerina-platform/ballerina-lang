// test without the return and a leading character

function testAnonFunction() {
    function (string, http:Response, TestAnonRecord, TestAnonObject) anonFunction = f
}

type TestAnonRecord record {
    int field1;
    string field2;
};

type TestAnonObject object {
    string field1;
    string field2;
};
