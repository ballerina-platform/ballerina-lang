
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
    xmap["a"] = xml `<elem xmlns="ns-uri" attr="val" xml:attr="xml-val"></elem>`;
    string|error val = xmap.a.'xml:attr;
    string|error? val2 = xmap.a?.'xml:attr;
    string|error? val3 = xmap.b?.'xml:attr;
    string|error? val4 = xmap.a.'xml:attr2;
    string|error? val5 = xmap.a?.'xml:attr2;
    return [val, val2, val3, val4 is (), val5 is ()];
}

function getXMLMap() returns map<xml> {
    map<xml> xmap = {};
    xmap["a"] = xml `<elem attr="val"></elem>`;
    return xmap;
}
