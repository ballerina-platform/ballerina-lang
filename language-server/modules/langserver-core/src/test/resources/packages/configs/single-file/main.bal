import ballerina/io;
import ballerina/http;

enum Color {
    # This is red color
    RED,
    GREEN,
    BLUE
}

const BALLERINA = "Ballerina";

type Coordinates record {|
    decimal latitude;
    decimal longitude;
|};

xml modLevelVariable = xml `<book>
                <name>Sherlock Holmes</name>
                <author>Sir Arthur Conan Doyle</author>
                <!--Price: $10-->
              </book>`;

configurable boolean enableRemote = true;

class Person {
    string name;
    int age;
    function init(string name) {
        self.name = name;
    }
}

public type Address object {
    public string city;
    public string country;

    public function value() returns string;
};

type SampleError error<SampleErrorData>;

public type CountryCode LK|LKA|USA;

public function main() {
    io:println("Hello!");
}

service /hey on new http:Listener(9093) {

    resource function get satyHello() returns string {
        return "Hey!";
    }
}
