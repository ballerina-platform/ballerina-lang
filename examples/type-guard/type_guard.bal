import ballerina/io;

public function main() {
    // The `value` variable can hold a value of the type `string`, `int` or `boolean`.
    string|int|boolean value = 10;

    // The type guard can be used with `value` to test to which of the types it belongs to and perform conditional
    // logic based on the type which it belongs to.
    if (value is string) {
        // The type of `value` within this block is narrowed to `string`, and thus, `value` can be assigned to a
        // variable of the type `string`.
        string str = value;
        io:println("value is a string: ", str);
    } else if (value is int) {
        // The type of `value` within this block is narrowed to `int`, and thus, `value` can be used in a
        // context where an `int` is expected.
        io:println("value is an int: ", value);
        io:println("value + 1: ", addOneToInt(value));

        // If the value is updated within a type guard, the type is reset to the original type.
        value = "Hello World";

        // The type test needs to be used again since the type of `value` is reset to `string|int|boolean`.
        if (value is int) {
            // The `value` is an `int` here.
            int i = value;
            io:println("- value is an int: ", i);
        } else {
            // The `value` is `string` or `boolean` here.
            string|boolean sb = value;
            io:println("- value is string|boolean: ", sb);
        }
    } else {
        // Within this block, the type of `value` is `boolean` since the previous `if` and `else if` blocks handle the
        // other possible scenarios.
        if (value) {
            io:println("s is 'true'");
        }
    }
}

// A function that expects an `int` as an argument.
function addOneToInt(int i) returns int {
    return i + 1;
}
