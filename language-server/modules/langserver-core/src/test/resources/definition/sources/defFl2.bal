import ballerina/io;

xmlns "http://ballerina.com/aa" as ns0;

public function xmlnsAccess() {
    xmlns "http://ballerina.com/default";
    io:println(ns0:foo);
    functionInSameFile();
    
    int testInt = functionInSameFile2();
    RecordInSameFile testRec = functionInSameFile3();
}

function functionInSameFile() {
    
}

function functionInSameFile2() returns int {
    return 12;
}

function functionInSameFile3() returns RecordInSameFile {
    RecordInSameFile rec1 = {};
    return rec1;
}

type RecordInSameFile record {
    int id = 12;
    string name = "John";
};

function testXMLAttributeWithCompoundAssignment() returns (string){
    xml x1 = xml `<root xmlns:ns3="http://sample.com/wso2/f"></root>`;
    x1@[ns0:foo1] = "bar1";
    var result = x1@[ns0:foo1];

    if (result is string) {
        result += "bar2";
        return result;
    }

    return "";
}

type Foo record {
    int x;
    string y;
};

function tar1(Foo | map<Foo> | int f) returns string {

    var testUnion = f;

    return "Value is 'Default'";
}

function functionWithParams(Foo param1, Foo param2 = {x: 12, y: "Hello"}, Foo... param3) returns Foo {
    Foo testVar1 = param1;
    Foo testVar2 = param2;
    Foo[] restVar = param3;
    
    return testVar1;
}