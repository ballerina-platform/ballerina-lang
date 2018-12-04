import ballerina/io;

// Constants are defined using the `const` modifier.
const string GET = "GET";

// Constants can be defined without type using `var` keyword. Then the type will be inferred from the RHS.
const POST = "POST";

// These constants can be used to create type definitions. This type definition can only hold `GET` or `POST` values.
type ACTION GET|POST;

// Integer constant.
const int I = 125;

public function main() {

    // This is identical to `ACTION get = "GET";`.
    ACTION get = GET;
    testAction(get);

    ACTION post = "POST";
    testAction(POST);

    // We can concatenate I with any other integer value.
    int value = I + 10;
    io:println(value);

}

function testAction(ACTION action) {
    if (action == GET) {
        io:println("GET action");
    } else if (action == POST) {
        io:println("POST action");
    }
}
