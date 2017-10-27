function main (string[] args) {
    //Create a XML.
    var x, _ = <xml>("<h:Store id = \"AST\" xmlns:h=\"http://www.test.com\">" +
                     "<h:name>Anne</h:name>" +
                     "<h:address><h:street>Main</h:street>" +
                     "<h:city>94</h:city></h:address>" +
                     "<h:code><h:item>4</h:item><h:item>8</h:item></h:code>" +
                     "</h:Store>");
    //Convert to JSON with default attribute prefix and with namespaces.
    xmlOptions options1 = {};
    json j1 = x.toJSON(options1);
    println(j1);

    //Convert to JSON with custom attribute prefix and without namespaces.
    xmlOptions options2 = {attributePrefix:"#", preserveNamespaces:false};
    json j2 = x.toJSON(options2);
    println(j2);
}
