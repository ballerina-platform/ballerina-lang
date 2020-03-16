import ballerina/lang.'xml;

function testXMLAsMapContent() returns [string|error?, string|error?, boolean] {
    map<xml> xmap = getXMLMap();
    string|error val = xmap.a.attr;
    string|error? val2 = xmap.a?.attr;
    string|error? val3 = xmap.a?.attr2;
    return [val, val2, val3 is ()];
}

function testXMLASMapContentInvalidKey() returns string|error? {
    map<xml> xmap = getXMLMap();
    string|error val = xmap.b.attr;
    return val;
}

function testXMLAttributeWithNSPrefix() returns
    [string|error?, string|error?, string|error?, boolean, boolean] {
    map<xml> xmap = {};
    xmap["a"] = xml `<elem xmlns="ns-uri" attr="val" xml:space="preserve"></elem>`;
    string|error val = xmap.a.'xml:space;
    string|error? val2 = xmap.a?.'xml:space;
    string|error? val3 = xmap.b?.'xml:space;
    return [val, val2, val3];
}

function getXMLMap() returns map<xml> {
    map<xml> xmap = {};
    xmap["a"] = xml `<elem attr="val"></elem>`;
    return xmap;
}
