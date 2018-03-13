function testMapElementIncrement()(int){
    map namesMap = {fname:1};
    namesMap["fname"]++;
    int x;
    x, _ = (int)namesMap["fname"];
    return x;
}

function testMapElementDecrement()(int){
    map namesMap = {fname:1};
    namesMap["fname"]--;
    int x;
    x, _ = (int)namesMap["fname"];
    return x;
}

function testInvalidExpressionIncrement() (int) {
    getInt()++;
    return getInt();
}

function testInvalidExpressionDecrement() (int) {
    getInt()--;
    return getInt();
}

function getInt()(int){
    return 3;
}

function testStringVarRefIncrement()(string){
    string x = "compound";
    x++;
    return x;
}

function testStringVarRefDecrement()(string){
    string x = "compound";
    x--;
    return x;
}

function testMultiReturnWithCompound()(int){
    int x = 4;
    x += <int>"NotAInteger";
    return x;
}

function testInvalidRefExpressionWithCompound()(int){
    int x = 5;
    getInt() += x;
    return x;
}

function testInvalidTypeJsonStringCompound()(json){
    json j = {"test":"test"};
    j += "sdasdasd";
    return j;
}

function testInvalidTypeIntStringCompound()(int){
    int x = 5;
    x += "sdasd";
    return x;
}

function testIntFloatDivision()(int){
    int x = 5;
    float d = 2.5;
    x /= d;
    return x;
}

function testCompoundAssignmentAdditionWithFunctionInvocation()(int){
    int x = 5;
    x += getMultiIncrement();
    return x;
}


function getMultiIncrement()(int, int) {
   return 200, 100;
}
