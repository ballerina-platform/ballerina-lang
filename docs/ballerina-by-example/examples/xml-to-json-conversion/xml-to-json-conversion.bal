import ballerina.io;

function main (string[] args) {
    //Create an XML.
    var x, _ = <xml>("<h:Store id = \"AST\" xmlns:h=\"http://www.test.com\">" +
                     "<h:name>Anne</h:name>" +
                     "<h:address><h:street>Main</h:street>" +
                     "<h:city>94</h:city></h:address>" +
                     "<h:code><h:item>4</h:item><h:item>8</h:item></h:code>" +
                     "</h:Store>");
    //Convert to JSON with default attribute prefix and with namespaces.
    json j1 = x.toJSON({});
    io:println(j1);

    //Convert to JSON with custom attribute prefix and without namespaces.
    json j2 = x.toJSON({attributePrefix:"#", preserveNamespaces:false});
    io:println(j2);
}
