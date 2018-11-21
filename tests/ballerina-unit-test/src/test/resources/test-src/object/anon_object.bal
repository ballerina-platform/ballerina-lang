
function testAnonObjectAsFuncParam() returns (int) {
    return testAnonObjectFunc(10, new (14, "sameera"));
}

function testAnonObjectFunc(int i, object {public int k = 10; public string s; new (k, s){}} anonSt) returns (int) {
    return anonSt.k + i;
}


function testAnonObjectAsLocalVar() returns (int) {
    object {public int k = 11; public string s;} anonSt = new;

    return anonSt.k;
}


object {
    public string fname = "";
    public string lname = "";
    public int age = 0;
    new (fname = "default fname", lname = "default lname") {}
} person = new;

function testAnonObjectAsPkgVar() returns (string) {

    person = new (fname = "sameera", lname = "jaya");
    person.lname = person.lname + "soma";
    person.age = 100;
    return person.fname + ":" + person.lname + ":" + person.age;
}

type employee object {
    public string fname = "";
    public string lname = "";
    public int age = 0;
    public object {
            public string line01 = "";
            public string line02 = "";
            public string city = "";
            public string state = "";
            public string zipcode = "";
          new (line01, city, state, zipcode) {}} address;

    public object {
        public string month = "JAN";
        public string day = "01";
        public string year = "1970";
        } dateOfBirth;

    new (fname, lname, age, address, dateOfBirth) {}
};

function testAnonObjectAsObjectField() returns (string) {

    employee e = new ("sam", "json", 100,
                     new ("12 Gemba St APT 134", "Los Altos", "CA", "95123"),
                        new());
    return e.dateOfBirth.month + ":" + e.address.line01 + ":" + e.address["state"] + ":" + e.fname;
}

object { public int age; public string name; new (age, string lname) {self.name = "a " + lname;} function getName() returns string {return self.name;}} p = new (5, "hello");

function testAnonObjectWithFunctionAsGlobalVar () returns string {
    return p.getName();
}

function testAnonObjectWithFunctionAsLocalVar () returns string {
    object { public int age; public string name; new (age, string lname) {self.name = "a " + lname;} function getName() returns string {return self.name;}} p1 = new (5, "hello");
    return p1.getName();
}


public type Person object {
    public int age = 0;
    public string name = "";
    public int length = 0;
    public string kind = "";

    public new (age, name, length) {

    }

    public function getName () returns string {
        return self.name;
    }

    public function getKind() returns string;
};

function Person.getKind() returns string {
    return self.kind;
}

function testObjectEquivalencyBetweenAnonAndNormalObject() returns (int, string, string) {
    object { public int age; public string name; public int length; public string kind;
    public new (age, name, string value) {
        self.kind = " hello " + value;
    }
    public function getName () returns string { return self.name; }

    public function getKind() returns string{ return self.name + self.kind; } } value = new (5, "passed Name", "sample value");

    Person person1 = value;

    return (person1.age, person1.name, person1.getKind());
}

function testAnonObjectWithRecordLiteral() returns (int, string) {
    object { public record {int age; string name;} details; private int length; private string kind;
    new (details, kind) {
    }
    function getName () returns string { return self.details.name; }} value = new ({age:8, name:"sanjiva"}, "passed kind");

    return (value.details.age, value.getName());
}

type Foo object {
    public record {int age; string name;} details;

    private int length = 0;
    private string kind = "";

    new (details, kind) {
    }

    function getName () returns string {
        return self.details.name;
    }
};

function testObjectWithAnonRecordLiteral() returns (int, string) {
    Foo value = new ({age:8, name:"sanjiva"}, "passed kind");

    return (value.details.age, value.getName());
}

function testObjectWithSelfReference() returns (int, string) {
    object {public int age; public string name; new () {self.age = 88;self.name = "Tyler ";}function test(int a, string n) {
        self.age = self.age + a;
        self.name = self.name + n;
    }} sample = new;

    sample.test(10, "Jewell");
    return (sample.age, sample.name);
}
