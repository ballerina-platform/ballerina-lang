import ballerina/io;
import ballerina/lang.'xml as xmllib;

xmlns "http://ballerina.com/aa" as ns0;

public function main() {
    // Creates an XML element, which has attributes that are bound to a namespace as well as ones that are not.
    xmllib:Element x1 = <xmllib:Element> xml `<ns0:book ns0:status="available" count="5"/>`;
    io:println(x1);

    // A single attribute that is bound to a namespace can be accessed using its qualified name.
    io:println(x1.ns0:status);

    // Attribute access expressions are lax typed.
    string|error count = x1.count;
    io:println(count);

    // Accessing a non-existent attribute will return an error.
    string|error count2 = x1.count2;
    io:println(count2 is error);

    // It is possible to get all the attributes of a `xml` element.
    map<string> attributeMap = x1.getAttributes();
    io:println(attributeMap);

    // An attribute can also be accessed using the string representation of the qualified name from the attribute map.
    string? s = attributeMap["{http://ballerina.com/aa}status"];
    io:println(s);

    // Updates an attribute by updating the attribute map of a `xml` element.
    attributeMap[ns0:status] = "Not Available";
    io:println(x1.ns0:status);
}
