import ballerina/io;

type Person record {
    string name;
    int age;
};

configurable int[] prices = [62, 58];
configurable Person person = ?;
configurable int|string myVal = ?;
// configurable map<int> itemCodes = {"item1": 11, "item2": 12, "item3": 45};
configurable string[] itemNames = ["item1", "item2", "item3"];

public function main() {
    io:println("Hello, World!");
}
