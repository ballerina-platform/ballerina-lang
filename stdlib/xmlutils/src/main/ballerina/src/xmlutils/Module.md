## Module overview

This module provides utility functions to manipulate built-in `xml` data type. 
It provides APIs to convert a `json` to an `xml`, or convert a `table` to an `xml`.

### Samples

#### Convert a `json` to its `xml` representation
```ballerina
import ballerina/io;
import ballerina/xmlutils;

json personJson = {
    name: "John",
    age: 30
};

public function main() {
    xml | error personXml = xmlutils:fromJSON(personJson);

    if (personXml is error) {
        io:println("An error occurred: ", personXml);
    } else {
        io:println(personXml);
    }
}
```

#### Convert a `table` to its `xml` representation
```ballerina
import ballerina/io;
import ballerina/xmlutils;

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

    xml peopleXml = xmlutils:fromTable(peopleTable);
    io:println(peopleXml);
}
```
