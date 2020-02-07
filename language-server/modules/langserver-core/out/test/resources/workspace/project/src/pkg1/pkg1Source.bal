import ballerina/io;

type testObject object {
    int val1 = 12;
    public string val2 = "hello world";
    function printHelloFuncion() {
        string printValue = "Ballerina";
    }
    //function functionWithNoSignature();
};

type testRecord record {
    int recordInt = 12;
    string recordString = "Hello Record";
    boolean recordBoolean = false;
};
