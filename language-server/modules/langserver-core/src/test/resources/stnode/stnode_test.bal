import ballerina/httpx;

const CONST_VALUE = "This is a constant value.";

public function main() {
    string stringValue = "This is inside function.";
}

map<string> mapValues = {
    "stringValue": "This is inside map."
};

record {|
    string stringVal;
|} value = {stringVal: "This is inside record."};

service /hello on new httpx:Listener(9093) {

    resource function get satyHello() returns string {
        return "This is inside resource." ;
    }
}
