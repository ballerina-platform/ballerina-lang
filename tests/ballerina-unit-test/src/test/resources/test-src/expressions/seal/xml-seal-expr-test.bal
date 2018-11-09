
//----------------------------XML Seal -------------------------------------------------------------
function sealXMLToAny() returns any {

    xml xmlValue = xml `<book>The Lost World</book>`;

    any anyValue = xmlValue.seal(any);
    return anyValue;
}

function sealXMLToXML() returns xml {

    xml xmlValue = xml `<book>The Lost World</book>`;

    xml returnValue = xmlValue.seal(xml);
    return returnValue;
}

function sealXMLToAnydata() returns anydata {

    xml xmlValue = xml `<book>The Lost World</book>`;

    anydata anydataValue = xmlValue.seal(anydata);
    return anydataValue;
}
