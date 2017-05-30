function backtickXMLTest (string name)(xml) {
    xml msg;
    msg = `<name>John</name>`;
    return msg;
}

function testJSONInit (string name)(json) {
    json msg;
    msg = {"name":"John"};
    return msg;
}

function backtickVariableAccessXML(string variable) (xml) {
    xml backTickMessage;
    backTickMessage = `<name>${variable}</name>`;
    return backTickMessage;
}


function testStringVariableAccessInJSONInit(string variable) (json) {
    json backTickMessage;
    backTickMessage = {"name":variable};
    return backTickMessage;
}

function testIntegerVariableAccessInJSONInit(int variable) (json) {
    json backTickMessage;
    backTickMessage = {"age":variable};
    return backTickMessage;
}

function testEnrichFullJSON(int variable) (json) {
    json msg;
    json backTickMessage;
    msg = {"name":"John"};
    backTickMessage = msg;
    return backTickMessage;
}

function testMultipleVariablesInJSONInit(string fname, string lname) (json) {
    json msg;
    json backTickMessage;
    msg = {"name":{"first_name":fname, "last_name":lname}};
    backTickMessage = msg;
    return backTickMessage;
}

function testArrayVariableAccessInJSONInit()(json) {
    json msg;
    string[] stringArray;
    int[] intArray;

    stringArray = ["value0", "value1", "value2"];
    intArray = [0, 1, 2];
    msg = {"strIndex0":stringArray[0],"intIndex2":intArray[2],"strIndex2":stringArray[2]};
    return msg;
}

function backtickXMLArrayVariableAccess()(xml) {
    xml msg;
    string[] stringArray;
    int[] intArray;

    stringArray = ["value0", "value1", "value2"];
    intArray = [0, 1, 2];
    msg = `<root><stringIndex1>${stringArray[1]}</stringIndex1><intIndex1>${intArray[1]}</intIndex1></root>`;
    return msg;
}

function testMapVariableAccessInJSONInit()(json) {
    json msg;
    map myMap;

    myMap = {"stirngVal" : "value0" , "intVal" : 1};
    msg = {"val1":(string)myMap["stirngVal"],"val2": (int)myMap["intVal"]};
    return msg;
}

function backtickXMLMapVariableAccess()(xml) {
    xml msg;
    map myMap;

    myMap = {"stirngVal" : "value0" , "intVal" : 1};
    msg = `<root><stringIndex0>${myMap["stirngVal"]}</stringIndex0><intIndex1>${myMap["intVal"]}</intIndex1></root>`;
    return msg;
}

function testBooleanIntegerValuesAsStringsInJSONInit()(json) {
    json msg;
    string[] intStrArray;
    string[] boolStrArray;

    intStrArray = ["0", "1"];
    boolStrArray = ["true", "false"];
    msg = {"intStrIndex0":intStrArray[0],"intStrIndex1":intStrArray[1], "boolStrIndex0":boolStrArray[0], "boolStrIndex1":boolStrArray[1]};
    return msg;
}
