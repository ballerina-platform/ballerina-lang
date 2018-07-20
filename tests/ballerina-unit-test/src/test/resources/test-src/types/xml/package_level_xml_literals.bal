xmlns "http://ballerina.com/b" as ns1; 

xml person = xml `<p:person xmlns:p="foo" xmlns:q="bar">hello</p:person>`;
xml student = xml `<ns1:student>hello</ns1:student>`;

function testPackageLevelXML() returns (xml, xml) {
    return (person, student);
}

    