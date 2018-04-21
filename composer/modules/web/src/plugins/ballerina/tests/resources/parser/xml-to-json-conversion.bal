import ballerina/lang.system;
import ballerina/lang.xmls;

function main (string... args) {
    //Create xml
    xml x = xmls:parse("<h:Store id = \"AST\" xmlns:h=\"http://www.test.com\">" +
                     "<h:name>Anne</h:name>" +
                     "<h:address><h:street>Main</h:street>" +
                     "<h:city>94</h:city></h:address>" +
                     "<h:code><h:item>4</h:item><h:item>8</h:item></h:code>" +
                        "</h:Store>");
    //Convert to JSON with default attribute prefix and with namespaces.
    xmls:Options options1 = {};
    json j1 = xmls:toJSON(x, options1);
    system:println(j1);

    //Convert to JSON with custom attribute prefix and without namespaces.
    xmls:Options options2 = {attributePrefix : "#", preserveNamespaces : false};
    json j2 = xmls:toJSON(x, options2);
    system:println(j2);
}
