import ballerina/io;

// This function accepts the age and returns the age category. If the age is a
// negative value, it returns an error. 
function getAgeCategory(int age) returns string|error {
    if (age < 0) {
        error e = { message: "Invalid" };
        return e;
    } else if (age <= 18) {
        return "Child";
    } else {
        return "Adult";
    }
}

function main(string... args) {

    // The `getAgeCategory` function returns a string for a valid age. This string
    // is matched with the string pattern in the match expression. Then the same value is assigned to the error
    //category.

    string ageCategory = getAgeCategory(25) but {
        string s => s,
        error e => e.message
    };
    io:println(ageCategory);


    // If a negative value is provided, this returns an error. This error is matched with
    // the error pattern in the match expression. Then the message to be sent with the error is assigned based on
    //the error category.

    ageCategory = getAgeCategory(-5) but {
        string s => s,
        error e => e.message
    };
    io:println(ageCategory);


    // A match expression has an implicit default pattern. This is because
    // the `getAgeCategory` function returns a string and the type of `ageCategory`
    // is also string. Therefore, it is not required to provide a pattern to match the string type
    // in the match expression. Any string value returned by the function
    // is assigned to the `ageCategory` by default.
    ageCategory = getAgeCategory(25) but {
        error e => e.message
    };
    io:println(ageCategory);
}
