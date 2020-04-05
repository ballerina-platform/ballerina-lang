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
    output = output + i.toString() + ":" + v + " ";
}

function testXMLWithArityOne () returns (string) {
    output = "";
    foreach var x in xdata {
        if x is xml {
            concatString(x.toString());
        }
    }
    return output;
}

function testXMLWithArityTwo () returns (string) {
    output = "";
    int i = 0;
    foreach var x in xdata {
        if x is xml {
            concatIntString(i, x.toString());
            i += 1;
        }
    }
    return output;
}

function testXMLWithArityChildren () returns (string) {
    output = "";
    int i = 0;
    foreach var x in (xdata/*).elements() {
        if x is xml {
            concatIntString(i, x.toString());
            i += 1;
        }
    }
    return output;
}
