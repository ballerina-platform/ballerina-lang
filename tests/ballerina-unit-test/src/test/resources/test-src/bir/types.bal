import ballerina / io;

type MyString string;
type MyUnion int|boolean;
type MyUnion2 string|byte;

() nilType;
int intType = 42;
byte byteType = 43;
float floatType = 1.2;
boolean booleanType = false;
string stringType = "goodbye";
MyString stringTypeAliased = "orange";
MyUnion unionType =2;
MyUnion2 unionType2 = "dance";
string[] stringArrType = [];
int[][] towDimArrType = [];
function() returns () funcNilType = function () returns (){};
function (int) returns int funcIntType = function (int i) returns int { return i;};
function (int, boolean) returns int funcIntBoolType = function (int i, boolean b) returns int { return i; };
