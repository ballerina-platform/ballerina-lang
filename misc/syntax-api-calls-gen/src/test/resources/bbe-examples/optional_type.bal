import ballerina/io;

// This function (optionally) returns a `string` value. In this example, `string?` is equivalent to `string|()`.
function getLargestCountryInContinent(string continent) returns string? {
    match continent {
        "North America" => { return "USA"; }
        "South America" => { return "Brazil"; }
        "Africa" => { return "Algeria"; }
        "Europe" => { return "Russia"; }
        "Asia" => { return "Russia"; }
        "Australia" => { return "Australia"; }
        // In Ballerina, the `nil` type that is provided as `()` contains a single value named "nil". This is used
        // to represent the absence of any other value. The `nil` value is written as `()`.
        "Antarctica" => { return (); }
    }
    // Not having a return statement at the end is also the same as explicitly returning `()`.
}

public function main() {
    // It is optional for `getLargestCountryInContinent()` to return a value of type `string`. 
    // Thus, the value could be either of type `string` or of type `()` and needs to be handled explicitly.
    string continent = io:readln("Enter continent: ");
    string? country = getLargestCountryInContinent(continent);

    // The type test can then be used to check if the value is in fact a `string` and then operate on it.
    if country is string {
        io:println("The largest country in ", continent, " is ", country);
    } else {
        io:println("The continent ", continent, " does not have any countries");
    }
}
