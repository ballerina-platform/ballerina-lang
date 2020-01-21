import ballerina / io;

type MyString string;
type MyUnion int|boolean;
type MyUnion2 string|byte;
type RecursiveObj object{RecursiveObj me;};
type RecursiveL2Obj object{RecursiveObj that;};
type RecursiveL3Obj object{RecursiveL2Obj other;};


type RecursiveDeepL0Obj object{RecursiveDeepL2Obj? a;};
type RecursiveDeepL1Obj object{RecursiveDeepL0Obj b;};
type RecursiveDeepL2Obj object{RecursiveDeepL1Obj c;};

type Employee record {
    int id;
    string name;
};

// expected: ()
() nilType = ();

// expected: int
int intType = 42;

// expected: byte
byte byteType = 43;

// expected: float
float floatType = 1.2;

// expected: decimal
decimal decimalType = 10.2;

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

// expected: object {... me; }
RecursiveObj myRecObj = new;

// expected: object {object {object {... me; } that; } other; }
RecursiveL3Obj myRecL2Obj = new;

// expected: object {object {object {...|() a; } b; } c; }
RecursiveDeepL2Obj deepRec = new;

// expected: error (string, map<anydata|...>)
error e = error("not good");

// expected: record {int id; string name; }
Employee teacher = {id : 2345343, name: "Marco Polo"};

// expected: table<record {int id; string name; }>
table<Employee> employeeTable = table {
        {id, name},
        [
            {1, "Employee1"},
            {2, "Employee2"}
        ]
    };

