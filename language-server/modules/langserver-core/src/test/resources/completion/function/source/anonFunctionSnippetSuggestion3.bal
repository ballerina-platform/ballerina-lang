// test with the return type not having package alias and empty input parameters

function testAnonFunction() {
    function () returns (http:Response) anonFunction = 
}

type TestAnonRecord record {
    int field1;
    string field2;
};

type TestAnonObject object {
    string field1;
    string field2;
};
