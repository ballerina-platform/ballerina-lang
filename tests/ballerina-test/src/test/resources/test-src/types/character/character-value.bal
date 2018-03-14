char globalChar;

function testCharValue() (char){
    char a;
    a = 'D';
    return a;
}

function testCharParam(char b) (char) {
    char a;
    a = b;
    return a;
}

function testGlobalChar(char b) (char){
    globalChar = b;
    return globalChar;
}

function testIntToCharCast(int b) (char){
    char a = b;
    return a;
}

function testCharToIntCast(char a) (int) {
    int b = a;
    return b;
}

function testCharToFloatCast(char a) (float) {
    float b = a;
    return b;
}

function testCharToAnyCast(char i) (any, char){
    char a = i;
    type typeOfa = (typeof a);
    return typeOfa, a;
}

function testCharArrayToAny(char[] caIn) (any, any) {
    any caOut = caIn;
    any typeOfca = (typeof caIn);
    return typeOfca, caOut;
}

function testIntToCharExplicitCast(int b) (char) {
    char a = (char)b;
    return a;
}

function testIntToCharConversion(int b) (char){
    var c = <char>b;
    return c;
}

function testCharToIntConversion(char b) (int){
    char c = <int>b;
    return c;
}

function testFloatToCharConversion(float f) (char) {
    var c = <char>f;
    return c;
}

function testCharToFloatConversion(char f) (float) {
    var c = <float>f;
    return c;
}


function testCharArray() (char[]){
    char[] ca = ['w', 'T', '4'];
    return ca;
}

function testCharArrayAssignment(char[] cArrayIn) (char[]) {
    char[] cArray;
    cArray = cArrayIn;
    return cArray;
}

function testCharArrayZeroLength() (int) {
    char[] a = [];
    return (lengthof a);
}

function testCharArrayLength() (int) {
    char[] a = ['d','v','f','k'];
    return (lengthof a);
}

function testCharArrayIncreaseSize() (int){
    char[] a = ['d','v','f','k'];
    a[9] = 'e';
    return (lengthof a);
}

function testCharArrayOfArray() (int, int) {
    char[][] aa = [['w', 'd', 'f', 'r'], ['h', 't', 'y'], ['u', 'j', 'l']];
    int a = (lengthof aa);
    int b = (lengthof aa[0]);
    return a, b;
}

function testCharBinaryOperation(char a, char b, char c) (boolean, boolean) {
    boolean b1 = (a == b);
    boolean b2 = (a == c);
    return b1, b2;
}

function testCharBinaryNotEqualOperation(char a, char b, char c) (boolean, boolean) {
    boolean b1 = (a != b);
    boolean b2 = (a != c);
    return b1, b2;
}

function testCharMapValues() (map){
    map m = {};
    map addrMap = {code:'S', allowed:'Y', name: "Some Name"};
    var code, _ = (char)addrMap["code"];
    addrMap.continue = 'N';
    addrMap.remove("allowed");
    return addrMap;
}


struct Person {
    string name;
    int age = -1;
    Person parent;
    char gender;
}

function testCharStructFields () (string, char, string, char) {
    Person p1 = {};

    Person p2 = {name:"Jack", age:20, parent:p1, gender:'M'};

    p1.name = "Anne";
    p1.age = 35;
    p1.gender = 'F';

    return p1.name, p1.gender, p2.name, p2.gender;
}


function testWorkerWithCharVariable() {
   worker w1 {
     int a = 10;
     char b = 'E';
     a, b -> w2;
     b <- w2;
   }
   worker w2 {
     int a = 0;
     int b = 15;
     char c = 'G';
     a, c <- w1;
     c -> w1;
   }
}

function testStringToCharArray(string b) (char[]) {
    return b.toCharArray();
}
