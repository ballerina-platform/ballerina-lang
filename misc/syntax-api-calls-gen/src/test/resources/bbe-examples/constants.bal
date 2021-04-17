import ballerina/io;

// Constants are defined using the `const` modifier.
const float PI = 3.14159;

// Constants can be defined without the type. Then, the type is inferred from the right hand side.
const SPEED_OF_LIGHT = 299792000;

// Constants of the `map` type can be defined as well. The constraint of these constant maps should be either a simple type
// or another map type, which should eventually have a simple type constraint. `var` cannot be used to declare
// constant maps. Therefore, the correct type should be present when declaring the constant.
const map<string> data = { "user": "Ballerina", "ID": "1234" };

// Constant with nested map literals.
const map<map<string>> complexData = { "data": data,
                                       "data2": { "user": "WSO2" } };

public function main() {
    io:println("PI: ", PI);

    int distanceOfALightYear = SPEED_OF_LIGHT * 3600 * 24 * 365;
    io:println("Distance of a light year (meters): ", distanceOfALightYear);

    io:println(data);
    io:println(complexData);

    // Values of a constant map can be accessed and used as usual.
    io:println(complexData["data"]["user"]);

    // However, updating a constant map similar to the following will produce a compile-time error:
    // data.ID = "4321";
    // data.newField = 10;
}

