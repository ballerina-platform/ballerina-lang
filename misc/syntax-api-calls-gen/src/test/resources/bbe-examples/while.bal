import ballerina/io;
import ballerina/lang.'int as ints;

public function main() {
    int i = 0;
    // This is a basic `while` loop.
    while (i < 3) {
        io:println(i);
        i = i + 1;
    }

    while (true) {
        string input = io:readln("Enter age: (q to exit): ");
        if (input == "q") {
            // The `break` statement can be used to break the loop.
            break;
        }
        var value = ints:fromString(input);
        int age;
        if (value is int) {
            age = value;
        } else {
            io:println("Invalid value, try again.");
            // The `continue` statement can be used to move to the
            // next loop iteration immediately.
            continue;
        }
        string name = io:readln("Enter name: ");
        io:println(name + ((age >= 18) ? " can" : " cannot") + " vote!");
    }

}
