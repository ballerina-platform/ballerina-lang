import ballerina/io;
import ballerina/http;
import project.module2 as mod2;
import ballerina/jballerina.java;
import ballerina/mime;

class Person {
    # + fname - this is first name
    string fname;
    # + lname - this is last name
    string lname;
    public Person? parent = ();
    private boolean employed;
    public string email = "default@abc.com";

    # Description
    #
    # + fname - Parameter Description
    # + lname - Parameter Description
    # + employed - Parameter Description
    function init(string fname, string lname, boolean employed) {
        self.fname = fname;
        self.lname = lname;
        self.employed = employed;
    }

    function getFullName() returns string {
        return self.fname + " " + self.lname;
    }

    function isEmployed() returns boolean => self.employed;
}

# Description
#
# + x - Field Description
public type Coord record {
    readonly int x;
    # + y - Field Description
    int y;
};

# Description
#
# + city - Field Description
# + country - Field Description
public type Address object {
    public string city;
    public string country;

    public function value() returns string;
};

type MapArray map<string>[];

public const int ONE = 1;
public int count = 0;
Person[] people = [];

public enum Color {
    RED,
    GREEN,
    BLUE
}

// ** Annotation **
annotation MyAnnotation serviceAnnotation on service, class;

# Description
#
# + name - Parameter Description
# + age - Parameter Description
# + modules - Parameter Description
public function printDetails(string name, int age = 18, string... modules) returns string {
    int index = 0;
    string moduleString = "Module(s): " + ", ".'join(...modules);
    return moduleString;
}

public function main() returns error? {
    // ** Class **
    // 1. Same module level class reference in the same file (declaration before the reference)
    Person person = new ("Alice", "Miller", true);
    io:println(person.getFullName());
    people.push(person);

    // 2. Same module level class reference, but in a separate file
    Counter counter = new (12);
    io:println(counter.get());

    // 3. Same module level class reference in the same file (declaration after the reference)
    Employee employee = new ();
    io:print(employee.getEmail());

    // 4. Different module class reference
    mod2:Teenager teenager = check new ("Bob", 15);
    io:print(teenager.getName());

    // ** Record **
    // 1. Define record
    record {int x; int y;} recordObj = {
        x: 1,
        y: 2
    };
    int a = recordObj.y;
    io:println(a);

    // 2. Same module level record reference in the same file (declaration before the reference)
    Coord c = {
        x: 1,
        y: 2
    };
    int b = c.x;
    io:println(b);

    // 3. Same module level record reference, but in a separate file
    Teacher teacher = {
        name: "Alex",
        age: 24,
        salary: 8000.0
    };
    io:print(teacher.name);

    // 4. Same module level record reference in the same file (declaration after the reference)
    Student student = {
        name: "Alice",
        age: 10,
        address: {
            city: "NY",
            country: "USA"
        }
    };
    io:print(student.age);

    // 5. Different module record reference
    table<mod2:Row> key(k) t = table [
    {k: "John", value: 17}
];
    t.add({k: "Anne", value: 18});
    io:println(t);

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

    // 3. Same module level object reference, but in a separate file
    io:print(helloService);

    // 4. Same module level object reference in the same file (declaration after the reference)
    typedesc<any> td = typeof ser;

    // 5. Different module object reference
    io:print(mod2:Bar);

    // ** Type **
    // 1. Same module level type reference in the same file (declaration before the reference)
    MapArray mapArr = [
        {"x": "foo"},
        {"y": "bar"}
    ];
    io:println(mapArr[0]);

    // 2. Same module level type reference, but in a separate file
    AccountNotFoundError accountNotFoundError = error AccountNotFoundError("ACCOUNT_NOT_FOUND", count = 500);

    // 3. Same module level type reference in the same file (declaration after the reference)
    InvalidIdError invalidIdError = error InvalidIdError("Error", id = "");

    // 4. Different module type reference
    mod2:InvalidIdError invalidError = error mod2:InvalidIdError("Error", id = "");

    // ** Function **
    // 1. Define function
    stream<string[], io:Error> csvStream = check io:fileReadCsvAsStream("/path");
    error? e = csvStream.forEach(function(string[] val) {
        io:println(val);
    });

    // 2. Same module level function reference in the same file (declaration before the reference)
    io:print(printDetails("Alice", 20, "JP"));

    // 3. Same module level function reference, but in a separate file
    string? largestCountryInContinent = getLargestCountryInContinent("SL");

    // 4. Same module level function reference in the same file (declaration after the reference)
    handle newArrayDequeResult = newArrayDeque();

    // 5. Different module function reference
    json[] content = [];
    json jsonObj = {};
    [string[], string[]] fields = mod2:getFields(<map<json>>content[0]);

    // ** Enum **
    // 1. Same module level enum reference in the same file (declaration before the reference)
    io:print(RED);
    io:print(Color);

    // 2. Same module level enum reference, but in a separate file
    io:print(GOLD);
    io:print(CATEGORY);

    // 3. Same module level enum reference in the same file (declaration after the reference)
    io:print(EN);
    io:print(Language);

    // 4. Different module enum reference
    io:print(mod2:BLUE);
    io:print(mod2:Color);

    // ** Variable **
    // 1. Define variable
    anydata x1 = [1, "string", true];
    int:Unsigned8 unsigned = 128;
    byte[] byteArray2 = base16 `aeeecdefabcd12345567888822`;

    // 2. Same module level variable reference in the same file (declaration before the reference)
    anydata x2 = x1.clone();
    boolean eq = (x1 == x2);
    io:print(ONE);
    io:print(++count);

    // 3. Same module level variable reference, but in a separate file
    io:print(BALLERINA);
    io:print(value);

    // 4. Same module level variable reference in the same file (declaration after the reference)
    io:print(TWO);
    io:print(++number);

    // 5. Different module variable reference
    io:print(mod2:RANDOM);
    io:print(mod2:val);

}

