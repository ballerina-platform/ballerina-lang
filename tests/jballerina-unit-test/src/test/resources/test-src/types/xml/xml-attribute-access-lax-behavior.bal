
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

function getXMLMap() returns map<xml> {
        map<xml> xmap = {};
        xmap["a"] = xml `<elem attr="val"></elem>`;
        return xmap;
}
