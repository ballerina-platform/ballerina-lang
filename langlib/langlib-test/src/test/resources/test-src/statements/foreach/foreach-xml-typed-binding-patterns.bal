string output = "";

xml xdata = xml `<p:person xmlns:p="foo" xmlns:q="bar">
        <p:name>bob</p:name>
        <p:address>
            <p:city>NY</p:city>
            <q:country>US</q:country>
        </p:address>
        <q:ID>1131313</q:ID>
    </p:person>`;

function concatIntXml(int i, xml x) {
    output = output + i.toString() + ":" + x.toString() + " ";
}

// ---------------------------------------------------------------------------------------------------------------------

function testXmlWithRootWithSimpleVariableWithoutType() returns string {
    output = "";

    int i = 0;
    foreach var v in xdata {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    return output;
}

function testXmlWithRootWithSimpleVariableWithType() returns string {
    output = "";

    int i = 0;
    foreach xml|string v in xdata {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testXmlInnerElementsWithSimpleVariableWithoutType() returns string {
    output = "";

    int i = 0;
    foreach var v in xdata.* {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    return output;
}

function testXmlInnerElementsWithSimpleVariableWithType() returns string {
    output = "";

    int i = 0;
    foreach xml|string v in xdata.* {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    return output;
}

// ---------------------------------------------------------------------------------------------------------------------

function testEmptyXmlIteration() returns string {
    output = "";

    xml data = xml `ABC`;

    int i = 0;
    foreach var v in data.* {
        if v is xml {
            concatIntXml(i, v);
            i += 1;
        }
    }
    return output;
}
