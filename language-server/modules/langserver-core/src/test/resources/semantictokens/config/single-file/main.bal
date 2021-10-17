import ballerina/io;
import ballerina/http;
import ballerina/log;
import ballerina/test;

// ** Service **
service http:Service /foo on new http:Listener(9090) {
    // The `resource path` identifier associates the relative path to the service object's path. E.g., `bar`.
    resource function post bar(http:Caller caller, http:Request req) {
        var payload = req.getJsonPayload();
        http:Response res = new;
        if (payload is json) {
            res.setJsonPayload(<@untainted>payload);
        } else {
            res.statusCode = 500;
            res.setPayload(<@untainted>payload.message());
        }
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error in responding", err = result);
        }
    }
}

class File {
    string path;
    string contents;
    function init(string p, string? c) returns error? {
        self.path = p;
        self.contents = check c.ensureType(string);
    }
}

type Person record {
    string first;
    string last;
    int yearOfBirth;
};

function add(int x, int y) returns int {
    int sum = x + y;
    return sum;
}

public enum Color {
    YELLOW
}

# Description
#
# + city - Field Description
# + country - Field Description
public type Address object {
    public string city;
    public string country;

    public function value() returns string;
};

type EmployeeTable table<Employee> key(age);

public function main() {
    // ** Variable **
    // 1. Define variable
    string[] s = ["A", "B"];

    // 2. Same module level variable reference in the same file (declaration before the reference)
    io:print(s.toString());

    // 3. Same module level variable reference in the same file (declaration after the reference)
    if flag {
        io:println(1);
    } else {
        io:println(2);
    }

    // ** Enum **
    // 1. Same module level enum reference in the same file (declaration before the reference)
    io:print(YELLOW);

    // 2. Same module level enum reference in the same file (declaration after the reference)
    io:print(DOG);

    // ** Class **
    // 1. Same module level class reference in the same file (declaration before the reference)
    File file = check new File("test.txt", "Hello World");
    io:println(file.contents);

    // 2. Same module level class reference in the same file (declaration after the reference)
    MyClass x = new MyClass(1234);
    x.func();
    int n = x.n;
    io:println(n);

    // ** Record **
    // 1. Define record
    record {int x; int y;} recordObj = {
        x: 1,
        y: 2
    };
    int a = recordObj.y;
    io:println(a);

    // 2. Same module level record reference in the same file (declaration before the reference)
    Person[] persons = [
        {first: "Melina", last: "Kodel", yearOfBirth: 1994},
        {first: "Tom", last: "Riddle", yearOfBirth: 1926}
    ];
    var names1 = from var {first: f, last: l} in persons
        select {first: f, last: l};
    io:println(names1);
    var names2 = from var {first, last} in persons
        select {first, last};
    io:println(names2);

    // 3. Same module level record reference in the same file (declaration after the reference)
    final readonly & UniformTypeOps[] ops;

    // ** Object **
    // 1. Define object
    object {
        *object:Iterable;
        public function iterator() returns 
            object {
            public function next() returns record {|int value;|}?;
        };
    } iterableObj = 25 ..< 28;

    // 2. Same module level object reference in the same file (declaration before the reference)
    Address address = object {
        public string city;
        public string country;

        public function init() {
            self.city = "London";
            self.country = "UK";
        }

        public function value() returns string {
            return self.city + ", " + self.country;
        }

        function getCity() returns string => string `Default: ${self.city}`;
    };

    // ** Type **
    // 1. Same module level type reference in the same file (declaration before the reference)
    EmployeeTable employeeTab = table [
            {age: 1, name: "John", salary: 300.50, married: false},
            {age: 2, name: "Bella", salary: 500.50, married: false}
        ];

    // 2. Same module level type reference in the same file (declaration after the reference)
    CountryCode code = LK;

    // ** Function **
    // 1. Same module level function reference in the same file (declaration before the reference)
    io:print(add(10, 20));

    // 2. Same module level function reference in the same file (declaration after the reference)
    printLines(s);

    // 3. Different module function reference
    string st = "abc".substring(1, 2);
    int p = st.length();
    int m = string:length(st);

    io:println([10, 20, 30] is int[]);
}

function printLines(string[] sv) {
    foreach var s in sv {
        io:println(s);
    }
}

public type Student record {
    string name;
    int age;
    Address address;
};

int n = flag ? 1 : 2;
boolean flag = true;

class MyClass {
    int n;
    function init(int n) {
        self.n = n;
    }
    function func() {
        self.n += 1;
    }
}

type UniformTypeOps readonly & record {|
    BinOp union = binOpPanic;
|};

type BinOp function (any t1, any t2) returns string;

function binOpPanic(any t1, any t2) returns string {
    return "done";
}

public enum PET {
    DOG
}

public type Employee record {
    string name;
    readonly int age;
    boolean married;
    float salary;
};

public type CountryCode LK|US;

public const string LK = "LK";
public const string US = "USA";
