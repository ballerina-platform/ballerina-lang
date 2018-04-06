
function testAnonObjectAsFuncParam() returns (int) {
    return testAnonObjectFunc(10, {k:14, s:"sameera"});
}

function testAnonObjectFunc(int i, object {public {int k = 10; string s;}} anonSt) returns (int) {
    return anonSt.k + i;
}


function testAnonObjectAsLocalVar() returns (int) {
    object {public{int k = 11; string s;}} anonSt = {};

    return anonSt.k;
}


object { public {string fname; string lname; int age;} new () {}} person;

function testAnonObjectAsPkgVar() returns (string) {

    person = {fname:"sameera", lname:"jaya"};
    person.lname = person.lname + "soma";
    person.age = 100;
    return person.fname + ":" + person.lname + ":" + person.age;
}

type employee object {
    public {
        string fname;
        string lname;
        int age;
        object {public{ string line01;
            string line02;
            string city;
            string state;
            string zipcode;
        }} address;

        object {public{
        string month = "JAN";
        string day = "01";
        string year = "1970";
        }} dateOfBirth;
    }
};

function testAnonObjectAsObjectField() returns (string) {

    employee e = {fname:"sam", lname:"json", age:100,
                     address:{line01:"12 Gemba St APT 134", city:"Los Altos", state:"CA", zipcode:"95123"},
                    dateOfBirth:{}};
    return e.dateOfBirth.month + ":" + e.address.line01 + ":" + e.address["state"] + ":" + e.fname;
}

object { public {int age, string name;} new (age, string lname) {name = "a " + lname;} function getName() returns string {return name;}} p = new (5, "hello");

function testAnonObjectWithFunctionAsGlobalVar () returns string {
    return p.getName();
}

function testAnonObjectWithFunctionAsLocalVar () returns string {
    object { public {int age, string name;} new (age, string lname) {name = "a " + lname;} function getName() returns string {return name;}} p = new (5, "hello");
    return p.getName();
}


type Person object {
    public {
        int age;
        string name;
    }
    private {
        int length;
        string kind;
    }

    new (age, name, length) {

    }

    function getName () returns string {
        return name;
    }

    function getKind() returns string;
};

function Person::getKind() returns string {
    return kind;
}

function testObjectEquivalencyBetweenAnonAndNormalObject() returns (int, string, string) {
    object { public { int age; string name;} private { int length; string kind; }
    new (age, name, string value) {
        kind = " hello " + value;
    }
    function getName () returns string { return name; }

    function getKind() returns string{ return name + kind; } } value = new (5, "passed Name", "sample value");

    Person person  = value;

    return (person.age, person.name, person.getKind());
}

function testAnonObjectWithRecordLiteral() returns (int, string) {
    object { public { {int age; string name;} details;} private { int length; string kind; }
    new (details, kind) {
    }
    function getName () returns string { return details.name; }} value = new ({age:8, name:"sanjiva"}, "passed kind");

    return (value.details.age, value.getName());
}

type Foo object {
    public {
        {int age; string name;} details;
    }
    private {
        int length;
        string kind;
    }

    new (details, kind) {
    }

    function getName () returns string {
        return details.name;
    }
};

function testObjectWithAnonRecordLiteral() returns (int, string) {
    Foo value = new ({age:8, name:"sanjiva"}, "passed kind");

    return (value.details.age, value.getName());
}
