function testMapElementIncrement()  returns (int){
    map namesMap = {fname:1};
    namesMap["fname"]++;
    int x;
    x = check <int>namesMap["fname"];
    return x;
}

function testMapElementDecrement() returns (int){
    map namesMap = {fname:1};
    namesMap["fname"]--;
    int x;
    x = check <int>namesMap["fname"];
    return x;
}

function testInvalidExpressionIncrement() returns  (int) {
    getInt()++;
    return getInt();
}

function testInvalidExpressionDecrement() returns  (int) {
    getInt()--;
    return getInt();
}

function getInt() returns (int){
    return 3;
}

function testStringVarRefIncrement() returns (string){
    string x = "compound";
    x++;
    return x;
}

function testStringVarRefDecrement() returns (string){
    string x = "compound";
    x--;
    return x;
}

function testMultiReturnWithCompound() returns (int){
    int x = 4;
    x += <int>"NotAInteger";
    return x;
}

function testInvalidRefExpressionWithCompound() returns (int){
    int x = 5;
    getInt() += x;
    return x;
}

function testInvalidTypeJsonStringCompound() returns (json){
    json j = {"test":"test"};
    j += "sdasdasd";
    return j;
}

function testInvalidTypeIntStringCompound() returns (int){
    int x = 5;
    x += "sdasd";
    return x;
}

function testIntFloatDivision() returns (int){
    int x = 5;
    float d = 2.5;
    x /= d;
    return x;
}

function testCompoundAssignmentAdditionWithFunctionInvocation() returns (int){
    int x = 5;
    x += getMultiIncrement();
    return x;
}


function getMultiIncrement() returns (int, int) {
   return (200, 100);
}
