string output = "";

xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;

function concatString (string value) {
    output = output + value + " ";
}

function concatIntString (int i, string v) {
    output = output + i + ":" + v + " ";
}

function testXMLWithArityOne () (string) {
    output = "";
    foreach x in xdata {
        var s = <string> x;
        concatString(s);
    }
    return output;
}

function testXMLWithArityTwo () (string) {
    output = "";
    foreach i,x in xdata {
        var s = <string> x;
        concatIntString(i,s);
    }
    return output;
}

function testXMLWithArityChildren () (string) {
    output = "";
    foreach i,x in xdata.children().elements() {
        var s = <string> x;
        concatIntString(i,s);
    }
    return output;
}
