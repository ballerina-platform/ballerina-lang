import ballerina/module1;

type Address record {
    string city;
    string state;
};

type Person record {
    string name;
    Address addr;
};

public function main() {
    module1:Client cl = new("http://localhost:9090");
    cl.
}