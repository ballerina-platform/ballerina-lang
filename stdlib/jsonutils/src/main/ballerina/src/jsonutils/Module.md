## Module overview

This module provides utility functions to manipulate built-in `json` data type. This module helps to convert an `xml` to a `json`, or convert a `table` to a `json`.

### Samples

#### Convert an `xml` to a `json`
```ballerina
import ballerina/io;
import ballerina/jsonutils;

public function main() {
    var xmlSubElement1 = xml `<!-- outer comment -->`;
    var xmlSubElement2 = xml `<name>Sam</name>`;
    xml xmlElement = xmlSubElement1 + xmlSubElement2;

    json | error jsonObject = jsonutils:fromXML(xmlElement);
    if (jsonObject is error) {
        io:println("An error occurred: ", jsonObject);
    } else {
        io:println(jsonObject.toJsonString());
    }
}
```

#### Convert a `table` to a `json`
```ballerina
import ballerina/io;
import ballerina/jsonutils;

type Person record {
    int id;
    string name;
};

public function main() {
    table<Person> peopleTable = table {
        {key id, name},
        [
            { 1, "Sam"},
            { 2, "John"}
        ]
    };

    json peopleJson = jsonutils:fromTable(peopleTable);
    io:println(peopleJson.toJsonString());
}
```
