function testStringToCharCast() (char){
    string strVal = "Hello Ballerina!";
    char charVal = (char)strVal;
    return charVal;
}

function testStringToCharConv() (char){
    string strVal = "Hello Ballerina!";
    var charVal, _ = <char>strVal;
    return charVal;
}

function testFloatToCharCast() (char) {
    float a = 23.4;
    char b = (char)a;
    return b;
}