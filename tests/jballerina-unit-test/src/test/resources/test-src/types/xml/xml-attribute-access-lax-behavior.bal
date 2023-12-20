import ballerina/lang.'xml;

function testXMLAttributeWithNSPrefix() returns [string|error?, string|error?] {
    xml a = xml `<elem xmlns="ns-uri" attr="val" xml:space="preserve"></elem>`;
    string|error val = a.'xml:space;
    string|error? val2 = a?.'xml:space;
    return [val, val2];
}

function testXMLDirectAttributeAccess() returns [boolean, boolean, boolean, boolean] {
    xml x = xml `<elem xmlns="ns-uri" attr="val" xml:space="default"></elem>`;
    return [x.attr is string, x?.attr is string, x.attrNon is error, x?.attrNon is ()];
}
