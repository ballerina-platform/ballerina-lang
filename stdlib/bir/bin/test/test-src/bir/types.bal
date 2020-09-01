import ballerina / io;

type MyString string;
type MyUnion int|boolean;
type MyUnion2 string|byte;

// expected: ()
() nilType = ();

// expected: int
int intType = 42;

// expected: byte
byte byteType = 43;

// expected: float
float floatType = 1.2;

// expected: boolean
boolean booleanType = false;

// expected: string
string stringType = "goodbye";

// expected: string
MyString stringTypeAliased = "orange";

// expected: int|boolean
MyUnion unionType =2;

// expected: string|byte
MyUnion2 unionType2 = "dance";

// expected: string[]
string[] stringArrType = [];

// expected: int[][]
int[][] towDimArrType = [];

// expected: function () returns ()
function () returns () funcNilType = function () returns (){};

// expected: function (int) returns int
function (int) returns int funcIntType = function (int i) returns int { return i;};

// expected: function (int, boolean) returns int
function (int, boolean) returns int funcIntBoolType = function (int i, boolean b) returns int { return i; };

// expected: object {}
object {} myObj = new;

// expected: object {int age; }
object {int age;} myPerson = new;

// expected: object {int age; function () returns string getFullName; }
object {int age;  function getFullName() returns string { return ""; }} myPerson2 = new;
