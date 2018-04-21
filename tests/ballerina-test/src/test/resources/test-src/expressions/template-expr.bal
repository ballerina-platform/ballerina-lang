function testJSONInit (string name) returns (json) {
    json msg;
    msg = {"name":"John"};
    return msg;
}

function testStringVariableAccessInJSONInit (string variable) returns (json) {
    json backTickMessage;
    backTickMessage = {"name":variable};
    return backTickMessage;
}

function testIntegerVariableAccessInJSONInit (int variable) returns (json) {
    json backTickMessage;
    backTickMessage = {"age":variable};
    return backTickMessage;
}

function testEnrichFullJSON (int variable) returns (json) {
    json msg;
    json backTickMessage;
    msg = {"name":"John"};
    backTickMessage = msg;
    return backTickMessage;
}

function testMultipleVariablesInJSONInit (string fname, string lname) returns (json) {
    json msg;
    json backTickMessage;
    msg = {"name":{"first_name":fname, "last_name":lname}};
    backTickMessage = msg;
    return backTickMessage;
}

function testArrayVariableAccessInJSONInit () returns (json) {
    json msg;
    string[] stringArray;
    int[] intArray;

    stringArray = ["value0", "value1", "value2"];
    intArray = [0, 1, 2];
    msg = {"strIndex0":stringArray[0], "intIndex2":intArray[2], "strIndex2":stringArray[2]};
    return msg;
}

function testMapVariableAccessInJSONInit () returns (json) {
    json msg;
    map myMap;

    myMap = {"stirngVal":"value0", "intVal":1};
    //with new cast change, have to do the casting outside if it is a unsafe cast, hence moved the cast expression
    //outside the json init
    string val2;
    int intVal;
    val2 = <string> myMap["stirngVal"];
    intVal = check <int> myMap["intVal"];
    msg = {"val1":val2, "val2":intVal};
    return msg;
}

function testBooleanIntegerValuesAsStringsInJSONInit () returns (json) {
    json msg;
    string[] intStrArray;
    string[] boolStrArray;

    intStrArray = ["0", "1"];
    boolStrArray = ["true", "false"];
    msg = {"intStrIndex0":intStrArray[0], "intStrIndex1":intStrArray[1], "boolStrIndex0":boolStrArray[0], "boolStrIndex1":boolStrArray[1]};
    return msg;
}
