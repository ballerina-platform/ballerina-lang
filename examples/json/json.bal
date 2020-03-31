import ballerina/io;

public function main() {
    // Create a JSON string value.
    json j1 = "Apple";
    io:println(j1);

    // Create a JSON number value.
    json j2 = 5.36;
    io:println(j2);

    // Create a JSON true value.
    json j3 = true;
    io:println(j3);

    // Create a JSON false value.
    json j4 = false;
    io:println(j4);

    // Create a JSON null value.
    json j5 = null;

    // Creates a JSON Object. This is equivalent to a `map<json>`.
    json j6 = {name: "apple", color: "red", price: j2};
    io:println(j6);

    // The `.toJsonString()` defined for `json` values returns a `string` that
    // represents the value in JSON format.
    io:println(j6.toJsonString());

    // Create a JSON Array. This is equivalent to a `json[]`.
    json j7 = [1, false, null, "foo", {first: "John", last: "Pala"}];
    io:println(j7.toJsonString());

    // The `.mergeJson()` method can be used to merge two `json` values.
    // If either of the two values is `null`, the result of `.mergeJson()` is
    // the other value.
    json j8 = checkpanic j5.mergeJson(j7);
    io:println(j8.toJsonString());

    // `.mergeJson()` can also be used to merge two JSON objects.
    // Where both `json` values are mappings, for each entry in the second,
    // if an entry is not present in the first by the same key, the entry
    // is added to the first mapping. If there is already a field by the same
    // key in the first mapping, `.mergeJson()` is called recursively.
    json j9 = {name: "Anne", age: null, marks: {math: 90, language: 95}};
    json j10 = {name: (), age: 20, marks: {physics: 85}};
    json j11 = checkpanic j9.mergeJson(j10);
    io:println(j11.toJsonString());

    // Reference equality checks between `j9` and `j11` evaluate to true since
    // the `j9` itself is updated and returned if the merge is successful.
    io:println(j9 === j11);

    // `.mergeJson()` returns an `error` if the values cannot be merged.
    // For example, attempting to merge a mapping value with a non-mapping value
    // or two non-mapping values where neither is `null`.
    json|error j12 = j2.mergeJson(j3);
    io:println(j12);

    // The `.fromJsonString()` defined on `string` values attempts parsing the
    // string expected to be in the JSON format and returns the represented JSON value 
    // if successful. This method returns an error if the string cannot be parsed.
    string s = j6.toJsonString();
    json j13 = checkpanic s.fromJsonString();
    io:println(j13.toJsonString());

    // The value would be equal to the original value from which the string
    // was created.
    io:println(j6 == j13);
}
