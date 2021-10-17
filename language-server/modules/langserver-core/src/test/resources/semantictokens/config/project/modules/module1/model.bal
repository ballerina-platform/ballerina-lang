import ballerina/io;
import ballerina/http;
import ballerina/log;

public class Counter {
    private int n;

    public function init(int n = 0) {
        self.n = n;
    }

    public function get() returns int {
        return self.n;

    }

    public function inc() {
        self.n += 1;
    }
}

public type Teacher record {
    string name;
    readonly int age;
    float salary;
};

const BALLERINA = "Ballerina";
string value = "test variable";

enum CATEGORY {
    GOLD,
    SILVER,
    BRONZE
}

var helloService = service object {
    resource function get sayHello(http:Caller caller, http:Request req) {
        var respondResult = caller->respond("Hello, World!");
        if (respondResult is error) {
            log:printError("Error occurred when responding.", err = respondResult);
        }
    }
};

type AccountNotFoundError error;

function getLargestCountryInContinent(string continent) returns string? {
    match continent {
        "North America" => {
            return "USA";
        }
        "Antarctica" => {
            return ();
        }
    }
}
