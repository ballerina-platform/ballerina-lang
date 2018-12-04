// test with the return type having package alias

function testAnonFunction() {
    function (string, http:Response, TestAnonRecord, TestAnonObject) returns (http:Response) anonFunction = 
}

type TestAnonRecord record {
    int field1;
    string field2;
};

type TestAnonObject object {
    string field1;
    string field2;
};
