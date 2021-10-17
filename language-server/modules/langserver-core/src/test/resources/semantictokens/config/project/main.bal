import ballerina/io;
import ballerina/http;
import ballerina/log;
import project.module1 as mod1;
import ballerina/test;

class EvenNumberGenerator {
    int i = 0;
    public isolated function next() returns record {|int value;|}|error? {
        self.i += 2;
        return {value: self.i};
    }
}

public type Address object {
    public string city;
    public string country;

    public function value() returns string;
};

public type Person record {
    string name;
    int age;
    Address address;
};

int currentId = 1000;

type Entry map<json>;

// Same module level function reference in the same file (declaration after the reference)
final RoMap m = loadMap();

isolated function lookup(string s) returns readonly & Entry? {
    // Accesses `m` directly without locking.
    return m[s];
}

type EmployeeTable table<Employee> key(age);

public enum NodeType {
    Super = "super"
}

public function main() {

    // ** Class **
    // 1. Same module level class reference in the same file (declaration before the reference)
    EvenNumberGenerator evenGen = new ();
    stream<int, error?> evenNumberStream = new (evenGen);

    // 2. Same module level class reference, but in a separate file
    var x = new MyClass();
    MyClass y = new;

    // 3. Same module level class reference in the same file (declaration after the reference)
    MainController controller = object {
        final int id;
        // A value is set for the `final` field `codes` using an initializer
        // expression.
        // A value cannot be set for this field via the `init` method or
        // once the value is created.
        final string[] & readonly codes = ["AB", "CD"];

        function init() {
            self.id = currentId;
            currentId = currentId + 1;
        }

        function getId() returns string => string `Default: ${self.id}`;
    };

    // 4. Different module class reference
    mod1:Counter counter = new (12);
    io:println(counter.get());

    // ** Object
    // 1. Same module level object reference in the same file (declaration before the reference)
    Person john = {
        name: "John Doe",
        age: 25,
        address: object Address {
            public function init() {
                self.city = "Colombo";
                self.country = "Sri Lanka";
            }

            public function value() returns string {
                return self.city + ", " + self.country;
            }
        }
    };
    io:println("City: ", john.address.city);

    // 2. Same module level object reference, but in a separate file
    DummyObject objectIns = object {
        public string fieldOne = "";
        public string fieldTwo = "";

        public function doThatOnObject(string paramOne) returns boolean {
            return false;
        }
    };
    // 3. Same module level object reference in the same file (declaration after the reference)
    StringIterator iter;

    // 4. Different module object reference
    mod1:Address address = object {
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

    // ** Type
    // 1. Same module level type reference in the same file (declaration before the reference)
    EmployeeTable employeeTab = table [
            {age: 1, name: "John", salary: 300.50, married: false},
            {age: 2, name: "Bella", salary: 500.50, married: false}
        ];

    // 2. Same module level type reference, but in a separate file
    CountryCode code = LK;

    // 3. Same module level type reference in the same file (declaration after the reference)
    ContextString contextString = {
        content: "",
        headers: {one: ""}
    };

    // 4. Different module type reference
    mod1:InvalidIdError invalidIdError = error mod1:InvalidIdError("Error", id = "");

    // ** Function
    // 1. Define function
    function (string, string) returns string concat = 
            function(string x, string y) returns string {
        return x + y;
    };

    // 2. Same module level function reference in the same file (declaration before the reference)
    io:println(lookup("france"));

    // 3. Same module level function reference, but in a separate file
    future<int> fut = @strand {thread: "any"} start foo();

    // 4. Different module function reference
    io:print(mod1:printDetails("Alice", 20, "JP"));
    byte[] bytes = base16 `aeeecdefabcd12345567888822`;
    string str = check string:fromBytes(bytes);

    // ** Enum
    // 1. Same module level enum reference in the same file (declaration before the reference)
    io:print(NodeType);
    io:print(Super);

    // 2. Same module level enum reference, but in a separate file
    io:print(PET);
    io:print(DOG);

    // 3. Same module level enum reference in the same file (declaration after the reference)
    io:print(OCCUPATION);
    io:print(Doctor);

    // 4. Different module enum reference
    io:print(mod1:RED);
    io:print(mod1:Color);

    // ** Variable **
    // 1. Define variable
    map<string> & readonly countryCapitals = {
        "USA": "Washington, D.C.",
        "Sri Lanka": "Colombo",
        "England": "London"
    };
    foreach var [country, capital] in countryCapitals.entries() {
        io:println("Country: ", country, ", Capital: ", capital);
    }

    // 2. Same module level variable reference in the same file (declaration before the reference)
    int|error futureIns = wait fut;
    io:println(futureIns);

    // 3. Same module level variable reference, but in a separate file
    io:print('from);

    // 4. Same module level variable reference in the same file (declaration after the reference)
    io:print(GREETING);

    // 5. Different module variable reference
    io:print(mod1:ONE);
    io:print(++mod1:count);

    // ** Record **
    // 1. Define record
    record {int x; int y;} r = {
        x: 1,
        y: 2
    };
    io:print(r.x);

    // 2. Same module level record reference in the same file (declaration before the reference)
    Address adr = object {
        public string city;
        public string country;

        public function init() {
            self.city = "London";
            self.country = "UK";
        }

        public function value() returns string {
            return self.city + ", " + self.country;
        }
    };

    // 3. Same module level record reference, but in a separate file
    Name name1 = {
        firstName: "Rowan",
        lastName: "Atkinson"
    };
    io:println(name1);

    // 4. Same module level record reference in the same file (declaration after the reference)
    var evenNumber = evenNumberStream.next();
    if (evenNumber is ResultValue) {
        io:println("Retrieved even number: ", evenNumber.value);
    }

    // 5. Different module record reference
    mod1:Teacher teacher = {
        name: "Alex",
        age: 24,
        salary: 8000.0
    };
    io:print(teacher.name);
}

readonly class MainController {
    int id;
    string[] codes;

    function init(int id, readonly & string[] codes) {
        self.id = id;
        // The expected type for `codes` is `readonly & string[]`.
        self.codes = codes;
    }

    function getId() returns string {
        return string `Main: ${self.id}`;
    }
}

type ResultValue record {|
    int value;
|};

function loadMap() returns RoMap {
    readonly & Entry entry1 = {
        "munich": {latitude: "48.1351N", longitude: "11.5820E"},
        "berlin": {latitude: "52.5200N", longitude: "13.4050E"}
    };
    readonly & Entry entry2 = {
        "bordeaux": {latitude: "44.8378N", longitude: "0.5792W"},
        "paris": {latitude: "48.8566N", longitude: "2.3522E"}
    };
    RoMap roMap = {"germany": entry1, "france": entry2};
    return roMap;
}

// ** Service **
service /stockquote on new http:Listener(8889) {
    # Description
    #
    # + conn - Parameter Description
    # + req - Parameter Description
    transactional resource function post update/updateStockQuote(http:Caller conn, http:Request req) {
        json|http:ClientError updateReq = <@untainted>req.getJsonPayload();

        // Generate the response.
        http:Response res = new;
        if (updateReq is json) {
            string msg = "Update stock quote request received. " + "symbol:%s, price:%s" + updateReq.toString();
            log:printInfo(msg);
            json jsonRes = {"message": "updating stock"};
            res.statusCode = http:STATUS_OK;
            res.setJsonPayload(jsonRes);
        }

        map<any> pathMParams = req.getMatrixParams("/sample/path");
        var a = <string>pathMParams["a"];
        var b = <string>pathMParams["b"];
        string pathMatrixStr = string `a=${a}, b=${b}`;
    }
}

type RoMap readonly & map<Entry>;

const string GREETING = "Hello";

public type ContextString record {|
    string content;
    map<string|string[]> headers;
|};

public const string LK = "LK";
public const string US = "US";

object {
    public function __iterator() returns 
        object {
        public function next() returns record {|int value;|}?;
    };
} iterableObj = 25 ..< 28;

// ** Annotation **
@test:Mock {
    moduleName: "ballerina/time",
    functionName: "currentTime"
}
string[] outputs = [];
test:MockFunction mock_currentTime = new ();

type StringIterator object {
    public isolated function next() returns record {|
        int value;
    |}?;
};

public enum OCCUPATION {
    Doctor = "Dr."
}
