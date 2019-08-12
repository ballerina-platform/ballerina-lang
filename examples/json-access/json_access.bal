import ballerina/io;

public function main() {
    // Define a variable of type `json` that holds a mapping value.
    json j1 = {
        fname: "Mary",
        lname: "Jane",
        address: {
            line: "20 Palm Grove",
            city: "Colombo 03",
            country: "Sri Lanka"
        }
    };

    // Field access is allowed on the `json` typed variable, but the return
    // type would be a union of `json` and `error`. An error will be returned
    // either if the value on which the access is done is not a mapping or if
    // it is a mapping, but it does not contain the particular key.
    json|error r1 = j1.fname;
    // `r1` is of type `json` since `j1` is a mapping and contains the field `fname`.
    io:println(r1);

    // Similarly, chained field access is also allowed for `json`. If an `error`
    // is encountered at any point during the evaluation, evaluation stops at
    // that point and the result would be an error.
    json|error r2 = j1.address.city;
    // `r2` is of type `json` since `j1` is a mapping with the field `address`, which in
    // turn is a mapping with the field `city` which will be the resultant value.
    io:println(r2);

    // Attempting to access a non-existent field would result in an error.
    json|error r3 = j1.age.year;
    // `r3` is of type `error` since `j1` does not have a field with the key `age`.
    io:println(r3);

    // Similarly, optional field access is also allowed on lax types.
    // The main difference between field and optional field access for lax types
    // is that when a key is not found in a mapping, while the former returns
    // an `error`, the latter returns `null` (`()`) instead.
    // Similar to how errors are lifted with field access, `()` is lifted
    // for optional field access, allowing chained access.
    json|error r4 = j1?.age?.year;
    // `r4` is `()` since `j1` does not have a field with the key `age`.
    io:println(r4);

    // Moreover, when optional field access is done on `null` (`()`), the result would
    // also be `()`, whereas for field access it would have been an `error`.
    json j2 = null;
    json|error r5 = j2?.name;
    // `r5` is `()` since `j2` is `()`.
    io:println(r5);
}
