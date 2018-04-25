import ballerina/io;

function main(string... args) {
    // This creates an XML and associates it with a variable.
    var x = xml `<h:Store id = "AST" xmlns:h="http://www.test.com">
                     <h:name>Anne</h:name>
                     <h:address><h:street>Main</h:street>
                     <h:city>94</h:city></h:address>
                     <h:code><h:item>4</h:item><h:item>8</h:item></h:code>
                     </h:Store>`;
    // This converts the XML to JSON with namespaces and the default 'attributePrefix'.
    json j1 = x.toJSON({});
    io:println(j1);

    // This converts the XML to JSON without namespaces and with a custom 'attributePrefix'.
    json j2 = x.toJSON({ attributePrefix: "#", preserveNamespaces: false });
    io:println(j2);
}
