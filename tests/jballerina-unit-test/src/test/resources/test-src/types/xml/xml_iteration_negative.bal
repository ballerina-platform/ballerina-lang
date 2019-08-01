xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
                    <p:name>bob</p:name>
                    <p:address>
                        <p:city>NY</p:city>
                        <q:country>US</q:country>
                    </p:address>
                    <q:ID>1131313</q:ID>
                  </p:person>`;

function testExcessVars() {
    foreach var [i, x, y] in xdata {
    }
}

function testExcessVarsIterableOp() {
    xdata.forEach(function ([int, xml, string] entry) {});
}
