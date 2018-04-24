import ballerina/io;

// This function accept the age and returns the age category. If the age is a negative value,
// it returns an error. 
function getAgeCategory(int age) returns string|error {
    if (age < 0) {
        error e = {message: "Invalid"};
        return e;
    } else if (age <= 18) {
        return "Child";
    } else {
        return "Adult";
    }
}

function main (string... args) {

    // getAgeCategory function will return a string for a valid age. Then it will be matched 
    // to the string pattern in the match statement, and will assign the same string as the
    // error ctegory. 
    string ageCategory = getAgeCategory(25) but {
                            string s => s,
                            error e => e.message
                        };
    io:println(ageCategory);


    // Providing a negative value will return an error. Then it will match to the 
    // error pattern in the match expression, and will assign the message of the error,
    // as the error category
    ageCategory = getAgeCategory(-5) but {
                      string s => s,
                      error e => e.message
                  };
    io:println(ageCategory);


    // Match expression also has an implicit default pattern. That is, since the getAgeCategory function 
    // returns a string and the type of ageCategory is also string, then we dont need to give a pattern 
    // to match string type inside the match expression. Any string value returned by the function will
    // get assigned to ageCategory by default.
    ageCategory = getAgeCategory(25) but {
                      error e => e.message
                  };
    io:println(ageCategory);
}
