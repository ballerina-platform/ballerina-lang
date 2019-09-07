## Module overview

This module provides utility functions to manipulate built-in `json` data type. This module helps to convert an `xml` to a `json`, or convert a `table` to a `json`.

### Examples

#### Convert an `xml` to a `json`
```ballerina
import ballerina/jsonutils;

function convertXmlToJson() {
    var xmlSubElement1 = xml `<!-- outer comment -->`;
    var xmlSubElement2 = xml `<name>Sam</name>`;
    xml xmlElement = xmlSubElement1 + xmlSubElement2;

    json|error jsonObject = jsonutils:fromXML(xmlElement);
    if (jsonObject is error) {
        // handle the error
    } else {
        // handle the jsonObject
    }
}
```

#### Convert a `table` to a `json`
```ballerina
import ballerina/jsonutils;

type Person record {
    int id;
    string name;
};

table<Person> peopleTable = table{
    { key id, name },
    [
        { 1, "Sam" },
        { 2, "John" }
    ]
};

json peopleJson = fromTable(peopleTable);
```
