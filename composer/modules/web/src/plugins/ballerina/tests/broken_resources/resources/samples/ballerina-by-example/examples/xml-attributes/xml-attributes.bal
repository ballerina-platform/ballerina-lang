import ballerina/lang.system;

xmlns "http://ballerina.com/aa" as ns0;

function main (string... args) {

    // Create an XML element having attributes that are bound to a namespace as well as that are not.
    xml x1 = xml `<ns0:book ns0:status="available" count="5"/>`;
    system:println(x1);

    // A single attribute that is bound to a namespace can be accessed using its qualified name. 
    system:println(x1@[ns0:status]);

    // An attribute can also be accessed using its the string representation of the qualified name. 
    string s = x1@["{http://ballerina.com/aa}status"];
    system:println(s);

    // An attribute that is not bound to a namespace can be accessed using the string representation of the name.
    system:println(x1@["count"]);

    // Accessing an attribute using with a dynamic name.
    string attributeName = "count";
    system:println(x1@[attributeName]);

    // Update an attribute.
    x1@[ns0:status] = "Not Available";
    system:println(x1@[attributeName]);

    // Getting all the attributes at once, can be done. However this cannot be assigned to any variable.
    system:println(x1@);

    // To assign all the attributes to a variable, it can be cast to a map. Then the values can be accessed one by one using the map access syntax.
    map attributeMap = <map> x1@;
    system:println(attributeMap);
    system:println(attributeMap["count"]);
}
