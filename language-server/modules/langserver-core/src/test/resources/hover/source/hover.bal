import ballerina/module1;

# Description for module variable with init
final isolated configurable int modVar1 = 12;

# Description for module variable with no init
int modVar2;

# Description for CONST1
const int CONST1 = 12;

# Description for CONST2
public const CONST2 = "test";

function init() {
    modVar2 = 12;
}

# Test Listener
public listener module1:Listener lst = new module1:Listener(23);

type TestType client object {
    # Test Description1 for testMethodDecl
    #
    # + a - a Parameter Description1
    # + return - Return Value Description1
    function testMethodDecl(int a) returns int; // 1

    # Test Description for testRemoteMethodDecl1
    #
    # + a - a Parameter Description1
    # + return - Return Value Description1
    remote function testRemoteMethodDecl1(int a) returns int;
};

# Description for TestClass
# 
# + field1 - field1 description
client class TestClass {
    *TestType;

    int field1 = 1;
    
    # Test Description2 for testMethodDecl
    #
    # + a - a Parameter Description2
    # + return - Return Value Description2
    function testMethodDecl(int a) returns int { // 2
        return 123;
    }

    # Test Description2 for testMethodDef
    #
    # + a - a Parameter Description2
    # + return - Return Value Description2
    function testMethodDef(int a) returns int { // 2
        return 123;
    }

    # Test Description2 for testRemoteMethodDecl1
    #
    # + a - a Parameter Description2
    # + return - Return Value Description2
    remote function testRemoteMethodDecl1(int a) returns int { // 2
        return 123;
    }

    # Test Description for testRemoteMethodDef
    #
    # + b - b Parameter Description1
    # + return - Return Value Description1
    remote function testRemoteMethodDef(int b) returns int { // 2
        return 123;
    }
}

function testFunction1() {
    var var1 = 
    client object {
               # Test Description2 for testMethodDef
               #
               # + a - a Parameter Description2
               # + return - Return Value Description2
               function testMethodDef(int a) returns int { // 2
                   return 123;
               }

               # Test Description for testRemoteMethodDef
               #
               # + b - b Parameter Description1
               # + return - Return Value Description1
               remote function testRemoteMethodDef(int b) returns int { // 2
                   return 123;
               }
           };
}

# service description
service / on lst {
    # Test Description2 for testResource1
    #
    # + param1 - a Parameter Description2
    # + return - Return Value Description2
    isolated transactional resource function testResource1 .(int param1) returns int {
        return 123;
    }
}

# Description
#
# + param1 - param1 Parameter Description
# + param2 - param2 Parameter Description
# + param3 - param3 Parameter Description
# + restparam - restparam Parameter Description
# + return - Return Value Description
function testFunction2(int param1, int param2, string param3 = "", int... restparam) returns int {
    return 123;
}

# Test Error type definition
type ErrorName error<map<anydata>>;

# Test Type definition2
type TestType2 int;

# Description for TestObjectType1
# 
# + field1 - Description for the field1
type TestObjectType1 client object {
    public int field1;
    function methodDecl1();
    remote function remoteMethodDecl();
};

# Description for TestRecordType1
#
# + field1 - field1 Parameter Description 
# + field2 - field2 Parameter Description 
# + field3 - field3 Parameter Description
type TestRecordType1 record {
    int field1;
    int field2?;
    int field3 = 123;
};

# Description for TestRecordType2
#
# + field1 - field1 Parameter Description 
# + field2 - field2 Parameter Description
# + field3 - field2 Parameter Description
type TestRecordType2 record {|
    int field1;
    int field2?;
    int field3 = 123;
    int...;
|};

# Description for TestClass1
# + field1 - field1 description
client class TestClass1 {
    *TestType;
    int field1 = 1;

    function function1(int a, string b = "", int... rest) returns int {
        return 123;
    }

    # Test Description2 for testMethodDecl
    #
    # + a - a Parameter Description2
    # + return - Return Value Description2
    function testMethodDecl(int a) returns int { // 2
        return 123;
    }

    # Test Description2 for rtestRemoteMethodDecl1
    #
    # + a - a Parameter Description2
    # + return - Return Value Description2
    remote function testRemoteMethodDecl1(int a) returns int { // 2
        return 123;
    }
}

# Description for TestEnum1
public enum TestEnum1 {
    # Description for MEMBER1
    MEMBER1,
    MEMBER2
}

# Description for the TestAnnotation1
annotation AnnotationRecord TestAnnotation1 on function;

type AnnotationRecord record {
    int field1 = 1;
};

@TestAnnotation1 {
    field1: 0
}
function testFunctionAnnotation() {
    
}

public function main() {
    TestClass c = new ();
    int var1 = c.testMethodDecl(2);
    int var2 = c->testRemoteMethodDecl1(3);
    int var3 = 1 + CONST1 + modVar1 + modVar2;
    TestEnum1 var4 = MEMBER1;
    int testFunction2Result = testFunction2(1, 2, "", 1);
    var var5 = c.testMethodDef(1);
    var var6 = c->testRemoteMethodDef(1);
    TestType2 var7 = 123;
    TestObjectType1 var8;
    TestRecordType1 var9 = {
        field1: 0,
        field2: 0,
        field3: 0
    };
    TestRecordType2 var10 = {
        field1: 0,
        field2: 0,
        field3: 0
    };
    
    module1:Client cl = new("http://localhost/test");
}

type Query object {
  # Query string
  string query;
};

public type NodeCredential record {|
    # IP Address
    string ip;
    int port;
    string username;
    constants:NodeType nodetype;
    # Optfield
    string optField ?;
|};

function testVarType() {
    int testInt;
    var testVar = testFunction3();
    if !(testVar is string) {
        return;
    }
    testInt = 10;
}

public function testFunction3() returns int|string|boolean[]?{
    return 2;
}

var testAnonFuncVar = function() returns string {
    return "hello";
};

var testAnonFuncNilVar = function() {
    return;
};

# Description for new test record for TestRecordType1
TestRecordType1 testRecordType1 = {
    field1: 1,
    field2: 2,
    field3: 3
};

int? testVar1 = ();

type MapArray map<string>[];

function testFunction4() {
     MapArray arr = [
        {"x": "foo"},
        {"y": "bar"}
    ];
    return;
}

type TestStructuredName record {
    string firstName;
    string lastName;
};
type Name TestStructuredName|string;

function testFunction5() {
    Name name1 = {
        firstName: "Rowan",
        lastName: "Atkinson"
    };
}
