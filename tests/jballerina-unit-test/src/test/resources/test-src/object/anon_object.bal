
function testAnonObjectAsFuncParam() returns (int) {
    return testAnonObjectFunc(10, new (14, "sameera"));
}

function testAnonObjectFunc(int i, object {
                                        public int k = 10; 
                                        public string s = "";
                                        
                                        function init (int k, string s) {
                                            self.k = k;
                                            self.s = s;
                                        }
                                    } anonSt) returns (int) {
    return anonSt.k + i;
}


function testAnonObjectAsLocalVar() returns (int) {
    object {public int k = 11; public string s = "";} anonSt = new;

    return anonSt.k;
}


object {
    public string fname = "";
    public string lname = "";
    public int age = 0;
    function init (string fname = "default fname", string lname = "default lname") {
        self.fname = fname;
        self.lname = lname;
    }
} person = new;

function testAnonObjectAsPkgVar() returns (string) {

    person = new (fname = "sameera", lname = "jaya");
    person.lname = person.lname + "soma";
    person.age = 100;
    return person.fname + ":" + person.lname + ":" + person.age.toString();
}

class employee {
    public string fname;
    public string lname;
    public int age;
    public object {
        public string line01 = "";
        public string line02 = "";
        public string city = "";
        public string state = "";
        public string zipcode = "";
        public function init (string line01, string city, string state, string zipcode) {
                                self.line01 = line01;
                                self.city = city;
                                self.state = state;
                                self.zipcode = zipcode;
        }
    } address;

    public object {
        public string month = "JAN";
        public string day = "01";
        public string year = "1970";
    } dateOfBirth;

    function init (string fname, string lname, int age,
                        object {
                            public string line01 = "";
                            public string line02 = "";
                            public string city = "";
                            public string state= "";
                            public string zipcode = "";
                            public function init (string line01, string city, string state, string zipcode) {
                                self.line01 = line01;
                                self.city = city;
                                self.state = state;
                                self.zipcode = zipcode;
                            }
                        } address,
                        object {
                            public string month = "JAN";
                            public string day = "01";
                            public string year = "1970";
                        } dateOfBirth) {
        self.fname = fname;
        self.lname = lname;
        self.age = age;
        self.address = address;
        self.dateOfBirth = dateOfBirth;
    }
}

function testAnonObjectAsObjectField() returns (string) {

    employee e = new ("sam", "json", 100,
                     new ("12 Gemba St APT 134", "Los Altos", "CA", "95123"),
                        new());
    return e.dateOfBirth.month + ":" + e.address.line01 + ":" + e.address.state + ":" + e.fname;
}

object { public int age = 0; public string name = ""; function init (int age, string lname) {self.name = "a " + lname;} function getName() returns string {return self.name;}} p = new (5, "hello");

function testAnonObjectWithFunctionAsGlobalVar () returns string {
    return p.getName();
}

function testAnonObjectWithFunctionAsLocalVar () returns string {
    object { public int age = 0; public string name = ""; function init (int age, string lname) {self.name = "a " + lname;} function getName() returns string {return self.name;}} p1 = new (5, "hello");
    return p1.getName();
}


public class Person {
    public int age = 0;
    public string name = "";
    public int length = 0;
    public string kind = "";

    public function init (int age, string name, int length) {
        self.name = name;
        self.age = age;
        self.length = length;
    }

    public function getName () returns string {
        return self.name;
    }

    public function getKind() returns string {
        return self.kind;
    }
}

function testObjectEquivalencyBetweenAnonAndNormalObject() returns [int, string, string] {
    object { 
        public int age = 0;
        public string name = "";
        public int length = 0;
        public string kind = "";
        public function init (int age, string name, string value) {
            self.name = name;
            self.age = age;
            self.kind = " hello " + value;
        }
        public function getName () returns string { return self.name; }
    
        public function getKind() returns string{ return self.name + self.kind; }
    } value = new (5, "passed Name", "sample value");

    Person person1 = value;

    return [person1.age, person1.name, person1.getKind()];
}

function testAnonObjectWithRecordLiteral() returns [int, string] {
    object { 
        public record {| int age; string name; anydata...; |} details;
        private int length; 
        private string kind;
        
        function init (record {| int age; string name; anydata...; |} details, string kind) {
            self.details = details;
            self.kind = kind;
        }
    
        function getName () returns string { return self.details.name; }
        
    } value = new ({age:8, name:"sanjiva"}, "passed kind");

    return [value.details.age, value.getName()];
}

class Foo {
    public record {| int age; string name; anydata...; |} details;

    private int length = 0;
    private string kind = "";

    function init (record {| int age; string name; anydata...; |} details, string kind) {
        self.details = details;
        self.kind = kind;
    }

    function getName () returns string {
        return self.details.name;
    }
}

function testObjectWithAnonRecordLiteral() returns [int, string] {
    Foo value = new ({age:8, name:"sanjiva"}, "passed kind");

    return [value.details.age, value.getName()];
}

function testObjectWithSelfReference() returns [int, string] {
    object {
        public int age; 
        public string name; 
        function init () {
            self.age = 88;
            self.name = "Tyler ";
        }
        
        function test(int a, string n) {
            self.age = self.age + a;
            self.name = self.name + n;
        }
    } sample = new;

    sample.test(10, "Jewell");
    return [sample.age, sample.name];
}

function testCodeAnalyzerRunningOnAnonymousObjectsForDeprecatedFunctionAnnotation() {
    object {
        int b = Test();
    } obj = new;
}

@deprecated
function Test() returns int { return 0;}
