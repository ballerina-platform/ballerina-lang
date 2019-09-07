## Module overview

This module provides utility functions to manipulate built-in `xml` data type. This module helps to convert a `json` to an `xml`, or convert a `table` to an `xml`.

### Examples

#### Convert a `json` to an `xml`
```ballerina
import ballerina/xmlutils;

json personJson = {
    name: "John",
    age: 30
};

function convertJsonToXml() {
    xml|error personXml = xmlutils:fromJSON(personJson);
    
    if (personXml is error) {
        // handle the error
    } else {
        // handle the xml 
    }
}
```

#### Convert a `table` to an `xml`
```ballerina
import ballerina/xmlutils;

type Person record {
    int id;
    string name;
};

function convertTableToXml() {
    table<Person> peopleTable = table{
        { key id, name },
        [
            { 1, "Sam" },
            { 2, "John" }
        ]
    };
    
    xml peopleXml = fromTable(peopleTable);
}
```
