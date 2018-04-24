
function testAnonObjectAsFuncParam() returns (int) {
    return testAnonObjectFunc(10, new (14, "sameera"));
}

function testAnonObjectFunc(int i, object {public {int k = 10; string s;} new (k, s){}} anonSt) returns (int) {
    return anonSt.k + i;
}


function testAnonObjectAsLocalVar() returns (int) {
    object {public{int k = 11; string s;}} anonSt = new;

    return anonSt.k;
}


object { public {string fname; string lname; int age;} new (fname = "default fname", lname = "default lname") {}} person;

function testAnonObjectAsPkgVar() returns (string) {

    person = new (fname = "sameera", lname = "jaya");
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
        } new (line01, city, state, zipcode) {}} address;

        object {public{
        string month = "JAN";
        string day = "01";
        string year = "1970";
        }} dateOfBirth;
    }

    new (fname, lname, age, address, dateOfBirth) {}
};

function testAnonObjectAsObjectField() returns (string) {

    employee e = new ("sam", "json", 100,
                     new ("12 Gemba St APT 134", "Los Altos", "CA", "95123"),
                        new());
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


public type Person object {
    public {
        int age;
        string name;
    //}
    //private {
        int length;
        string kind;
    }

    public new (age, name, length) {

    }

    public function getName () returns string {
        return name;
    }

    public function getKind() returns string;
};

public function Person::getKind() returns string {
    return self.kind;
}

function testObjectEquivalencyBetweenAnonAndNormalObject() returns (int, string, string) {
    object { public { int age; string name;int length; string kind; }
    public new (age, name, string value) {
        kind = " hello " + value;
    }
    public function getName () returns string { return name; }

    public function getKind() returns string{ return name + kind; } } value = new (5, "passed Name", "sample value");

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

function testObjectWithSelfReference() returns (int, string) {
    object {public{int age, string name,}new () {self.age = 88;self.name = "Tyler ";}function test(int age, string name) {
        self.age = self.age + age;
        self.name = self.name + name;
    }} sample;

    sample.test(10, "Jewell");
    return (sample.age, sample. name);
}