import ballerina/io;

public function main() {
    string? input = ();

    // You can use a type-guard with a conditional expression to check if a value is nil
    // and if so, provide a new value or else use the input value itself.
    string name = input is () ? "John Doe" : input;
    io:println("Name: ", name);

    // You can achieve the same using the Elvis operator as follows.
    // Here, it implicitly returns the value `input` if it's not nil.
    name = input ?: "John Doe";
    io:println("Name: " + name);

    input = "Bill Lambert";
    name = input ?: "John Doe";
    io:println("Name: " + name);
}