// ** Service **
http:Client clientEP = check new ("http://localhost:9091/backEndService");

# Description
service /actionService on new http:Listener(9090) {

    # Description
    #
    # + caller - Parameter Description
    # + req - Parameter Description
    resource function 'default messageUsage(http:Caller caller, http:Request req) {
        var response = clientEP->get("/greeting");
        if response is http:Response {
            return;
        }
        var bStream = io:fileReadBlocksAsStream("./files/logo.png");
        if (bStream is stream<byte[], io:Error>) {
            mime:Entity part1 = new;
            part1.setJson({"name": "Jane"});
        } else {
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(<@untainted>bStream.message());
            var result = caller->respond(res);
        }
    }

    resource function get profile(int id) returns http:Response|error {
        if (id < people.length()) {
            return error("Person with id " + id.toString() + " not found");
        }
    }
}

class Employee {
    private string name = "";
    private int age = 0;
    private string email = "default@abc.com";
    private string address = "No 20, Palm Grove";

    private function getDomainName() returns string => "abc.com";

    function setEmail(string username) {
        self.email = string `${username}@${self.getDomainName()}`;
    }

    public function getEmail() returns string => self.email;
}

public type Student record {
    string name;
    readonly int age;
    record {|
        string city;
        string country;
    |} address;
};

public const int TWO = 2;
int number = 0;

enum Language {
    EN = "English",
    TA = "Tamil",
    SI = "Sinhala"
}

service object {} ser = 
@serviceAnnotation {
    foo: "serviceAnnotation"
} 
service object {
    int i = 1234;
    remote function name() {

    }
};

type MyAnnotation record {
    string foo;
};

type InvalidIdDetail record {|
    error cause?;
    string id;
|};

public type InvalidIdError error<InvalidIdDetail>;

# Description
# + return - Return Value Description
function newArrayDeque() returns handle = @java:Constructor {
    'class: "java.util.ArrayDeque"
} external;
